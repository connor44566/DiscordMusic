package minn.music.managers;

import minn.music.MusicBot;
import minn.music.commands.Container;
import minn.music.commands.GenericCommand;
import minn.music.hooks.CommandListener;
import minn.music.hooks.MentionListener;
import minn.music.util.EntityUtil;
import minn.music.util.IgnoreUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommandManager
{
	private final List<GenericCommand> noPrivateCommands = new LinkedList<>();
	private final List<GenericCommand> commands = new LinkedList<>();
	private final List<CommandListener<GenericCommand>> listeners = new LinkedList<>();
	private final List<MentionListener> mentionListeners = new LinkedList<>();
	private final List<Container> containers = new LinkedList<>();
	private static final Map<String, Integer> usage = new HashMap<>();
	public final MusicBot bot;
	public final static SimpleLog LOG = SimpleLog.getLog("CommandManager");

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
		final Thread t = new Thread(r, "CommandExecutorThread");
		t.setDaemon(true);
		t.setPriority(5);
		return t;
	});

	private JDA api;

	/**
	 * Creates a new CommandManager.
	 *
	 * @param api A JDA implementation. (Make a new one per shard)
	 * @throws NullPointerException If JDA instance or MusicBot instance is null.
	 */
	public CommandManager(JDA api, MusicBot bot) throws NullPointerException
	{
		if (api == null || bot == null)
		{
			LOG.log(new NullPointerException());
			throw new NullPointerException();
		}
		this.api = api;
		this.bot = bot;

		this.api.addEventListener((EventListener) event ->
		{
			if (event instanceof GuildMessageReceivedEvent
					&& EntityUtil.mentionsMe(((GuildMessageReceivedEvent) event).getMessage()))
			{
				for (MentionListener listener : getMentionListeners())
				{
					listener.onMention((GuildMessageReceivedEvent) event);
				}
			}
			if (event instanceof MessageReceivedEvent)
				handleMessage((MessageReceivedEvent) event);
		});

	}

	private void handleMessage(MessageReceivedEvent event)
	{
		if ((!event.getMessage().getRawContent().startsWith(MusicBot.config.prefix)
				&& (event.getGuild() != null
				&& !event.getMessage().getRawContent().startsWith(PrefixManager.getPrefix(event.getGuild())))) // If Guild -> has custom prefix?
				|| event.getAuthor() == event.getJDA().getSelfInfo()
				|| IgnoreUtil.isIgnored(event.getTextChannel()))
			return;
		String trimmed = null;
		if (!PrefixManager.isCustom(event.getMessage(), event.getGuild()))
			trimmed = event.getMessage().getRawContent().substring(MusicBot.config.prefix.length()).trim();
		else
			trimmed = event.getMessage().getRawContent().substring(PrefixManager.getPrefix(event.getGuild()).length()).trim();
		if (trimmed.isEmpty()) return;
		final String com = trimmed.split("\\s+", 2)[0];

		// General Commands
		for (GenericCommand c : getCommands())
		{
			if (!c.getAlias().equalsIgnoreCase(com))
				continue;
			String finalTrimmed = trimmed;
			executor.submit(() -> {
				try
				{
					GenericCommand.CommandEvent ce = new GenericCommand.CommandEvent(event, finalTrimmed);
					c.invoke(ce);
					for (CommandListener<GenericCommand> listener : getListeners())
						listener.onCommand(c, ce);
					onCommand(c, ce); // static synced listener
				} catch (Exception e)
				{
					LOG.log(e);
				}
			});

			return;
		}

		// Containers
		for (Container c : getContainers())
		{
			if (!c.isPrivate() && event.isPrivate())
				continue;
			// Info
			if (com.equalsIgnoreCase(c.getAlias()))
			{
				GenericCommand.CommandEvent ce = new GenericCommand.CommandEvent(event, trimmed);
				ce.send(c.getInfo());
				for (CommandListener<GenericCommand> listener : getListeners())
					listener.onCommand(c, ce);
				onCommand(c, ce); // static synced listener
				return;
			}
			// Command in Container
			GenericCommand cmd = c.getCommand(com);
			if (cmd == null)
				continue;
			String finalTrimmed1 = trimmed;
			executor.submit(() ->
			{
				try
				{
					GenericCommand.CommandEvent ce = new GenericCommand.CommandEvent(event, finalTrimmed1);
					cmd.invoke(ce);
					for (CommandListener<GenericCommand> listener : getListeners())
						listener.onCommand(cmd, ce); // Calls listener with cmd
					onCommand(cmd, ce); // static synced listener
				} catch (Exception e)
				{
					LOG.log(e);
				}
			});
			return;
		}

		// Private
		if (event.isPrivate())
			return;
		for (GenericCommand c : getNonPrivateCommands())
		{
			if (!c.getAlias().equalsIgnoreCase(com))
				continue;
			String finalTrimmed2 = trimmed;
			executor.submit(() -> {
				try
				{
					GenericCommand.CommandEvent ce = new GenericCommand.CommandEvent(event, finalTrimmed2);
					c.invoke(ce);
					for (CommandListener<GenericCommand> listener : getListeners())
						listener.onCommand(c, ce);
					onCommand(c, ce); // static synced listener
				} catch (Exception e)
				{
					LOG.log(e);
				}
			});
			return;
		}
	}

	/**
	 * Used to override default {@link CommandListener CommandListener} implementation used by the managers.
	 *
	 * @param listener A CommandListener implementation.
	 */
	public void addCommandListener(CommandListener<GenericCommand> listener)
	{
		if (listener == null)
			LOG.log(new NullPointerException("Listener to override can not be null!"));
		else
			this.listeners.add(listener);
	}

	/**
	 * Returns all commands that are available to both Guild and Private channels.
	 *
	 * @return List
	 */
	public List<GenericCommand> getCommands()
	{
		return Collections.unmodifiableList(new LinkedList<>(commands));
	}

	/**
	 * Returns all commands that are available only available to guilds.
	 *
	 * @return List
	 */
	public List<GenericCommand> getNonPrivateCommands()
	{
		return Collections.unmodifiableList(new LinkedList<>(noPrivateCommands));
	}

	/**
	 * Provides a thread safe version of {@link CommandManager#containers}
	 *
	 * @return Thread Safe Container list.
	 */
	public List<Container> getContainers()
	{
		return Collections.unmodifiableList(new LinkedList<>(containers));
	}

	public List<MentionListener> getMentionListeners()
	{
		return Collections.unmodifiableList(new LinkedList<>(mentionListeners));
	}

	public List<CommandListener<GenericCommand>> getListeners()
	{
		return Collections.unmodifiableList(new LinkedList<>(listeners));
	}

	public static Map<String, Integer> getUsage()
	{
		return Collections.unmodifiableMap(new HashMap<>(usage));
	}

	public static int getUsage(String alias)
	{
		final String[] c = {null};
		Map<String, Integer> use = getUsage();
		use.forEach((cmd, i) ->
		{
			if (cmd.equalsIgnoreCase(alias))
				c[0] = cmd;
		});
		if (c[0] == null)
			return -1;
		return use.get(c[0]);
	}

	public JDA getJDA()
	{
		return api;
	}

	/**
	 * Used to register a new {@link GenericCommand Command}.
	 *
	 * @param command GenericCommand.
	 */
	public <V extends GenericCommand> void registerCommand(V command)
	{
		if (command == null || command instanceof Container)
		{
			LOG.log(new IllegalArgumentException("Command must implement GenericCommand and not Container."));
			return;
		}
		if ((command).isPrivate())
			commands.add(command);
		else
			noPrivateCommands.add(command);
	}

	/**
	 * Used to structure command list.
	 *
	 * @param container A Not-Null Container.
	 */
	public <V extends Container> void registerContainer(V container)
	{
		assert container != null && !container.isEmpty();
		if (containers.contains(container))
			return;
		containers.add(container);
	}

	public <V extends MentionListener> void registerMentionListener(V listener)
	{
		assert listener != null;
		mentionListeners.add(listener);
	}

	private static synchronized void onCommand(GenericCommand command, GenericCommand.CommandEvent event)
	{
		if (usage.containsKey(command.getAlias()))
			usage.put(command.getAlias(), usage.get(command.getAlias()) + 1);
		else
			usage.put(command.getAlias(), 1);
		LOG.info(EntityUtil.transform(event.author) + ": " + command.getAlias() + " [" + getUsage(command.getAlias()) + "]");
	}

}

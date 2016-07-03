package minn.music.managers;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.hooks.CommandListener;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
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
	public final MusicBot bot;
	private final static SimpleLog LOG = SimpleLog.getLog("CommandManager");

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
			if (event instanceof MessageReceivedEvent)
				handleMessage((MessageReceivedEvent) event);
		});

		listeners.add(new CommandListenerImpl());
	}

	private void handleMessage(MessageReceivedEvent event)
	{
		if(!event.getMessage().getRawContent().startsWith(bot.config.prefix) || event.getAuthor() == event.getJDA().getSelfInfo())
			return;
		final String com = event.getMessage().getRawContent().split("\\s+", 2)[0];
		for (GenericCommand c : commands)
		{
			if((bot.config.prefix + c.getAlias()).equalsIgnoreCase(com))
			{
				executor.submit(() -> c.invoke(new GenericCommand.CommandEvent(event)));
				for(CommandListener<GenericCommand> listener : listeners)
					listener.onCommand(c);
				return;
			}
		}
		if (event.isPrivate())
			return;
		for (GenericCommand c : noPrivateCommands)
		{
			if((bot.config.prefix + c.getAlias()).equalsIgnoreCase(com))
			{
				executor.submit(() -> c.invoke(new GenericCommand.CommandEvent(event)));
				for(CommandListener<GenericCommand> listener : listeners)
					listener.onCommand(c);
				return;
			}
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
	 * Used to register a new {@link GenericCommand Command}.
	 *
	 * @param command GenericCommand.
	 */
	public void registerCommand(GenericCommand command)
	{
		if (!(command instanceof GenericCommand))
		{
			LOG.log(new IllegalArgumentException("Command must implement GenericCommand."));
			return;
		}
		if ((command).isPrivate())
			commands.add(command);
		else
			noPrivateCommands.add(command);
	}

	public static class CommandListenerImpl implements CommandListener<GenericCommand>
	{
		private Map<GenericCommand, Integer> usage = new HashMap<>();

		@Override
		public void onCommand(GenericCommand command)
		{
			if(usage.containsKey(command))
				usage.put(command, usage.get(command) + 1);
			else
				usage.put(command, 1);
			LOG.log(SimpleLog.Level.INFO, "Used: " + command.getAlias() + " [" + usage.get(command) + "]");
		}

	}

}

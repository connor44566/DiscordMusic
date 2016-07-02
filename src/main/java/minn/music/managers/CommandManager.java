package minn.music.managers;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.hooks.CommandListener;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CommandManager
{
	private List<GenericCommand> noPrivateCommands = new LinkedList<>();
	private List<GenericCommand> commands = new LinkedList<>();
	private CommandListener listener = new CommandListenerImpl();
	private MusicBot bot;
	private final SimpleLog LOG = SimpleLog.getLog("CommandManager");

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
	}

	private void handleMessage(MessageReceivedEvent event)
	{
		if(!event.getMessage().getRawContent().startsWith(bot.config.prefix))
			return;
		final String com = event.getMessage().getRawContent().split("\\s+", 2)[0];
		for (GenericCommand c : commands)
		{
			if((bot.config.prefix + c.getAlias()).equalsIgnoreCase(com))
			{
				executor.submit(() -> c.invoke(new GenericCommand.CommandEvent(event)));
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
				return;
			}
		}
	}

	/**
	 * Used to override default {@link CommandListener CommandListener} implementation used by the manager.
	 *
	 * @param listener A CommandListener implementation.
	 */
	public void setCommandListener(CommandListener listener)
	{
		if (listener == null)
			LOG.log(new NullPointerException("Listener to override can not be null!"));
		else
			this.listener = listener;
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
	 * Used to register a new {@link GenericCommand Command}.
	 *
	 * @param command GenericCommand.
	 */
	public void registerCommand(Object command)
	{
		if (!(command instanceof GenericCommand))
		{
			LOG.log(new IllegalArgumentException("Command must implement GenericCommand."));
			return;
		}
		if (((GenericCommand) command).isPrivate())
			commands.add((GenericCommand) command);
		else
			noPrivateCommands.add((GenericCommand) command);
	}

	public static class CommandListenerImpl implements CommandListener
	{

		@Override
		public void onCommand(GenericCommand command)
		{
			// TODO: Add for logs.
		}

	}

}

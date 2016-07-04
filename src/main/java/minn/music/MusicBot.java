package minn.music;

import minn.music.managers.CommandManager;
import minn.music.settings.Config;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class MusicBot implements EventListener
{
	public static Config config;
	private final static SimpleLog LOG = SimpleLog.getLog("MusicBot");
	public final List<CommandManager> managers = new LinkedList<>();
	private Consumer<CommandManager> callback;

	public void onEvent(Event event)
	{
		if (event instanceof ReadyEvent)
		{
			CommandManager m = new CommandManager(event.getJDA(), this);
			managers.add(m);
			event.getJDA().removeEventListener(this);
			callback.accept(m);
		}
	}

	public MusicBot(Consumer<CommandManager> callback, int shards, Config cfg)
	{
		assert shards > 0 && callback != null;


		try
		{
			config = cfg;
			this.callback = callback;
			if(shards == 1)
			{
				new JDABuilder()
						.setAudioEnabled(true)
						.setAutoReconnect(true)
						.setBotToken(config.token)
						.setBulkDeleteSplittingEnabled(false)
						.addListener(this)
						.buildAsync();
				return;
			}
			for (int i = 0; i < shards; i++)
			{
				new JDABuilder()
						.setAudioEnabled(true)
						.setAutoReconnect(true)
						.setBotToken(config.token)
						.setBulkDeleteSplittingEnabled(false)
						.addListener(this)
						.useSharding(i, shards)
						.buildAsync();
			}
		} catch (Exception e)
		{
			LOG.log(e);
			System.exit(1);
		}
	}
}

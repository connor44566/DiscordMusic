package minn.music;

import minn.music.managers.CommandManager;
import minn.music.settings.Config;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.function.Consumer;

public class MusicBot implements EventListener
{
	public Config config;
	private final static SimpleLog LOG = SimpleLog.getLog("MusicBot");
	public  CommandManager manager;
	private Consumer<MusicBot> callback;

	public void onEvent(Event event)
	{
		if(event instanceof ReadyEvent)
		{
			manager = new CommandManager(event.getJDA(), this);
			event.getJDA().removeEventListener(this);
			callback.accept(this);
		}
	}

	public MusicBot(Consumer<MusicBot> callback)
	{
		try
		{
			this.callback = callback;
			config = new Config("Base.json", true);
			JDA api = new JDABuilder()
					.setAudioEnabled(true)
					.setAutoReconnect(true)
					.setBotToken(config.token)
					//.setBulkDeleteSplittingEnabled(false)
					.addListener(this)
					.buildAsync();

		} catch (Exception e)
		{
			LOG.log(e);
			System.exit(1);
		}
	}
}

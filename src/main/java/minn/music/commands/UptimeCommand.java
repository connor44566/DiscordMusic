package minn.music.commands;

import minn.music.util.TimeUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.ReconnectedEvent;
import net.dv8tion.jda.hooks.EventListener;

public class UptimeCommand extends GenericCommand
{

	public static long start;
	private long instantiated = start; // If shard -> shard restart overriding instance time

	public UptimeCommand(JDA api)
	{
		api.addEventListener((EventListener) event ->
		{
			if(event instanceof ReconnectedEvent)
				instantiated = System.currentTimeMillis();
		});
	}

	@Override
	public String getAlias()
	{
		return "uptime";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		event.send(TimeUtil.uptime(System.currentTimeMillis() - instantiated));
	}
}

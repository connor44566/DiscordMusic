package minn.music.commands;

import minn.music.util.TimeUtil;

public class UptimeCommand extends GenericCommand
{

	public static long start;

	@Override
	public String getAlias()
	{
		return "uptime";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		event.send(TimeUtil.uptime(System.currentTimeMillis() - start));
	}
}

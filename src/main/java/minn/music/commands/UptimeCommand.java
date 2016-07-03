package minn.music.commands;

import minn.music.util.TimeUtil;

import java.lang.management.ManagementFactory;

public class UptimeCommand extends GenericCommand
{
	@Override
	public String getAlias()
	{
		return "uptime";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		event.send(TimeUtil.uptime(ManagementFactory.getRuntimeMXBean().getUptime()));
	}
}

package minn.music.util;

import net.dv8tion.jda.entities.TextChannel;

import java.util.HashMap;
import java.util.LinkedList;

public class IgnoreUtil
{

	private static HashMap<String, LinkedList<String>> ignoredChannels = (HashMap<String, LinkedList<String>>) PersistenceUtil.retrieve("ignoredChannels");
	private static boolean init = false;

	public static void init()
	{
		if (init)
			return;
		init = true;
		if (ignoredChannels == null)
			ignoredChannels = new HashMap<>();
	}

	public static HashMap<String, LinkedList<String>> getIgnoredChannels()
	{
		return new HashMap<>(ignoredChannels);
	}

	public static void ignore(TextChannel... channels)
	{
		for (TextChannel c : channels)
		{
			if (ignoredChannels.containsKey(c.getGuild().getId()))
				ignoredChannels.get(c.getGuild().getId()).add(c.getId());
			else
			{
				LinkedList<String> ids = new LinkedList<>();
				ids.add(c.getId());
				ignoredChannels.put(c.getGuild().getId(), ids);
			}
		}
		PersistenceUtil.save(ignoredChannels, "ignoredChannels");

	}

	public static void unIgnore(TextChannel... channels)
	{
		for (TextChannel c : channels)
		{
			if (!ignoredChannels.containsKey(c.getGuild().getId()) || !ignoredChannels.get(c.getGuild().getId()).contains(c.getId()))
				continue;
			ignoredChannels.get(c.getGuild().getId()).remove(c.getId());
		}
		PersistenceUtil.save(ignoredChannels, "ignoredChannels");
	}

	public static boolean isIgnored(TextChannel c)
	{
		return c != null && ignoredChannels.containsKey(c.getGuild().getId()) && ignoredChannels.get(c.getGuild().getId()).contains(c.getId());
	}

}

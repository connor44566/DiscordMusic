/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.util;

import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.HashMap;
import java.util.LinkedList;

public class IgnoreUtil
{

	private static HashMap<String, LinkedList<String>> ignoredChannels = (HashMap<String, LinkedList<String>>) PersistenceUtil.retrieve("ignoredChannels");
	private static LinkedList<String> ignoredUsers = (LinkedList<String>) PersistenceUtil.retrieve("ignoredUsers");

	static
	{
		if (ignoredChannels == null)
			ignoredChannels = new HashMap<>();
		if (ignoredUsers == null)
			ignoredUsers = new LinkedList<>();
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

	public static void ignore(User... users)
	{
		for (User u : users)
		{
			if (ignoredUsers.contains(u.getId()))
				continue;
			ignoredUsers.add(u.getId());
		}
		PersistenceUtil.save(ignoredUsers, "ignoredUsers");
	}

	public static void unIgnore(User... users)
	{
		for (User u : users)
		{
			if (!ignoredUsers.contains(u.getId()))
				continue;
			ignoredUsers.remove(u.getId());
		}
		PersistenceUtil.save(ignoredUsers, "ignoredUsers");
	}

	public static boolean isIgnored(TextChannel c)
	{
		return c != null && ignoredChannels.containsKey(c.getGuild().getId()) && ignoredChannels.get(c.getGuild().getId()).contains(c.getId());
	}

	public static boolean isIgnored(User user)
	{
		return ignoredUsers.contains(user.getId());
	}

}

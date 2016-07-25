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

package minn.music.commands;

import minn.music.MusicBot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DiscrimCommand extends GenericCommand
{
	private static long last_check = 0;
	private MusicBot bot;

	public DiscrimCommand(MusicBot bot)
	{
		this.bot = bot;
	}

	public String getAttributes()
	{
		return "[int]";
	}

	public String getInfo()
	{
		return "Used to retrieve all Users this bot can see that fit to given discriminator.";
	}

	@Override
	public String getAlias()
	{
		return "discr";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (last_check + TimeUnit.MINUTES.toMillis(2) > System.currentTimeMillis() && !event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("Cooldown: " + TimeUnit.MILLISECONDS.toSeconds((last_check + TimeUnit.MINUTES.toMillis(2)) - System.currentTimeMillis()) + " Seconds");
			return;
		}
		List<String> userList;
		String disc = event.author.getDiscriminator();

		if (event.allArgs.matches("#?\\d{1,4}"))
			disc = adjust(event.allArgs.replace("#", ""));
		else if (!event.allArgs.isEmpty())
		{
			event.send("Invalid discriminator.");
			return;
		}
		userList = getUsersWithDiscriminator(disc);
		if (userList.isEmpty())
		{
			event.send("No users found matching **" + disc + "**.");
			return;
		}
		event.send("Matching users for **" + disc + "**```" + userList + "```");
		last_check = System.currentTimeMillis();
	}

	public synchronized List<String> getUsersWithDiscriminator(String disc)
	{
		List<String> userlist = new ArrayList<>();
		bot.getShards().forEach((i, api) ->
				api.getUsers().parallelStream().filter(u -> u.getDiscriminator().equals(disc) && !userlist.contains(u.getUsername())).forEach(u -> userlist.add(u.getUsername())));
		return userlist;
	}

	public static String adjust(String disc)
	{
		assert disc.length() < 5;
		while (disc.length() < 4)
			disc = "0" + disc;
		return disc;
	}
}

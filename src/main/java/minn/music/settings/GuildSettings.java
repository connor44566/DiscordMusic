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

package minn.music.settings;

import minn.music.util.PersistenceUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.HashMap;
import java.util.Map;

public class GuildSettings
{

	private static HashMap<String, Integer> welcomeLevels;
	private static HashMap<String, String> modLogs;
	private static HashMap<String, String> messages;
	private static Map<String, GuildSettings> settingsMap = new HashMap<>();

	static
	{
		try
		{
			welcomeLevels = (HashMap<String, Integer>) PersistenceUtil.retrieve("welcomeLevels");
			if (welcomeLevels == null) welcomeLevels = new HashMap<>();
			modLogs = (HashMap<String, String>) PersistenceUtil.retrieve("modLogs");
			if (modLogs == null) modLogs = new HashMap<>();
			messages = (HashMap<String, String>) PersistenceUtil.retrieve("welcomeMessages");
			if (messages == null) messages = new HashMap<>();
			SimpleLog.getLog("GuildSettings").info("Ready!");
		} catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static void save()
	{
		PersistenceUtil.save(welcomeLevels, "welcomeLevels");
		PersistenceUtil.save(modLogs, "modLogs");
		PersistenceUtil.save(messages, "welcomeMessages");
	}

	public static GuildSettings get(Guild guild)
	{
		return get(guild.getId());
	}

	private static GuildSettings get(String id)
	{
		if (settingsMap.containsKey(id))
			return settingsMap.get(id);
		GuildSettings settings = new GuildSettings(id);
		settingsMap.put(id, settings);
		return settings;
	}

	public static boolean has(Guild guild)
	{
		return settingsMap.containsKey(guild.getId());
	}

	private WelcomeLevel welcomeLevel;
	private String modLog;
	private final String id;

	private GuildSettings(Guild guild)
	{
		this(guild.getId());
	}

	private GuildSettings(String id)
	{
		this.id = "" + id;
		this.welcomeLevel = (welcomeLevels.containsKey(this.id) ? getLevelFor(welcomeLevels.get(this.id)) : WelcomeLevel.UNSET);
		this.modLog = (modLogs.containsKey(this.id) ? modLogs.get(this.id) : null);
	}

	public static WelcomeLevel getLevelFor(int i)
	{
		switch (i)
		{
			default:
				throw new IllegalArgumentException("Given integer fits to no WelcomeLevel.");
			case -1:
				return WelcomeLevel.UNSET;
			case 0:
				return WelcomeLevel.NORMAL;
			case 1:
				return WelcomeLevel.MENTION;
			case 2:
				return WelcomeLevel.DIRECT;
		}
	}

	public boolean isModLog(TextChannel channel)
	{
		return channel != null && channel.getId().equals(modLog);
	}

	public String getModLog()
	{
		return modLog;
	}

	public WelcomeLevel getWelcomeLevel()
	{
		return welcomeLevel;
	}

	public String getWelcomeMessage()
	{
		return messages.containsKey(id) ? "" + messages.get(id) : null;
	}

	public String getWelcomeMessage(GuildMemberJoinEvent event)
	{
		return getWelcomeMessage()
				.replace("{userid}", event.getUser().getId())
				.replace("{server}", event.getGuild().getName())
				.replace("{mention}", event.getUser().getAsMention())
				.replace("@everyone", "@\u0001everyone")
				.replace("@here", "@\u0001here");
	}

	public void setModLog(TextChannel channel)
	{
		this.modLog = (channel != null ? channel.getId() : null);
		if (modLog == null)
			modLogs.remove(id);
		else
			modLogs.put(id, modLog);
		PersistenceUtil.save(modLogs, "modLogs");
	}

	public void setWelcomeLevel(WelcomeLevel level)
	{
		if (level == null)
			level = WelcomeLevel.UNSET;
		this.welcomeLevel = level;
		welcomeLevels.put(id, welcomeLevel.value);
		PersistenceUtil.save(welcomeLevels, "welcomeLevels");
	}

	public void setWelcomeMessage(String message) throws IllegalArgumentException
	{
		if (message == null)
			message = "";
		assert !message.toLowerCase().contains("@everyone") && !message.toLowerCase().contains("@here") && !message.matches(".*<@!?\\d{16,}>.*") : "Message is not allowed, please alter input.";
		if (message.length() > 250)
			throw new IllegalArgumentException("Welcome message is longer than 250 characters. Please shorten.");
		if (messages.containsKey(id) && message.isEmpty())
			messages.remove(id);
		else
			messages.put(id, message);
		PersistenceUtil.save(messages, "welcomeMessages");
	}

	public enum WelcomeLevel
	{

		UNSET(-1), NORMAL(0), MENTION(1), DIRECT(2);

		public final int value;

		WelcomeLevel(int value)
		{
			this.value = value;
		}
	}

}

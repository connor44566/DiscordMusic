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

package minn.music.managers;

import minn.music.MusicBot;
import minn.music.util.PersistenceUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;

import java.util.HashMap;

/**
 * Static PrefixManager (Used for custom Guild-Prefix)
 */
public class PrefixManager
{

	private static HashMap<String, String> prefixMap;

	static
	{
		prefixMap = (HashMap<String, String>) PersistenceUtil.retrieve("prefixMap");
		if (prefixMap == null)
			prefixMap = new HashMap<>();
	}

	public static HashMap<String, String> getPrefixMap()
	{
		return new HashMap<>(prefixMap);
	}

	public static String getPrefix(Guild guild)
	{
		return guild != null ? getPrefix(guild.getId()) : MusicBot.config.prefix;
	}

	public static String getPrefix(String guildID)
	{
		return prefixMap.containsKey(guildID) ? prefixMap.get(guildID) : MusicBot.config.prefix;
	}

	public static boolean setCustom(Guild guild, String fix)
	{
		prefixMap.put(guild.getId(), fix);
		save();
		return true;
	}

	public static boolean removeCustom(Guild guild)
	{
		if (prefixMap.containsKey(guild.getId()))
			prefixMap.remove(guild.getId());
		save();
		return true;
	}

	/**
	 * Convenience
	 *
	 * @param message
	 * @return
	 */
	public static boolean isCustom(String message, Guild guild)
	{
		return message.startsWith(getPrefix(guild));
	}

	/**
	 * Convenience for convenience
	 *
	 * @param message
	 * @return
	 */
	public static boolean isCustom(Message message, Guild guild)
	{
		return guild != null && message != null && isCustom(message.getRawContent(), guild);
	}

	private static void save()
	{
		new Thread(() -> PersistenceUtil.save(getPrefixMap(), "prefixMap"), "Async Prefix Saver").start();
	}

}

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
	private static boolean init = false;

	public static void init()
	{
		if (init)
			return;
		prefixMap = (HashMap<String, String>) PersistenceUtil.retrieve("prefixMap");
		if (prefixMap == null)
			prefixMap = new HashMap<>();
		init = true;
	}

	public static HashMap<String, String> getPrefixMap()
	{
		assert init : "PrefixManager.init() must be called before any other methods.";
		return new HashMap<>(prefixMap);
	}

	public static String getPrefix(Guild guild)
	{
		assert init : "PrefixManager.init() must be called before any other methods.";
		return guild != null ? getPrefix(guild.getId()) : MusicBot.config.prefix;
	}

	public static String getPrefix(String guildID)
	{
		assert init : "PrefixManager.init() must be called before any other methods.";
		return prefixMap.containsKey(guildID) ? prefixMap.get(guildID) : MusicBot.config.prefix;
	}

	public static boolean setCustom(Guild guild, String fix)
	{
		assert init : "PrefixManager.init() must be called before any other methods.";
		prefixMap.put(guild.getId(), fix);
		save();
		return true;
	}

	public static boolean removeCustom(Guild guild)
	{
		assert init : "PrefixManager.init() must be called before any other methods.";
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
		assert init : "PrefixManager.init() must be called before any other methods.";
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
		assert init : "PrefixManager.init() must be called before any other methods.";
		return guild != null && message != null && isCustom(message.getRawContent(), guild);
	}

	private static void save()
	{
		new Thread(() -> PersistenceUtil.save(getPrefixMap(), "prefixMap"), "Async Prefix Saver").start();
	}

}

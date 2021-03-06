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

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.utils.AvatarUtil;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntityUtil
{

	// Patterns
	public static final String ID = "\\d{16,}";
	public static final String USER_MENTION = "<!?@(" + ID + ")>";
	public static final String CHANNEL_MENTION = "<#(" + ID + ")>";
	public static final String USER_TAG = "\\S.{0,30}\\S#\\d{4}";

	/**
	 * Replaces mentions (<@!?(\d{16,}>) with {@link EntityUtil#transform(net.dv8tion.jda.entities.User)}.
	 *
	 * @param message Message to trim.
	 * @param api     JDA instance.
	 * @return Stripped message.
	 */
	public static String stripMentions(String message, JDA api)
	{
		Matcher matcher = Pattern.compile(USER_MENTION).matcher(message);
		String replacement = message;
		while (matcher.find())
		{
			String id = matcher.group().replaceAll(USER_MENTION, "$1");
			replacement = matcher.replaceFirst(EntityUtil.transform(api.getUserById(id)));
			matcher.reset(replacement);
		}
		return replacement;
	}

	/**
	 * Return whether the you were mentioned or not.
	 *
	 * @param m Message
	 * @return True if you were mentioned, false otherwise.
	 */
	public static boolean mentionsMe(Message m)
	{
		return m != null && m.getMentionedUsers().contains(m.getJDA().getSelfInfo());
	}

	/**
	 * Return whether the you were mentioned or not.
	 *
	 * @param s   Message String
	 * @param api JDA implementation.
	 * @return True if you were mentioned, false otherwise.
	 */
	public static boolean mentionsMe(String s, JDA api)
	{
		assert api != null;
		return !(s == null || s.isEmpty()) && s.matches(".*(<@!?" + api.getSelfInfo().getId() + ">).*");
	}

	/**
	 * Can be used to get {@link net.dv8tion.jda.entities.User User} as Username#Discriminator tag.
	 *
	 * @param user NotNull user instance.
	 * @return <i>Minn#6688</i> i.e.
	 */
	public static String transform(User user)
	{
		return user == null ? "unknown" : user.getUsername() + "#" + user.getDiscriminator();
	}

	/**
	 * Resolves {@link net.dv8tion.jda.entities.User User} from given {@link String}.
	 * <h3>Parsable Strings</h3>
	 * <ul>
	 * <li>Minn#6688</li>
	 * <li>86699011792191488</li>
	 * <li><@!86699011792191488></li>
	 * <li><@86699011792191488></li>
	 * </ul>
	 *
	 * @param s   String to parse
	 * @param api {@link net.dv8tion.jda.JDA JDA} instance to get User instance from.
	 * @return A User instance fitting to the unique parsed <i>s</i>. Or null if no user fits.
	 */
	public static User resolveUser(String s, JDA api)
	{
		if (s.isEmpty())
			return null;
		if (isID(s))
			return api.getUserById(s);
		if (isMention(s))
			return api.getUserById(s.replaceAll(USER_MENTION, "$1"));
		return getUserByNameDiscriminator(s, api);
	}

	/**
	 * Resolves {@link net.dv8tion.jda.entities.TextChannel TextChannel} from given {@link String}.
	 * <h3>Parsable Strings</h3>
	 * <ul>
	 * <li><#191249209553321985></li>
	 * <li>191249209553321985</li>
	 * </ul>
	 *
	 * @param s   String to parse
	 * @param api {@link net.dv8tion.jda.JDA JDA} instance to get Channel instance from.
	 * @return A Channel instance fitting to the unique parsed <i>s</i>. Or null if no channel fits.
	 */
	public static TextChannel resolveTextChannel(String s, JDA api)
	{
		if (s == null || s.isEmpty())
			return null;
		if (isID(s))
			return api.getTextChannelById(s);
		return (isChannelMention(s) ? api.getTextChannelById(s.replaceAll(CHANNEL_MENTION, "$1")) : getFirstText(s, api));
	}

	/**
	 * Resolves {@link net.dv8tion.jda.entities.TextChannel TextChannel} from given {@link String}.
	 * <h3>Parsable Strings</h3>
	 * <ul>
	 * <li><#191249209553321985></li>
	 * <li>191249209553321985</li>
	 * </ul>
	 *
	 * @param s     String to parse
	 * @param guild {@link net.dv8tion.jda.JDA JDA} instance to get Channel instance from.
	 * @return A Channel instance fitting to the unique parsed <i>s</i>. Or null if no channel fits.
	 */
	public static TextChannel resolveTextChannel(String s, Guild guild)
	{
		if (s == null || s.isEmpty())
			return null;
		if (isID(s))
			return guild.getTextChannels().parallelStream().filter(c -> c.getId().equals(s)).findFirst().orElse(null);
		return (isChannelMention(s) ? guild.getTextChannels().parallelStream().filter(c -> c.getId().equals(s.replaceAll(CHANNEL_MENTION, "$1"))).findFirst().orElse(null) : getFirstText(s, guild));
	}

	/**
	 * Used to get {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} from a {@link net.dv8tion.jda.entities.User User} instance.
	 *
	 * @param user To get Avatar from.
	 * @return {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} of User if available or null otherwise.
	 * @throws UnirestException
	 */
	public static AvatarUtil.Avatar getAvatarFromUser(User user) throws UnirestException
	{
		String url = user.getAvatarUrl();
		if (url == null || url.isEmpty())
			return null;
		InputStream in = Unirest.get(url).asBinary().getBody();
		return AvatarUtil.getAvatar(in);
	}

	/**
	 * Resolves User from a combo matching <i>YYYY+#9999</i>.
	 *
	 * @param combo (e.g. Minn#6688)
	 * @param api   A {@link JDA JDA} instance.
	 * @return User
	 */
	public static User getUserByNameDiscriminator(String combo, JDA api)
	{
		if (isValidTag(combo))
		{
			String[] split = split(combo);
			return getUserByNameDiscriminator(split[0], split[1], api);
		}
		return null;
	}

	/**
	 * Resolves User from a combo matching <i>YYYY+#9999</i>.
	 *
	 * @param name          (e.g. Minn)
	 * @param discriminator (e.g. 9999)
	 * @param api           A {@link JDA JDA} instance.
	 * @return User
	 */
	public static User getUserByNameDiscriminator(String name, String discriminator, JDA api)
	{
		return new LinkedList<>(api.getUsers())
				.parallelStream()
				.filter
						(
								u -> u.getUsername().equals(name) && u.getDiscriminator().equals(discriminator)
						)
				.findFirst()
				.orElse(null);
	}

	/**
	 * Check whether given String is a valid tag. (e.g. Minn#9988 would return true)
	 *
	 * @param tag
	 * @return boolean
	 */
	public static boolean isValidTag(String tag)
	{
		return tag.matches(USER_TAG);
	}

	/**
	 * Check whether given String is a valid mention. (e.g. <@!86699011792191488> would return true)
	 *
	 * @param mention
	 * @return boolean
	 */
	public static boolean isMention(String mention)
	{
		return mention.matches(USER_MENTION);
	}

	/**
	 * Check whether given String is a valid channel mention. (e.g. <#86699011792191488> would return true)
	 *
	 * @param mention
	 * @return boolean
	 */
	public static boolean isChannelMention(String mention)
	{
		return mention.matches(CHANNEL_MENTION);
	}

	/**
	 * Checks whether the given String is a digit with more than 16 numbers.
	 *
	 * @param id String to check.
	 * @return True if it is a valid id, false otherwise.
	 */
	public static boolean isID(String id)
	{
		return id.matches(ID);
	}


	/**
	 * Retrieves the VoiceChannel for given User of given Guild instance.
	 *
	 * @param user  User instance
	 * @param guild Guild instance
	 * @return VoiceChannel of user or null.
	 */
	public static VoiceChannel getVoiceForUser(User user, Guild guild)
	{
		assert guild != null && user != null;
		return guild.getVoiceStatusOfUser(user).getChannel();
	}

	/**
	 * Convenience function to get a matching {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} with the shortest matching name.
	 *
	 * @param match Match to look for.
	 * @param api   JDA instance to parse channels of.
	 * @return VoiceChannel matching given parameter. Or null if none was found.
	 */
	public static VoiceChannel getFirstVoice(String match, JDA api)
	{
		String lowerCase = match.toLowerCase();
		return api.getVoiceChannels().parallelStream().filter(c -> c.getName().toLowerCase().contains(lowerCase)).sorted((o1, o2) ->
		{
			if (o1.getName().length() < o2.getName().length())
				return -1;
			if (o1.getName().length() > o2.getName().length())
				return 1;
			return 0;
		}).findFirst().orElse(null);
	}

	/**
	 * Convenience function to get a matching {@link net.dv8tion.jda.entities.TextChannel TextChannel} with the shortest matching name.
	 *
	 * @param match Match to look for.
	 * @param api   JDA instance to parse channels of.
	 * @return TextChannel matching given parameter. Or null if none was found.
	 */
	public static TextChannel getFirstText(String match, JDA api)
	{
		String lowerCase = match.toLowerCase();
		return api.getTextChannels().parallelStream().filter(c -> c.getName().toLowerCase().contains(lowerCase)).sorted((o1, o2) ->
		{
			if (o1.getName().length() < o2.getName().length())
				return -1;
			if (o1.getName().length() > o2.getName().length())
				return 1;
			return 0;
		}).findFirst().orElse(null);
	}

	/**
	 * Convenience function to get a matching {@link net.dv8tion.jda.entities.VoiceChannel VoiceChannel} with the shortest matching name.
	 *
	 * @param match Match to look for.
	 * @param guild Guild instance to parse channels of.
	 * @return VoiceChannel matching given parameter. Or null if none was found.
	 */
	public static VoiceChannel getFirstVoice(String match, Guild guild)
	{
		String lowerCase = match.toLowerCase();
		return guild.getVoiceChannels().parallelStream().filter(c -> c.getName().toLowerCase().contains(lowerCase)).sorted((o1, o2) ->
		{
			if (o1.getName().length() < o2.getName().length())
				return -1;
			if (o1.getName().length() > o2.getName().length())
				return 1;
			return 0;
		}).findFirst().orElse(null);
	}

	/**
	 * Convenience function to get a matching {@link net.dv8tion.jda.entities.TextChannel TextChannel} with the shortest matching name.
	 *
	 * @param match Match to look for.
	 * @param guild Guild instance to parse channels of.
	 * @return TextChannel matching given parameter. Or null if none was found.
	 */
	public static TextChannel getFirstText(String match, Guild guild)
	{
		String lowerCase = match.toLowerCase();
		return guild.getTextChannels().parallelStream().filter(c -> c.getName().toLowerCase().contains(lowerCase)).sorted((o1, o2) ->
		{
			if (o1.getName().length() < o2.getName().length())
				return -1;
			if (o1.getName().length() > o2.getName().length())
				return 1;
			return 0;
		}).findFirst().orElse(null);
	}

	/**
	 * Convenience function to retrieve {@link Guild Guild} with the shortest matching guild contained in given shard.
	 *
	 * @param match String to look for in Guild names. (Ignores casing)
	 * @param api   JDA instance(shard)
	 * @return Guild with shortest matching name or null.
	 */
	public static Guild getFirstGuild(String match, JDA api)
	{
		String lowerCase = match.toLowerCase();
		return api.getGuilds().parallelStream().filter(g -> g.getName().toLowerCase().contains(lowerCase)).sorted((o1, o2) ->
		{
			if (o1.getName().length() < o2.getName().length())
				return -1;
			if (o1.getName().length() > o2.getName().length())
				return 1;
			return 0;
		}).findFirst().orElse(null);
	}

	private static String[] split(String combo)
	{
		int index = combo.lastIndexOf("#");
		return new String[]{combo.substring(0, index), combo.substring(index + 1)};
	}

}

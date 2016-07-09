package minn.music.util;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.AvatarUtil;

import java.io.InputStream;
import java.util.LinkedList;

public class EntityUtil
{

	/**
	 * Can be used to get {@link net.dv8tion.jda.entities.User User} as Username#Discriminator tag.
	 * @param user NotNull user instance.
	 * @return <i>Minn#6688</i> i.e.
	 */
	public static String transform(User user)
	{
		assert user != null;
		return user.getUsername() + "#" + user.getDiscriminator();
	}

	/**
	 * Resolves {@link net.dv8tion.jda.entities.User User} from given {@link String}.
	 * <h3>Parsable Strings</h3>
	 * <ul>
	 *     <li>Minn#6688</li>
	 *     <li><@!86699011792191488></li>
	 *     <li><@86699011792191488></li>
	 * </ul>
	 * @param s String to parse
	 * @param api {@link net.dv8tion.jda.JDA JDA} instance to get User instance from.
	 * @return A User instance fitting to the unique parsed <i>s</i>. Or null if no user fits.
	 */
	public static User resolveUser(String s, JDA api)
	{
		if(isMention(s))
			return api.getUserById(s.replaceAll("^<@!?(\\d{16,})>$","$1"));
		return getUserByNameDiscriminator(s, api);
	}

	/**
	 * Used to get {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} from a {@link net.dv8tion.jda.entities.User User} instance.
	 * @param user To get Avatar from.
	 * @return {@link net.dv8tion.jda.utils.AvatarUtil.Avatar Avatar} of User if available or null otherwise.
	 * @throws UnirestException
	 */
	public static AvatarUtil.Avatar getAvatarFromUser(User user) throws UnirestException
	{
		String url = user.getAvatarUrl();
		if(url == null || url.isEmpty())
			return null;
		InputStream in = Unirest.get(url).asBinary().getBody();
		return AvatarUtil.getAvatar(in);
	}

	/**
	 * Resolves User from a combo matching <i>YYYY+#9999</i>.
	 * @param combo (e.g. Minn#6688)
	 * @param api A {@link JDA JDA} instance.
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
	 * @param name (e.g. Minn)
	 * @param discriminator (e.g. 9999)
	 * @param api A {@link JDA JDA} instance.
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
	 * @param tag
	 * @return boolean
	 */
	public static boolean isValidTag(String tag)
	{
		return tag.matches("^\\S.{0,30}\\S#\\d{4}$");
	}

	/**
	 * Check whether given String is a valid mention. (e.g. <@!86699011792191488> would return true)
	 * @param mention
	 * @return boolean
	 */
	public static boolean isMention(String mention)
	{
		return mention.matches("^<@!?\\d{16,}>$");
	}

	private static String[] split(String combo)
	{
		int index = combo.lastIndexOf("#");
		return new String[]{combo.substring(0, index), combo.substring(index + 1)};
	}

}

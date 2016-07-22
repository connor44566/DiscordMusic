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

import minn.music.util.EntityUtil;
import minn.music.util.PersistenceUtil;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserProfile
{

	static
	{
		timer = new Timer();
		objects = new HashMap<>();
		initObjects();
	}

	private static void initObjects()
	{
		HashMap<String, String> retrieved = (HashMap<String, String>) PersistenceUtil.retrieve("userProfiles");
		if (retrieved != null)
		{
			retrieved.forEach((id, obj) ->
			{
				if (!obj.isEmpty() && !id.isEmpty())
					objects.put(id, new JSONObject(obj));
			});
		}
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				saveObjects();
			}
		}, TimeUnit.MINUTES.toMillis(2), TimeUnit.MINUTES.toMillis(2));
	}

	private static void saveObjects()
	{
		HashMap<String, String> toSave = new HashMap<>();
		new HashMap<>(objects).forEach((id, object) ->
				toSave.put(id, object.toString()));
		PersistenceUtil.save(toSave, "userProfiles");
	}

	private static final Timer timer;
	protected static final HashMap<String, JSONObject> objects;
	public final JSONObject object;
	protected User user;

	public UserProfile(User user)
	{
		this.user = user;
		if (!objects.containsKey(user.getId()))
			objects.put(user.getId(), new JSONObject());
		object = objects.get(user.getId());
	}

	public void setKey(Setting key, String toSet)
	{
		if (toSet.length() > key.length)
			throw new IndexOutOfBoundsException("Maximum length for that setting is: **" + key.length + "**.");
		else if (toSet.length() < 1)
			throw new IllegalArgumentException("No content given.");
		object.put(key.name(), toSet);
	}

	public void removeKey(Setting key)
	{
		if (object.has(key.name()))
			object.remove(key.name());
	}

	public synchronized String getProfilePage()
	{
		if (!object.keys().hasNext())
			return "No settings have been set, yet.";
		String s = "\uD83D\uDCDC __Profile for " + EntityUtil.transform(user).replace("_", "\\_") + "__ \uD83D\uDCDC\n";
		for (Iterator<String> it = new JSONObject(object.toString()).keys(); it.hasNext(); )
		{
			String key = it.next();
			try
			{
				s += "**" + Setting.valueOf(key).caption + "**: " + (Setting.valueOf(key).isEmbed ? object.getString(key) : object.getString(key).replaceAll("(https?://\\S+)", "<$1>")) + "\n";
			} catch (IllegalArgumentException e)
			{
				SimpleLog.getLog("Profiles").info("Setting has been removed due to failures.");
				object.remove(key);
			}
		}
		if (!object.keys().hasNext())
			return "No settings have been set, yet.";
		return s.trim().replace("@", "@\u0001");
	}


	public enum Setting
	{

		STEAM("\uD83C\uDFAE Steam", 32),
		TWITTER("\uD83D\uDC26 Twitter", 20),
		ABOUT_ME("\uD83D\uDCAD About Me", 400),
		TWITCH("\uD83D\uDC7E Twitch", 100),
		YOUTUBE("\uD83C\uDFAC Youtube", 100),
		WEBSITE("\uD83D\uDDA5 Website", 100),
		EMAIL("\uD83D\uDCE7 Email", 100),
		DISCORD("\uD83D\uDD17 Discord", 50, true);

		private String caption;
		private int length;
		private boolean isEmbed = false;

		Setting(String key, int length, boolean isEmbed)
		{
			this(key, length);
			this.isEmbed = isEmbed;
		}

		Setting(String key, int length)
		{
			this.caption = key;
			this.length = length;
		}

		public static String getSettings()
		{
			return Stream.of(Setting.values()).map(Enum::name).collect(Collectors.toList()).toString();
		}

	}

}

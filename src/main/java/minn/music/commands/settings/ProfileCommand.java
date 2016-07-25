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

package minn.music.commands.settings;

import minn.music.commands.GenericCommand;
import minn.music.settings.UserProfile;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.entities.User;

public class ProfileCommand extends GenericCommand
{

	@Override
	public void invoke(CommandEvent event)
	{
		if (event.allArgs.isEmpty())
		{
			event.send(new UserProfile(event.author).getProfilePage());
			return;
		}
		if (event.args[0].equalsIgnoreCase("set"))
		{
			try
			{
				UserProfile.Setting setting = UserProfile.Setting.valueOf(event.args[1].toUpperCase());
				if (event.args.length < 3)
				{
					new UserProfile(event.author).removeKey(setting);
					event.send("Removed key **" + setting.name() + "**!");
				} else
				{
					new UserProfile(event.author).setKey(setting, event.allArgs.split("\\s+", 3)[2]);
					event.send("Updated profile.");
				}
			} catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e)
			{
				event.send("Invalid property/input.");
			} catch (Exception e)
			{
				event.send(e.getMessage());
			}
		} else
		{
			User target = EntityUtil.resolveUser(event.allArgs, event.api);
			if (target == null)
				event.send("I don't know who `" + event.allArgs + "` is :pensive:.");
			else
				event.send(new UserProfile(target).getProfilePage());
		}
	}

	public String getInfo()
	{
		return "Used to edit the profile of a user.\nSettings: `" + UserProfile.Setting.getSettings() + "`\nExample 1: profile set TWITTER minn_in\nExample 2: profile Minn#6688";
	}

	public String getAttributes()
	{
		return "[set <property> [input]] | [<mention>]";
	}

	public String getAlias()
	{
		return "profile";
	}

}

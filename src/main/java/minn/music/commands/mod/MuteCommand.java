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

package minn.music.commands.mod;

import minn.music.commands.GenericCommand;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.PermissionOverride;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;

public class MuteCommand extends GenericCommand
{

	public boolean isPrivate()
	{
		return false;
	}

	public String getAttributes()
	{
		return "<mention>";
	}

	@Override
	public String getAlias()
	{
		return "mute";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		User user = EntityUtil.resolveUser(event.allArgs, event.api);
		if (user == null)
		{
			event.send("You have to mention someone to mute them.");
			return;
		}
		if (!event.guild.getUsers().contains(user))
		{
			event.send("User is not in this server.");
			return;
		}
		if (!PermissionUtil.checkPermission(event.author, Permission.MANAGE_PERMISSIONS, (TextChannel) event.channel))
		{
			event.send("You are not allowed to manage this channel.");
			return;
		}
		if (!PermissionUtil.checkPermission(event.api.getSelfInfo(), Permission.MANAGE_PERMISSIONS, (TextChannel) event.channel))
		{
			event.send("I need permission to manage channel permissions.");
			return;
		}
		PermissionOverride override = ((TextChannel) event.channel).getOverrideForUser(user);
		(override != null ? override.getManager() : ((TextChannel) event.channel).createPermissionOverride(user)).deny(Permission.MESSAGE_WRITE).update();
		event.send("*Muted " + EntityUtil.transform(user) + "*\nTo un-mute a user go to channel's permissions and grant the user to send messages in the channel.");
	}
}

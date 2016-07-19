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

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.managers.PrefixManager;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.utils.PermissionUtil;

public class PrefixCommand extends GenericCommand
{

	public String getAttributes()
	{
		return "<prefix>";
	}

	public String getInfo()
	{
		return "\nUsed to set a custom prefix for current server/guild.\n*Can only be invoked by users with **MANAGE_SERVER**.*\nThis will **not** override the default prefix!\n\nPro Tip: Setting an empty fix will remove it.";
	}

	@Override
	public String getAlias()
	{
		return "prefix";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!PermissionUtil.checkPermission(event.author, Permission.MANAGE_SERVER, event.guild) && !event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are unable to set a custom prefix. (Missing **MANAGE_SERVER** permission)");
			return;
		}
		if (event.allArgs.isEmpty())
		{
			PrefixManager.removeCustom(event.guild);
			event.send("Custom prefix has been erased.");
			return;
		}
		event.send((PrefixManager.setCustom(event.guild, event.args[0]) ? String.format("Prefix has been changed to **%s**.", event.args[0]) : "Prefix is not allowed."));
	}
}

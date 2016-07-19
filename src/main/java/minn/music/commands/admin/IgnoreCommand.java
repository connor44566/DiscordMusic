/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.commands.admin;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.util.EntityUtil;
import minn.music.util.IgnoreUtil;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

public class IgnoreCommand extends GenericCommand
{

	public String getInfo()
	{
		return "Gives owner ability to ignore specific channels.";
	}

	public String getAttributes()
	{
		return "<+/-> <type> <channel>";
	}

	@Override
	public String getAlias()
	{
		return "ignore";
	}

	@Override
	public void invoke(CommandEvent event) // TODO: Testing
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("Not happening.");
			return;
		}
		if (event.args.length < 3)
		{
			event.send("Usage: " + getAlias() + " " + getAttributes());
			return;
		}
		String method = event.args[0];
		if (!method.equals("+") && !method.equals("-"))
		{
			event.send("Invalid method. Try +/-");
			return;
		}
		if (event.args[1].equalsIgnoreCase("channel"))
			ignoreChannel(method.equals("+"), event);
		else if (event.args[1].equalsIgnoreCase("user"))
			ignoreUser(method.equals("+"), event);
		else
			event.send("Type not supported.");
	}

	private void ignoreUser(boolean ignore, CommandEvent event)
	{
		String indicator = event.allArgs.split("\\s+", 3)[2];
		User target = EntityUtil.resolveUser(indicator, event.api);
		if (target == null)
		{
			event.send("No matching user");
		} else
		{
			if (ignore)
			{
				IgnoreUtil.ignore(target);
				event.send("Now ignoring **" + target.getAsMention() + "**.");
			} else
			{
				IgnoreUtil.unIgnore(target);
				event.send("Stopped ignoring **" + target.getAsMention() + "**.");
			}
		}
	}

	private void ignoreChannel(boolean ignore, CommandEvent event)
	{
		String indicator = event.allArgs.split("\\s+", 3)[2];
		TextChannel target = EntityUtil.resolveTextChannel(indicator, event.api);
		if (target == null)
		{
			if (indicator.equalsIgnoreCase("all"))
			{
				if (ignore)
				{
					IgnoreUtil.ignore((TextChannel[]) event.guild.getTextChannels().toArray());
					event.send("Stopped listening to this guild.");
				} else
				{
					IgnoreUtil.unIgnore((TextChannel[]) event.guild.getTextChannels().toArray());
					event.send("Listening to entire guild now.");
				}
				return;
			}
			target = EntityUtil.getFirstText(event.allArgs, event.guild);
			if (target != null)
			{
				if (ignore)
				{
					IgnoreUtil.ignore(target);
					event.send("Now ignoring **" + target.getName() + "**.");
				} else
				{
					IgnoreUtil.unIgnore(target);
					event.send("Stopped ignoring **" + target.getName() + "**.");
				}
				return;
			}
			event.send("No matching channel found.");
		} else
		{
			if (ignore)
			{
				IgnoreUtil.ignore(target);
				event.send("Now ignoring **" + target.getName() + "**.");
			} else
			{
				IgnoreUtil.unIgnore(target);
				event.send("Stopped ignoring **" + target.getName() + "**.");
			}
		}
	}
}

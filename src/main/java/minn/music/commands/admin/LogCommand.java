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

package minn.music.commands.admin;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.hooks.Logger;

import java.io.File;

public class LogCommand extends GenericCommand
{

	public String getAttributes()
	{
		return "[1-500]";
	}

	@Override
	public String getAlias()
	{
		return "log";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You can't use this command.");
			return;
		}

		int amount = 100;
		if (!event.allArgs.isEmpty())
			try
			{
				amount = Integer.parseInt(event.args[0]);
			} catch (NumberFormatException ignored)
			{
			}
		File f = Logger.log(amount);
		if (f == null)
			event.send("\uD83D\uDCA2");
		else
			event.send("✅ `" + f.toString() + "`");
	}
}

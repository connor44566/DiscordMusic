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
import minn.music.managers.CommandManager;

import java.util.Map;

public class DetermineUsage extends GenericCommand
{
	@Override
	public String getAlias()
	{
		return "usage";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("Sorry fam, this ain't happening.");
			return;
		}
		Map<String, Integer> use = CommandManager.getUsage();
		StringBuilder b = new StringBuilder("Usage: ```xl\n");
		use.forEach((c, i) ->
				b.append(c).append(" - ").append(i).append("\n"));
		event.send(b.append("```").toString());
	}
}

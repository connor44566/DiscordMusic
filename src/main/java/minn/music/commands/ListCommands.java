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

package minn.music.commands;

import minn.music.MusicBot;
import minn.music.managers.CommandManager;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommands extends GenericCommand
{

	private MusicBot bot;

	public ListCommands(MusicBot bot)
	{
		this.bot = bot;
	}

	public String getAttributes()
	{
		return "[command]";
	}

	@Override
	public String getAlias()
	{
		return "commands";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		List<GenericCommand> commands = new LinkedList<>();
		List<GenericCommand> commands2 = new LinkedList<>();


		CommandManager manager = bot.managers.get(0);
		commands.addAll(manager.getCommands());
		commands2.addAll(manager.getNonPrivateCommands().parallelStream().filter(c -> !(c instanceof _Alias_)).collect(Collectors.toList()));
		List<Container> containers = manager.getContainers();

		if (!event.allArgs.isEmpty())
		{
			// Parse Regular Commands
			for (GenericCommand c : commands)
			{
				if (c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send("Command Info for **" + c.getAlias() + "**: " + c.getInfo());
					return;
				}
			}

			// Parse Non-Private Commands
			for (GenericCommand c : commands2)
			{
				if (c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send("Command Info for **" + c.getAlias() + "**: " + c.getInfo());
					return;
				}
			}

			// Parse Containers
			for (Container c : containers)
			{
				if (c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send(c.getInfo());
					return;
				}

				// Look for Command
				GenericCommand cmd = c.getCommand(event.args[0]);
				if (cmd == null) // Command found?
					continue;
				event.send("Command Info for **" + cmd.getAlias() + "**: " + cmd.getInfo());
				return;
			}

			// Seems like it doesn't exist.
			event.send("No command named **" + event.allArgs + "** registered.\n*Call this command without arguments to retrieve a list of commands.*");
			return;
		}


		// No search term given -> list commands
		String string = "";
		if (!commands.isEmpty())
		{
			string = "**Regular Commands**\n```xml";
			for (GenericCommand c : commands)
			{
				if (c instanceof _Alias_) continue;
				string += "\n> " + c.getAlias() + " " + c.getAttributes();
			}
			string += "```";
		}
		if (!event.isPrivate && !commands2.isEmpty())
		{
			string += "\n**Guild only**\n```xml";

			for (GenericCommand c : commands2)
			{
				string += "\n> " + c.getAlias() + " " + c.getAttributes();
			}
			string += "```";
		}

		if (!manager.getContainers().isEmpty())
		{
			string += "\n**Categorized**\n```xml";
			for (Container c : manager.getContainers())
			{
				if ((!c.isPrivate() && event.isPrivate) || (c.isAdmin && !event.author.getId().equals(MusicBot.config.owner)))
					continue;
				string += "\n> " + c.getAlias();
			}
			string += "```";
		}

		event.send(string);
	}
}

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

			// Pares Containers
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
				if (!c.isPrivate() && event.isPrivate)
					continue;
				string += "\n> " + c.getAlias();
			}
			string += "```";
		}

		event.send(string);
	}
}

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

		if (!event.allArgs.isEmpty())
		{
			for (GenericCommand c : commands)
			{
				if (c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send("Command Info for **" + bot.config.prefix + c.getAlias() + "**: " + c.getInfo());
					return;
				}
			}

			for (GenericCommand c : commands2)
			{
				if (c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send("Command Info for **" + bot.config.prefix + c.getAlias() + "**: " + c.getInfo());
					return;
				}
			}

			event.send("No command named **" + event.allArgs + "** registered.\n*Call this command without arguments to retrieve a list of commands.*");
			return;
		}


		String regular = "**Regular Commands**\n```xml";
		for (GenericCommand c : commands)
		{
			if (c instanceof _Alias_) continue;
			regular += "\n> " + c.getAlias() + " " + c.getAttributes();
		}
		regular += "```";

		if (!event.isPrivate && !commands2.isEmpty())
		{
			regular += "\n**Guild only**\n```xml";

			for (GenericCommand c : commands2)
			{
				regular += "\n> " + c.getAlias() + " " + c.getAttributes();
			}
			regular += "```";
		}

		if (!manager.getContainers().isEmpty())
		{
			regular += "\n**Categorized**\n```xml";
			for (Container c : manager.getContainers())
			{
				if(!c.isPrivate() && event.isPrivate)
					continue;
				regular += "\n> " + c.getAlias();
			}
			regular += "```";
		}

		event.send(regular);
	}
}

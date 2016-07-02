package minn.music.commands;

import minn.music.MusicBot;

import java.util.List;

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
		List<GenericCommand> commands = bot.manager.getCommands();
		List<GenericCommand> commands2 = bot.manager.getNonPrivateCommands();
		if(!event.allArgs.isEmpty())
		{
			for(GenericCommand c : commands)
			{
				if(c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send("Command Info for **" + bot.config.prefix + c.getAlias() + "**: " + c.getInfo());
					return;
				}
			}

			for(GenericCommand c : commands2)
			{
				if(c.getAlias().equalsIgnoreCase(event.allArgs))
				{
					event.send("Command Info for **" + bot.config.prefix + c.getAlias() + "**: " + c.getInfo());
					return;
				}
			}

			event.send("No command named **" + event.allArgs + "** registered.\n*Call this command without arguments to retrieve a list of commands.*");
			return;
		}


		String regular = "**Regular Commands**\n```xml";
		for(GenericCommand c : commands)
		{
			regular += "\n> " + c.getAlias() + " " + c.getAttributes();
		}
		regular += "```";

		if(!event.isPrivate)
		{
			regular += "\n**Guild only**\n```xml";

			for(GenericCommand c : commands2)
			{
				regular += "\n> " + c.getAlias() + " " + c.getAttributes();
			}
			regular += "```";
		}

		event.send(regular);
	}
}

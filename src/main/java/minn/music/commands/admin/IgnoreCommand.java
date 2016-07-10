package minn.music.commands.admin;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.util.EntityUtil;
import minn.music.util.IgnoreUtil;
import net.dv8tion.jda.entities.TextChannel;

public class IgnoreCommand extends GenericCommand
{

	public String getInfo()
	{
		return "Gives owner ability to ignore specific channels.";
	}

	public String getAttributes()
	{
		return "<+/-> <channel>";
	}

	@Override
	public String getAlias()
	{
		return "ignore";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("Not happening.");
			return;
		}
		if (event.args.length < 2)
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
		String channel = event.allArgs.split("\\s+", 2)[1];
		TextChannel target = EntityUtil.resolveTextChannel(channel, event.api);
		if (target == null)
		{
			if (channel.equalsIgnoreCase("all"))
			{
				if (method.equals("+"))
				{
					IgnoreUtil.ignore((TextChannel[]) event.guild.getTextChannels().toArray());
					event.send("Stopped listening to this guild.");
				}
				else
				{
					IgnoreUtil.unIgnore((TextChannel[]) event.guild.getTextChannels().toArray());
					event.send("Listening to entire guild now.");
				}
				return;
			}
			target = EntityUtil.getFirstText(event.allArgs, event.guild);
			if (target != null)
			{
				if(method.equals("+"))
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
			if(method.equals("+"))
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

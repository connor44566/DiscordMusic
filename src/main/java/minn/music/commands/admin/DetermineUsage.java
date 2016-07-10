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

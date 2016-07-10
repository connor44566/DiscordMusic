package minn.music.commands.admin;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.managers.CommandManager;

import java.util.LinkedList;
import java.util.List;

public class DetermineShards extends GenericCommand
{
	private final List<CommandManager> list;

	public DetermineShards(List<CommandManager> list)
	{
		this.list = list;
	}

	@Override
	public String getAlias()
	{
		return "shards";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if(!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You can't use admin commands.");
			return;
		}
		for(int i = 0; i < new LinkedList<>(list).size(); i++)
		{
			list.get(i).getJDA().getAccountManager().setGame("Shard: " + i);
		}
		event.send("Determined shards. Look at bot's status.");
	}
}

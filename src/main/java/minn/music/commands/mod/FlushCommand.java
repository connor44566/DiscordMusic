package minn.music.commands.mod;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;

import java.util.LinkedList;
import java.util.List;

public class FlushCommand extends GenericCommand
{

	public boolean isPrivate()
	{
		return false;
	}

	public String getInfo()
	{
		return "Deletes messages sent from the bot.\nRequires permission: **MESSAGE_MANAGE**.";
	}

	@Override
	public String getAlias()
	{
		return "flush";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!((TextChannel) event.channel).checkPermission(event.author, Permission.MESSAGE_MANAGE) && !event.author.getId().equals(MusicBot.config.owner))
		{
			event.send(":anger: You don't have the required permissions.");
			return;
		} else if (!((TextChannel) event.channel).checkPermission(event.api.getSelfInfo(), Permission.MESSAGE_MANAGE))
		{
			event.send(":anger: I don't have the required permission to delete messages.");
			return;
		}
		List<String> ids = new LinkedList<>();
		event.channel.getHistory().retrieve(100).parallelStream().filter(m -> !m.isPinned() && (m.getAuthor() == null || m.getAuthor().getId().equals(event.api.getSelfInfo().getId()))).forEach(m -> ids.add(m.getId()));
		if (ids.size() == 1)
			event.channel.getMessageById(ids.get(0)).deleteMessage();
		if (!ids.isEmpty())
			((TextChannel) event.channel).deleteMessagesByIds(ids);
		event.send(":white_check_mark:");
	}
}

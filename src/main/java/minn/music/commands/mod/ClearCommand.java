package minn.music.commands.mod;

import minn.music.commands.GenericCommand;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ClearCommand extends GenericCommand
{

	public String getInfo()
	{
		return "Clears `[amount]` messages. Default 100.\nRequires permission **MANAGE_MESSAGE**.";
	}

	@Override
	public String getAlias()
	{
		return "clear";
	}

	public boolean isPrivate()
	{
		return false;
	}

	public String getAttributes()
	{
		return "[amount]";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!((TextChannel) event.channel).checkPermission(event.author, Permission.MESSAGE_MANAGE))
		{
			event.send(":anger: You don't have the required permissions.");
			return;
		} else if (!((TextChannel) event.channel).checkPermission(event.api.getSelfInfo(), Permission.MESSAGE_MANAGE))
		{
			event.send(":anger: I don't have the required permission to delete messages.");
			return;
		}
		int amount = 100;
		if (!event.allArgs.isEmpty())
		{
			try
			{
				amount = Integer.parseInt(event.args[0]);
			} catch (NumberFormatException e)
			{
				event.send(":anger: Number is not valid.");
				return;
			}
		}
		List<Message> hist = event.channel.getHistory().retrieve(amount).parallelStream().filter(m -> !m.isPinned()).collect(Collectors.toList());

		if (hist.isEmpty())
		{
			event.send(":anger: No non-pinned messages found.");
			return;
		}

		List<List<String>> collections = new LinkedList<>();
		while (!hist.isEmpty())
		{
			List<Message> sublist = hist.subList(0, Math.min(100, hist.size()));
			List<String> ids = new LinkedList<>();
			sublist.forEach(m -> ids.add(m.getId()));
			collections.add(ids);
			hist.removeAll(sublist);
		}

		for (List<String> content : collections)
		{
			if (content.size() == 1)
				event.channel.getMessageById(content.get(0)).deleteMessage();
			else
				((TextChannel) event.channel).deleteMessagesByIds(content);
			try
			{
				Thread.sleep(2000);
			} catch (InterruptedException ignored)
			{
			}
		}
		event.send(":white_check_mark:");
	}
}

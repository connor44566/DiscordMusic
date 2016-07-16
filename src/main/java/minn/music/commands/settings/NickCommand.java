package minn.music.commands.settings;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.utils.PermissionUtil;

public class NickCommand extends GenericCommand
{
	public String getAttributes()
	{
		return "[name]";
	}

	@Override
	public String getAlias()
	{
		return "nick";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!PermissionUtil.checkPermission(event.author, Permission.NICKNAME_MANAGE, event.guild) && !event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are unable to change my nickname.");
			return;
		}
		if (!PermissionUtil.checkPermission(event.api.getSelfInfo(), Permission.NICKNAME_CHANGE, event.guild) && !PermissionUtil.checkPermission(event.api.getSelfInfo(), Permission.NICKNAME_MANAGE, event.guild))
		{
			event.send("I am unable to update my nickname, soz.");
			return;
		}
		if (event.allArgs.length() >= 32)
		{
			event.send("This name is too long to be allowed.");
			return;
		}
		event.guild.getManager().setNickname(event.api.getSelfInfo(), (event.allArgs.isEmpty() ? null : event.allArgs));
		event.send("Updated!");
	}
}

package minn.music.commands.mod;

import minn.music.commands.GenericCommand;
import minn.music.managers.CommandManager;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;

public class SoftbanCommand extends GenericCommand
{

	public boolean isPrivate()
	{
		return false;
	}

	public String getAttributes()
	{
		return "<mention>";
	}

	@Override
	public String getAlias()
	{
		return "softban";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (event.allArgs.isEmpty())
		{
			event.send("You have to mention someone to ban them.");
			return;
		}
		User user = EntityUtil.resolveUser(event.allArgs, event.api);
		if (!PermissionUtil.checkPermission(event.author, Permission.BAN_MEMBERS, event.guild) || (event.guild.getUsers().contains(user) && !PermissionUtil.canInteract(event.author, user, event.guild)))
		{
			event.send("You are not allowed to ban this user.");
			return;
		}
		if (!PermissionUtil.checkPermission(event.api.getSelfInfo(), Permission.BAN_MEMBERS, event.guild) || (event.guild.getUsers().contains(user) && !PermissionUtil.canInteract(event.api.getSelfInfo(), user, event.guild)))
		{
			event.send("I need permission to ban and my highest role needs to be above the targeted user's highest role.");
			return;
		}
		try
		{
			if (user != null)
				event.guild.getManager().ban(user, 7);
			else if (EntityUtil.isID(event.allArgs))
				event.guild.getManager().ban(event.allArgs, 7);
			else
			{
				event.send("I am unable to ban that user.");
				return;
			}
		} catch (IllegalArgumentException e)
		{
			event.send(e.getMessage());
			return;
		}
		event.send("*" + (user != null ? EntityUtil.transform(user) : "unknown user") + " was banned.*");
		try
		{
			Thread.sleep(2000);
		} catch (InterruptedException e)
		{
			CommandManager.LOG.log(e);
		}
		if (user != null)
			event.guild.getManager().unBan(user);
		else
			event.guild.getManager().unBan(event.allArgs);
	}
}

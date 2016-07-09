package minn.music.commands.mod;

import minn.music.commands.GenericCommand;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.PermissionUtil;

public class MuteCommand extends GenericCommand
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
		return "mute";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		User user = EntityUtil.resolveUser(event.allArgs, event.api);
		if (user == null)
		{
			event.send("You have to mention someone to mute them.");
			return;
		}
		if (!event.guild.getUsers().contains(user))
		{
			event.send("User is not in this server.");
			return;
		}
		if (!PermissionUtil.checkPermission(event.author, Permission.MANAGE_PERMISSIONS, (TextChannel) event.channel))
		{
			event.send("You are not allowed to manage this channel.");
			return;
		}
		if (!PermissionUtil.checkPermission(event.api.getSelfInfo(), Permission.MANAGE_PERMISSIONS, (TextChannel) event.channel))
		{
			event.send("I need permission to manage channel permissions.");
			return;
		}
		((TextChannel) event.channel).getOverrideForUser(user).getManager().deny(Permission.MESSAGE_WRITE).update();
		event.send("*Muted " + EntityUtil.transform(user) + "*\nTo un-mute a user go to channel's permissions and grant the user to send messages in the channel.");
	}
}

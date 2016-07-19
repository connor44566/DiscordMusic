package minn.music.commands.settings;

import minn.music.commands.GenericCommand;
import minn.music.settings.GuildSettings;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.PermissionUtil;

public class ModLogCommand extends GenericCommand
{

	public String getAttributes()
	{
		return "[channel]";
	}

	public String getInfo()
	{
		return "Sets the guild's modlog channel.\n__Features:__\n> Ban tracking\n> Nickname changes\n> Username changes\n\nRequires permission **MANAGE_SERVER**.";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "modlog";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!PermissionUtil.checkPermission(event.author, Permission.MANAGE_SERVER, event.guild))
		{
			event.send("You are missing required permissions.");
			return;
		}
		TextChannel channel = (TextChannel) event.channel;
		if (!event.allArgs.isEmpty())
			channel = EntityUtil.resolveTextChannel(event.args[0], event.guild);
		if (channel == null)
		{
			event.send("No channel found for **" + event.args[0] + "**.");
			return;
		}

		GuildSettings.get(event.guild).setModLog(channel);
		event.send("ModLog is now in " + channel.getAsMention());
	}
}

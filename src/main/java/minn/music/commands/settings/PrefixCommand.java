package minn.music.commands.settings;

import minn.music.commands.GenericCommand;
import minn.music.managers.PrefixManager;

public class PrefixCommand extends GenericCommand
{

	public String getAttributes()
	{
		return "<prefix>";
	}

	public String getInfo()
	{
		return "\nUsed to set a custom prefix for current server/guild.\n*Can only be invoked by server owner.*\nThis will **not** override the default prefix!\n\nPro Tip: Setting an empty fix will remove it.";
	}

	@Override
	public String getAlias()
	{
		return "prefix";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (event.author != event.guild.getOwner())
		{
			event.send("You are unable to set a custom prefix. (Owner only)");
			return;
		}
		if (event.allArgs.isEmpty())
		{
			PrefixManager.removeCustom(event.guild);
			event.send("Custom prefix has been erased.");
			return;
		}
		event.send((PrefixManager.setCustom(event.guild, event.args[0]) ? String.format("Prefix has been changed to **%s**.", event.args[0]) : "Prefix is not allowed."));
	}
}

package minn.music.commands.settings;

import minn.music.commands.GenericCommand;
import minn.music.settings.GuildSettings;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.utils.PermissionUtil;

public class WelcomeCommand extends GenericCommand
{

	public String getInfo()
	{
		return "Set's the Server welcome level or message.\n" +
				"Levels: " +
				"\n> UNSET (**-1**) - No welcome messages (default)" +
				"\n> NORMAL (**0**) - Sends a message in public channel." +
				"\n> MENTION (**1**) - Sends a message in public channel and mentions the new user." +
				"\n> DIRECT (**2**) - Sends a DM to the new user.\n\n__Vars:__ `userid`,`server`, `{mention}` (Surround with {var}, example `{userid}`)";
	}

	public String getAttributes()
	{
		return " <message/level> <message/integer>";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "welcome";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!PermissionUtil.checkPermission(event.author, Permission.MANAGE_SERVER, event.guild))
		{
			event.send("You require the permission **MANAGE_SERVER** to access this setting.");
			return;
		}
		if (event.allArgs.isEmpty())
		{
			event.send("Usage: " + getAlias() + " " + getAttributes() +
					"\nExample 1: `" + getAlias() + " message Welcome to {server}, <@{userid}>!` would mention and tell the name." +
					"\nExample 2: `" + getAlias() + " level 2` would send a direct message to the user.");
			return;
		}

		String method = event.args[0].toLowerCase();
		String input = null;
		if (event.args.length > 1) input = event.allArgs.split("\\s+", 2)[1];

		if (method.equalsIgnoreCase("level"))
			try
			{
				if (input == null)
				{
					GuildSettings.get(event.guild).setWelcomeLevel(GuildSettings.WelcomeLevel.UNSET);
					event.send("Welcome level has been set to -1.");
					return;
				}
				GuildSettings.WelcomeLevel level = GuildSettings.getLevelFor(Integer.parseInt(input));
				GuildSettings.get(event.guild).setWelcomeLevel(level);
				event.send("Level: **" + level.toString() + "**");
			} catch (IllegalArgumentException e)
			{
				event.send("**" + event.args[0] + "** is not a valid level. Look at help page.");
			}
		else if (method.equalsIgnoreCase("message"))
		{
			try
			{
				if (input == null)
				{
					GuildSettings.get(event.guild).setWelcomeMessage(null);
					event.send("Removed custom welcome message.");
					return;
				}
				GuildSettings.get(event.guild).setWelcomeMessage(input);
				event.send("Message has been updated.");
			} catch (IllegalArgumentException e)
			{
				event.send(e.getMessage());
			}
		} else event.send("Invalid input. Call command without input to get 2 examples.");
	}
}

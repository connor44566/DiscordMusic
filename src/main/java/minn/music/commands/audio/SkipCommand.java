package minn.music.commands.audio;

import net.dv8tion.jda.player.MusicPlayer;

public class SkipCommand extends GenericAudioCommand
{

	public String getAttributes()
	{
		return "[amount/all]";
	}

	public String getInfo()
	{
		return "Skips [amount/all] songs. (Including current in amount)";
	}

	@Override
	public String getAlias()
	{
		return "skip";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		try
		{
			MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			if (player == null)
			{
				event.send("No queued songs.");
				return;
			} else if (event.allArgs.equalsIgnoreCase("all"))
			{
				player.getAudioQueue().clear();
				player.skipToNext();
				event.send("Skipped all songs.");
				return;
			}
			try
			{
				int amount = Integer.parseInt(event.args[0]);
				int i;
				for (i = 0; i < amount - 1 && !player.getAudioQueue().isEmpty(); i++)
				{
					player.getAudioQueue().remove();
				}
				player.skipToNext();
				event.send("Skipped " + ++i + " song" + ((i == 1) ? "" : "s") + ".");
			} catch (IndexOutOfBoundsException e)
			{
				player.skipToNext();
				event.send("Skipped current song.");
			} catch (NumberFormatException e)
			{
				event.send("Invalid amount. Must be an integer value. (i.e. 2 or 4 and not 2.4)");
			}
		} catch (ClassCastException e)
		{
			event.send("Player is not available.");
		}
	}
}

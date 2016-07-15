package minn.music.commands.audio;

import net.dv8tion.jda.player.MusicPlayer;

public class PlayerVolumeCommand extends GenericAudioCommand
{
	@Override
	public String getAttributes()
	{
		return "[float]";
	}

	@Override
	public String getAlias()
	{
		return "volume";
	}

	@Override
	public String getInfo()
	{
		return "Sets the player's volume to given float value. (Example float value: 0.5)";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		try
		{
			MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			if (player == null)
			{
				player = new MusicPlayer();
				event.guild.getAudioManager().setSendingHandler(player);
			}

			if (event.allArgs.isEmpty())
			{
				event.send("Current volume **" + player.getVolume() + "**.");
				return;
			}

			float vol = Float.parseFloat(event.args[0]);
			vol = Math.min(Math.max(0, vol), 1); // Ensure 0 - 1
			player.setVolume(vol);
			event.send("**Volume: " + vol + "**");
		} catch (NumberFormatException e)
		{
			event.send("Invalid input. Example volume 0.5");
		} catch (ClassCastException e)
		{
			event.send("Player is not available.");
		}
	}
}

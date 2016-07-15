package minn.music.commands.audio;

import net.dv8tion.jda.player.MusicPlayer;

import java.util.Collections;

public class PlayerShuffleCommand extends GenericAudioCommand
{
	@Override
	public String getAttributes()
	{
		return "";
	}

	@Override
	public String getAlias()
	{
		return "shuffle";
	}

	@Override
	public String getInfo()
	{
		return "Shuffles current queue.";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		try
		{
			MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			if (player == null)
				event.send("Queue is empty.");
			else
			{
				Collections.shuffle(player.getAudioQueue());
				event.send("Queue has been shuffled.");
			}
		} catch (ClassCastException e)
		{
			event.send("Player is not available.");
		}
	}
}

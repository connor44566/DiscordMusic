package minn.music.commands.audio;

import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioSource;

public class RemoveSongCommand extends GenericAudioCommand
{
	@Override
	public String getAlias()
	{
		return "songremove";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
		if (player == null)
		{
			event.send("Your player has no queue songs.");
			return;
		}
		try
		{
			int index = Integer.parseInt(event.args[0]) - 1;
			if (index >= player.getAudioQueue().size() || index < 0)
			{
				event.send("Index is not listed.");
				return;
			}
			AudioSource source = player.getAudioQueue().get(index);
			player.getAudioQueue().remove(index);
			event.send("Removed **" + source.getInfo().getTitle() + "** from the queue.");
		} catch (NumberFormatException | IndexOutOfBoundsException e)
		{
			event.send("Invalid input. Usage: `remove <index>`.\nExample: `remove 5`");
		}
	}

	@Override
	public String getAttributes()
	{
		return "<index>";
	}

	@Override
	public String getInfo()
	{
		return "Removes the entry in the current queue fitting to given <index>.";
	}
}

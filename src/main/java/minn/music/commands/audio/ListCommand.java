package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import minn.music.util.TimeUtil;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioInfo;

public class ListCommand extends GenericCommand
{

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "list";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		MusicPlayer player = null;
		long total = 0L;
		try
		{
			player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
		} catch (ClassCastException ignored)
		{
		}
		if (player == null)
		{
			event.send("No queued songs.");
			return;
		}
		if (player.getCurrentAudioSource() == null && player.getAudioQueue().isEmpty())
		{
			event.send("No queued songs.");
			return;
		}
		String response = "__Current:__ " +
				"`[" + player.getCurrentTimestamp().getTimestamp() + "/" + player.getCurrentAudioSource().getInfo().getDuration().getTimestamp() + "]` ** " +
				player.getCurrentAudioSource().getInfo().getTitle().replaceAll("[*]{2}", "\\*\\*") + "**";

		total += player.getCurrentAudioSource().getInfo().getDuration().getTotalSeconds() - player.getCurrentTimestamp().getTotalSeconds();

		if (!player.getAudioQueue().isEmpty())
		{
			response += "\n__Queue: **" + player.getAudioQueue().size() + "** entries.__\n";
			for (int i = 0; i < player.getAudioQueue().size(); i++)
			{
				AudioInfo info = player.getAudioQueue().get(i).getInfo();
				if (i < 5)
					response += "\n**" + (i + 1) + ")** `[" + info.getDuration().getTimestamp() + "]` " + info.getTitle().replaceAll("[*]{2}", "\\*\\*");
				total += info.getDuration().getTotalSeconds();
			}
			if (player.getAudioQueue().size() >= 5)
			{
				response += "\n...";
			}
		}
		response += "\n\n" + TimeUtil.time(total) + " left.";

		event.send(response);
	}


}

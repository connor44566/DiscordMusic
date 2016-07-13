package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import minn.music.util.PersistenceUtil;
import minn.music.util.TimeUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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
			String document = response;
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
		response += "\n\nFull list at: " + getDocument(player, event.guild) + "\n" + TimeUtil.time(total) + " left.";

		event.send(response);
	}

	private String getDocument(MusicPlayer player, Guild guild)
	{
		assert player != null;
		AudioSource prev = player.getPreviousAudioSource();
		AudioSource curr = player.getCurrentAudioSource();
		List<AudioSource> queue = player.getAudioQueue();
		String content = "Created at: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/dd/MM hh:mm:ss a")) + " © MinnDevelopment\n\nPlaylist for: " + guild.getName() + "\n";
		if (prev != null)
			content += "\nPrevious song: " + prev.getInfo().getTitle();
		if (curr != null)
			content += "\nCurrent song: " + curr.getInfo().getTitle() + " [" + player.getCurrentTimestamp().getTimestamp()  +"/" + curr.getInfo().getDuration().getTimestamp() + "]";
		if (!queue.isEmpty())
		{
			content += "\n\n\nQueue:\n";
			int i = 0;
			for (AudioSource s : new LinkedList<>(queue))
			{
				content += "[" + ++i + "] [" + s.getInfo().getDuration().getTimestamp() + "] " + s.getInfo().getTitle() + "\n";
			}
		}

		try
		{
			return PersistenceUtil.hastebin(content) + ".dos";
		} catch (InterruptedException | ExecutionException | TimeoutException e)
		{
			return "*document unavailable*";
		}
	}


}

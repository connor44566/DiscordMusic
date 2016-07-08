package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.List;

public class PlayCommand extends GenericCommand
{
	private static final SimpleLog LOG = SimpleLog.getLog("PlayCommand");
	/*private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
		final Thread t = new Thread(r, "PlayCommand");
		t.setDaemon(true);
		return t;
	});*/

	public String getAttributes()
	{
		return "[url]";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "play";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		MusicPlayer player = new MusicPlayer();
		player.setVolume(.5f); // 50% volume
		if (event.guild.getAudioManager().getSendingHandler() != null)
		{
			try
			{
				player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			} catch (ClassCastException ignored)
			{
			}
		} else
			event.guild.getAudioManager().setSendingHandler(player);

		String url = event.allArgs;

		if (url.isEmpty())
		{
			if (!player.isPlaying() && !player.getAudioQueue().isEmpty())
			{
				player.play();
				event.send("**Started playing...**");
			} else
				event.send("I am unable to start playing. Provide a URL.");
			return;
		}
		Playlist list;
		try
		{
			list = Playlist.getPlaylist(url);
		} catch (NullPointerException e)
		{
			event.send("Something went wrong with your request. Please inform a bot dev. (NPE on play)");
			LOG.log(e);
			return;
		}
		List<AudioSource> sources = list.getSources();
		final Message[] currentMessage = {null};

		if (sources.isEmpty())
		{
			event.send("No audio source detected for URL.");
			return;
		}
		if (sources.size() == 1)
			event.send("Detected one audio source. Starting to queue.", m -> currentMessage[0] = m);
		else
			event.send("Detected playlist with **" + sources.size() + "** entries. Starting to queue.", m -> currentMessage[0] = m);


		int error = 0;
		for (AudioSource s : sources)
		{
			AudioInfo info = s.getInfo();
			if (info.isLive())
			{
				error++;
				continue;
			}
			if (info.getError() == null)
			{
				player.getAudioQueue().add(s);
				if (!player.isPlaying())
				{
					if (currentMessage[0] != null)
						currentMessage[0].updateMessageAsync("**Started playing...**", null);
					player.play();
				}
			} else
			{
				error++;
				LOG.warn("Encountered error: " + info.getError());
			}
		}
		if (error == 0)
			currentMessage[0].updateMessageAsync("Finished enqueuing sources.", null);
		else if (error == sources.size())
			currentMessage[0].updateMessageAsync("None of the sources were available.", null);
		else if (error == 1)
			currentMessage[0].updateMessageAsync("Enqueueing of sources resulted in one error.", null);
		else if (error > 1)
			currentMessage[0].updateMessageAsync("Enqueueing of sources resulted in " + error + " errors.", null);
	}
}
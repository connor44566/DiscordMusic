package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import minn.music.util.SearchUtil;
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

	public String getInfo()
	{
		return "Used to queue audio sources(like Youtube/Soundcloud).\nAccepts URLs and search terms.\nIf no input is given it will try to start playing. ";
	}

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
	public void invoke(CommandEvent event) // Don't even try to understand it, I don't even understand it myself
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
		final Playlist[] list = new Playlist[1];
		MusicPlayer finalPlayer = player;
		final Message[] msg = new Message[]{null};
		event.send("Fetching...", m ->
		{
			new Thread(() ->
			{
				msg[0] = m;
				try
				{
					list[0] = Playlist.getPlaylist(url);
				} catch (NullPointerException e)
				{
					if (!SearchUtil.isURL(url)) // Not a URL? Look it up.
					{
						AudioSource source = SearchUtil.getRemoteSource(url);
						if (source != null)
						{
							finalPlayer.getAudioQueue().add(source);
							String s = String.format("Added **%s**", source.getInfo().getTitle());
							if (!finalPlayer.isPlaying())
							{
								finalPlayer.play();
								s += " and started playing";
							}
							if (msg[0] != null) msg[0].updateMessageAsync(s + ".", null);
						}
						return;
					}
					if (msg[0] != null)
						msg[0].updateMessageAsync("Something went wrong with your request. Please inform a bot dev. (NPE on play)", null);
					return;
				}
				List<AudioSource> sources = list[0].getSources();
				if (sources.isEmpty())
				{
					if (msg[0] != null) msg[0].updateMessageAsync("No audio source detected for URL.", null);
					return;
				}
				if (sources.size() == 1)
				{
					if (msg[0] != null)
						msg[0].updateMessageAsync("Detected one audio source. Starting to queue.", msg0 -> msg[0] = msg0);
				} else if (msg[0] != null)
					msg[0].updateMessageAsync("Detected playlist with **" + sources.size() + "** entries. Starting to queue.", msg0 -> msg[0] = msg0);
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
						finalPlayer.getAudioQueue().add(s);
						if (!finalPlayer.isPlaying())
						{
							if (msg[0] != null)
								msg[0].updateMessageAsync("**Started playing...**", null);
							finalPlayer.play();
						}
					} else
					{
						error++;
						LOG.debug("Encountered error: " + info.getError());
					}
				}
				if (msg[0] == null)
					return;
				if (error == 0)
					msg[0].updateMessageAsync("Finished enqueuing sources.", null);
				else if (error == sources.size())
					msg[0].updateMessageAsync("None of the sources were available.", null);
				else if (error == 1)
					msg[0].updateMessageAsync("Enqueueing of sources resulted in one error.", null);
				else if (error > 1)
					msg[0].updateMessageAsync("Enqueueing of sources resulted in " + error + " errors.", null);
			}, "Fetcher").start();
		});
	}
}

/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import minn.music.managers.ConnectionManager;
import minn.music.util.SearchUtil;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PlayCommand extends GenericCommand
{
	private static final SimpleLog LOG = SimpleLog.getLog("PlayCommand");
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 200, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r ->
	{
		Thread t = new Thread(r, "PlaylistEnqueue");
		t.setDaemon(true);
		return t;
	});
	private ThreadPoolExecutor queue = new ThreadPoolExecutor(1, 300, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), r ->
	{
		Thread t = new Thread(r, "Queue Entry");
		t.setDaemon(true);
		return t;
	});

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
		{
			event.guild.getAudioManager().setSendingHandler(player);
			ConnectionManager.addPlayer(event.guild, player);
		}


		if (player.getAudioQueue().size() >= 150)
		{
			event.send("You have reached the limit of requests. (150 max)");
			return;
		}
		String url = event.allArgs;
		if (url.isEmpty())
		{
			if (!player.isPlaying() && !player.getAudioQueue().isEmpty())
			{
				player.play();
				event.send("**Started playing...**");
			} else if (event.guild.getAudioManager().getSendingHandler() == player)
				event.send("I am unable to start playing. Provide a URL.");
			else if (player.isPlaying() && event.guild.getAudioManager().getSendingHandler() != player)
			{
				event.guild.getAudioManager().setSendingHandler(player);
				event.send("**Started playing...**");
			}
			return;
		}

		if (url.matches("<.+>"))
			url = url.replaceAll("^<(.*)>$", "$1");

		final Playlist[] list = new Playlist[1];
		MusicPlayer finalPlayer = player;
		final Message[] msg = new Message[]{null};
		String finalUrl = url;
		event.send("Fetching...", m ->
		{
			msg[0] = m;
			try
			{
				list[0] = Playlist.getPlaylist(finalUrl);
			} catch (NullPointerException e)
			{
				if (!SearchUtil.isURL(finalUrl)) // Not a URL? Look it up.
				{
					AudioSource source = SearchUtil.getRemoteSource(finalUrl);
					if (source != null)
					{
						AudioInfo info = source.getInfo();
						if (info.getError() != null)
						{
							event.send("I can't queue that.");
							return;
						}
						if (info.isLive())
						{
							event.send("I'm not playing livestreams.");
							return;
						}
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
			executor.submit(() ->
			{
				for (int i = 0; i < sources.size() && i < 50; i++)
				{
					int finalI = i;
					final boolean[] limited = {false};
					queue.submit(() ->
					{
						if(limited[0])
							return;
						AudioInfo info = sources.get(finalI).getInfo();
						if (info.isLive())
							return;
						if (info.getError() == null && finalPlayer.getAudioQueue().size() <= 150)
						{
							finalPlayer.getAudioQueue().add(sources.get(finalI));
							if (!finalPlayer.isPlaying())
							{
								if (msg[0] != null)
									msg[0].updateMessageAsync("**Started playing...**", null);
								finalPlayer.play();
							}
						} else if (info.getError() != null)
						{
							LOG.debug("Encountered error: " + info.getError());
						} else if (!limited[0])
						{
							limited[0] = true;
							msg[0].updateMessageAsync("Queue limit reached.", null);
						}
					});
				}
			});
		});
	}
}

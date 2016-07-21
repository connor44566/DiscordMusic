/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.audio.send;

import minn.music.managers.ConnectionManager;
import minn.music.util.PersistenceUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.utils.SimpleLog;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QueueManager
{

	private static HashMap<String, LinkedList<String>> queueCache;
	private static final SimpleLog LOG = SimpleLog.getLog("QueueManager");
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 200, 2, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r ->
	{
		Thread t = new Thread(r, "Resuming Playlist");
		t.setDaemon(true);
		return t;
	});

	static {
		queueCache = (HashMap<String, LinkedList<String>>) PersistenceUtil.retrieve("queueCache");
		new File("queueCache").delete();
		LOG.info("Ready!");
	}

	public static void resume(JDA api)
	{
		if (queueCache == null)
		{
			queueCache = new HashMap<>();
			LOG.warn("Queue Cache was null.");
			return;
		}
		queueCache.forEach((id, list) ->
		{
			VoiceChannel c = api.getVoiceChannelById(id);
			if (c == null)
			{
				LOG.debug("Channel is null.");
				return;
			}
			executor.submit(() ->
			{
				LOG.info("Resuming: " + id);
				Guild g = c.getGuild();
				MusicPlayer player = new MusicPlayer();
				g.getAudioManager().setSendingHandler(player);
				ConnectionManager.addPlayer(g, player);
				try
				{
					g.getAudioManager().openAudioConnection(c);
				} catch (Exception e)
				{
					LOG.warn(e);
				}
				int[] i = new int[]{0};
				list.parallelStream().forEach(s ->
				{
					if (i[0]++ >= 15)
						return;
					executor.submit(() ->
					{
						try
						{
							Playlist playlist = Playlist.getPlaylist(s);
							playlist.getSources().forEach(source ->
							{
								player.getAudioQueue().add(source);
								if (!player.isPlaying())
									player.play();
							});
						} catch (Exception e)
						{
							LOG.debug(e);
						}
					});
				});
				LOG.info("Resumed: " + c.toString());
			});
		});
	}

	public static void save(JDA jda) // Has to be blocking
	{
		for (Guild g : new LinkedList<>(jda.getGuilds()))
		{
			if (!g.getAudioManager().isConnected())
				continue;
			LOG.info("Saving : " + g.getAudioManager().getConnectedChannel());
			try
			{
				MusicPlayer player = (MusicPlayer) g.getAudioManager().getSendingHandler();
				if (player == null || player.getAudioQueue().isEmpty())
					return;
				LinkedList<String> list = new LinkedList<>();
				player.getAudioQueue().parallelStream().forEach(s ->
						list.add(s.getInfo().getOrigin()));
				queueCache.put(g.getAudioManager().getConnectedChannel().getId(), list);
			} catch (Exception ignored)
			{
			}
		}
	}

	public static void save()
	{
		PersistenceUtil.save(queueCache, "queueCache");
	}

}

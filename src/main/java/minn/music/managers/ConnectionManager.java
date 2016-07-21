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

package minn.music.managers;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.voice.VoiceLeaveEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.hooks.PlayerEventListener;
import net.dv8tion.jda.player.hooks.events.FinishEvent;
import net.dv8tion.jda.player.hooks.events.PlayerEvent;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ConnectionManager
{

	private final static Map<JDA, List<String>> shards = new HashMap<>();
	private final static Map<String, MusicPlayer> guilds = new HashMap<>();
	private final static Map<MusicPlayer, Long> last_check = new HashMap<>();
	private final static PlayerListener listener = new PlayerListener();
	private final static Thread keepAlive;
	private final static long KEEP_ALIVE_TIME;
	public final static SimpleLog LOG;

	static
	{
		KEEP_ALIVE_TIME = TimeUnit.MINUTES.toMillis(5);
		LOG = SimpleLog.getLog("Connection");
		keepAlive = new Thread(() ->
		{
			while (!Thread.currentThread().isInterrupted())
			{
				List<String> toRemove = new LinkedList<>();
				guilds.forEach((g, p) ->
				{
					if (p.isPlaying() || last_check.get(p) + KEEP_ALIVE_TIME > System.currentTimeMillis())
						return;
					AudioManager manager = getManagerFor(g);
					if (manager != null)
					{
						if (manager.isConnected())
							manager.closeAudioConnection();
						manager.setSendingHandler(null);
					}
					p.removeEventListener(listener);
					last_check.remove(p);
					toRemove.add(g);
					LOG.info("Closed Connection. (" + g + ")");
				});
				toRemove.parallelStream().forEach(guilds::remove);
				try
				{
					Thread.sleep(KEEP_ALIVE_TIME);
				} catch (InterruptedException e)
				{
					break;
				}
			}
		});
		keepAlive.setDaemon(true);
		keepAlive.start();
		LOG.info("Ready!");
	}

	public static AudioManager getManagerFor(String id)
	{
		AtomicReference<JDA> shard = new AtomicReference<>(null);
		shards.forEach((api, list) ->
		{
			if (!list.contains(id) || shard.get() != null)
				return;
			shard.set(api);
		});
		return shard.get().getAudioManager(shard.get().getGuildById(id));
	}

	public static void addPlayer(Guild guild, MusicPlayer player)
	{
		if (shards.containsKey(guild.getJDA()))
			shards.get(guild.getJDA()).add(guild.getId());
		else
		{
			List<String> list = new LinkedList<>();
			list.add(guild.getId());
			shards.put(guild.getJDA(), list);
		}
		if (!guild.getJDA().getRegisteredListeners().contains(listener))
			guild.getJDA().addEventListener(listener);
		guilds.put(guild.getId(), player);
		last_check.put(player, System.currentTimeMillis());
		player.addEventListener(listener);
	}

	private static class PlayerListener implements PlayerEventListener, EventListener
	{

		@Override
		public void onEvent(PlayerEvent event)
		{
			if (event instanceof FinishEvent)
				last_check.put(event.getPlayer(), System.currentTimeMillis());
		}

		@Override
		public void onEvent(Event event)
		{
			if (event instanceof VoiceLeaveEvent)
				if (((VoiceLeaveEvent) event).getOldChannel().getUsers().isEmpty() && guilds.containsKey(((VoiceLeaveEvent) event).getGuild().getId())
						&& ((VoiceLeaveEvent) event).getGuild().getAudioManager().getConnectedChannel() == ((VoiceLeaveEvent) event).getOldChannel()
						&& ((VoiceLeaveEvent) event).getGuild().getAudioManager().getSendingHandler() instanceof MusicPlayer)
					last_check.put((MusicPlayer) ((VoiceLeaveEvent) event).getGuild().getAudioManager().getSendingHandler(), System.currentTimeMillis());
		}
	}

}

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

package minn.music.commands.admin;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.util.TimeUtil;
import net.dv8tion.jda.audio.AudioSendHandler;
import net.dv8tion.jda.entities.*;
import net.dv8tion.jda.player.MusicPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class StatsCommand extends GenericCommand
{

	private final MusicBot bot;

	public StatsCommand(MusicBot bot)
	{
		this.bot = bot;
	}

	@Override
	public String getAlias()
	{
		return "stats";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You can't view the stats page doe.");
			return;
		}
		AtomicReference<Consumer<Message>> consumer = new AtomicReference<>();
		AtomicInteger count = new AtomicInteger();
		consumer.set(message ->
		{
			if (message == null || count.getAndAdd(1) >= 10) return;
			try
			{
				Thread.sleep(3000);
			} catch (InterruptedException e)
			{
				return;
			}
			message.updateMessageAsync(getStats(), consumer.get());
		});
		event.send(getStats(), consumer.get());
	}

	public String getStats()
	{
		String uptime = TimeUtil.uptime();
		List<VoiceChannel> channels = getVoiceChannels();
		int voiceConnections = getConnectedChannels().size();
		int shards = bot.getShards().size();
		int threads = Thread.activeCount();
		int guilds = getGuilds().size();
		int text = getTextChannels().size();
		int voice = channels.size();
		int queueSize = getQueueSize(channels);
		int pms = getPrivateChannels().size();
		long mem = getMemoryUsage();

		return "__Stats for total bot [**" + shards + "** shard(s)]__\n\n**"
				+ guilds + "** Guilds with **" + (text + voice) + "** channels. (**" + text + "** TC, **" + voice + "** VC)\n**"
				+ pms + "** Private Channels and **" + voiceConnections + "** Voice connections. (**" + queueSize + "** queue entries)" +
				"\nMemory in MB: [" + "`" + mem + "`/`" + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "`/`" + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "`] (**" + threads+"** Threads)"+
				"\nUptime: " + uptime ;
	}

	public static int getQueueSize(List<VoiceChannel> channels)
	{
		int[] amount = new int[]{0};
		channels.parallelStream().forEach(c ->
		{
			AudioSendHandler handler = c.getGuild().getAudioManager().getSendingHandler();
			if (!(handler instanceof MusicPlayer)) return;
			amount[0] += ((MusicPlayer) handler).getAudioQueue().size();
		});
		return amount[0];
	}

	public static long getMemoryUsage()
	{
		Runtime r = Runtime.getRuntime();
		return (r.totalMemory() - r.freeMemory()) / (1024 * 1024);
	}

	/**
	 * @return a joint list of all voice connections.
	 */
	public List<VoiceChannel> getConnectedChannels()
	{
		List<VoiceChannel> channels = new LinkedList<>();
		bot.getShards().forEach((i, api) ->
				api.getGuilds().parallelStream()
						.filter(g -> g.getAudioManager().isConnected())
						.forEach(g -> channels.add(g.getAudioManager().getConnectedChannel())));
		return channels;
	}

	public List<Guild> getGuilds()
	{
		List<Guild> guilds = new LinkedList<>();
		bot.getShards().forEach((i, api) -> guilds.addAll(api.getGuilds()));
		return guilds;
	}

	public List<TextChannel> getTextChannels()
	{
		List<TextChannel> channels = new LinkedList<>();
		bot.getShards().forEach((i, api) -> channels.addAll(api.getTextChannels()));
		return channels;
	}

	public List<PrivateChannel> getPrivateChannels()
	{
		List<PrivateChannel> channels = new LinkedList<>();
		bot.getShards().forEach((i, api) -> channels.addAll(api.getPrivateChannels()));
		return channels;
	}

	public List<VoiceChannel> getVoiceChannels()
	{
		List<VoiceChannel> channels = new LinkedList<>();
		bot.getShards().forEach((i, api) -> channels.addAll(api.getVoiceChannels()));
		return channels;
	}

}

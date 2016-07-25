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

package minn.music;

import minn.music.managers.CommandManager;
import minn.music.settings.Config;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.ReadyEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.*;
import java.util.function.Consumer;

public class MusicBot implements EventListener
{
	public static Config config;
	private final static SimpleLog LOG = SimpleLog.getLog("MusicBot");
	public final List<CommandManager> managers = new LinkedList<>();
	private final Map<Integer, JDA> shards = new HashMap<>();
	private Consumer<CommandManager> callback;

	public void onEvent(Event event)
	{
		if (event instanceof ReadyEvent)
		{
			CommandManager m = new CommandManager(event.getJDA(), this);
			managers.add(m);
			event.getJDA().removeEventListener(this);
			callback.accept(m);
		}
	}

	public MusicBot(Consumer<CommandManager> callback, int shards, Config cfg)
	{
		assert shards > 0 && callback != null;
		try
		{
			config = cfg;
			this.callback = callback;
			if(shards == 1)
			{
				this.shards.put(0, new JDABuilder()
						.setAudioEnabled(true)
						.setAutoReconnect(true)
						.setBotToken(config.token)
						.setBulkDeleteSplittingEnabled(false)
						.addListener(this)
						.buildAsync());
				return;
			}
			for (int i = 0; i < shards; i++)
			{
				this.shards.put(i, new JDABuilder()
						.setAudioEnabled(true)
						.setAutoReconnect(true)
						.setBotToken(config.token)
						.setBulkDeleteSplittingEnabled(false)
						.addListener(this)
						.useSharding(i, shards)
						.buildAsync());
			}
		} catch (Exception e)
		{
			LOG.log(e);
			System.exit(1);
		}
	}

	public Map<Integer, JDA> getShards()
	{
		return Collections.unmodifiableMap(shards);
	}
}

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

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import minn.music.MusicBot;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.ApplicationUtil;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class CarbonAPIManager
{
	private String carbon = null;
	private String abal = null;
	private String clientID;
	private MusicBot bot;
	private List<JDA> jdaList = new LinkedList<>();

	public CarbonAPIManager(JDA api)
	{
		clientID = ApplicationUtil.getApplicationId(api);
		try
		{
			carbon = (String) MusicBot.config.get("carbon");
		} catch (NullPointerException | ClassCastException e)
		{
			carbon = null;
		}
		try
		{
			abal = (String) MusicBot.config.get("botlist");
		} catch (NullPointerException | ClassCastException e)
		{
			carbon = null;
		}
	}

	public void setBot(MusicBot bot)
	{
		this.bot = bot;
	}

	public void listenTo(JDA api)
	{
		if (jdaList.contains(api))
			return;
		jdaList.add(api);
		api.addEventListener((EventListener) event ->
		{
			if (event instanceof GuildJoinEvent || event instanceof GuildLeaveEvent)
				post();
		});
	}

	private synchronized int getSize()
	{
		List<Guild> guildAmount = new LinkedList<>();
		bot.getShards().forEach((i, api) -> guildAmount.addAll(api.getGuilds()));
		return guildAmount.size();
	}

	private void post()
	{
		if (carbon != null)
		{
			try
			{
				if (Unirest.post("https://www.carbonitex.net/discord/data/botdata.php").body(new JSONObject()
						.put("servercount", getSize())
						.put("key", carbon)
						.put("logoid", (jdaList.isEmpty() ? null : jdaList.get(0).getSelfInfo().getAvatarId()))
						.toString()).asString().getStatus() > 299)
					carbon = null;
			} catch (UnirestException e)
			{
				carbon = null;
			}
		}
		if (abal != null)
		{
			try
			{
				if (Unirest.post("https://bots.discord.pw/api/bots/" + clientID + "/stats")
						.header("Authorization", abal)
						.body(new JSONObject()
								.put("server_count", getSize())
								.toString()).asString().getStatus() > 299)
					abal = null;
			} catch (UnirestException e)
			{
				abal = null;
			}
		}
	}

}

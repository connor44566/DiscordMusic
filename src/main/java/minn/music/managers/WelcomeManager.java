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

import minn.music.settings.GuildSettings;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.guild.member.GuildMemberBanEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.EventListener;

public class WelcomeManager implements EventListener
{

	public WelcomeManager(JDA api)
	{
		api.addEventListener(this);
	}

	@Override
	public void onEvent(Event event)
	{
		if (event instanceof GuildMemberJoinEvent)
			onJoin((GuildMemberJoinEvent) event);
		else if (event instanceof GuildMemberLeaveEvent && !(event instanceof GuildMemberBanEvent))
			onLeave((GuildMemberLeaveEvent) event);
	}

	private void onLeave(GuildMemberLeaveEvent event)
	{
		GuildSettings settings = GuildSettings.get(event.getGuild());
		if (event.getUser() == null) return;
		try
		{
			switch (settings.getWelcomeLevel().value)
			{
				default:
					return;
				case -1:
					return;
				case 0:
				{
					event.getGuild().getPublicChannel().sendMessageAsync("Goodbye to **" + event.getUser().getUsername().replace("@", "@\u0001") + "**.", null);
					break;
				}
				case 1:
				{
					event.getGuild().getPublicChannel().sendMessageAsync("Goodbye to **" + event.getUser().getAsMention() + "**.", null);
					break;
				}
				case 2:
				{
					event.getUser().getPrivateChannel().sendMessageAsync("We will miss you at **" + event.getGuild().getName() + "**!", null);
					break;
				}
			}
		} catch (Exception ignored)
		{
		}
	}

	private void onJoin(GuildMemberJoinEvent event)
	{
		GuildSettings settings = GuildSettings.get(event.getGuild());
		try
		{
			switch (settings.getWelcomeLevel().value)
			{
				default:
					return;
				case -1:
					return;
				case 0:
				{
					if (settings.getWelcomeMessage() == null)
						event.getGuild().getPublicChannel().sendMessageAsync("Welcome to **" + event.getGuild().getName() + "**, " + event.getUser().getUsername().replace("@", "@\u0001") + ".", null);
					else
						event.getGuild().getPublicChannel().sendMessageAsync(EntityUtil.stripMentions(settings.getWelcomeMessage(event), event.getJDA()), null);
					break;
				}
				case 1:
				{
					if (settings.getWelcomeMessage() == null)
						event.getGuild().getPublicChannel().sendMessageAsync("Welcome to **" + event.getGuild().getName() + "**, " + event.getUser().getAsMention() + ".", null);
					else
						event.getGuild().getPublicChannel().sendMessageAsync(settings.getWelcomeMessage(event), null);
					break;
				}
				case 2:
				{
					if (settings.getWelcomeMessage() == null)
						event.getUser().getPrivateChannel().sendMessageAsync("Welcome to **" + event.getGuild().getName() + "**!", null);
					else
						event.getUser().getPrivateChannel().sendMessageAsync(settings.getWelcomeMessage(event), null);
					break;
				}
			}
		} catch (PermissionException ignored)
		{
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}



}

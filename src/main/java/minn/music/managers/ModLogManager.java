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
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.impl.MessageImpl;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.guild.member.*;
import net.dv8tion.jda.exceptions.PermissionException;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.List;

public class ModLogManager implements EventListener
{

	private Tuple<User, MessageImpl> tuple = new Tuple<>();

	public static TextChannel getChannel(GuildSettings settings, JDA jda, Guild guild)
	{
		String log = settings.getModLog();
		if (log == null || log.isEmpty())
			return null;
		TextChannel channel = jda.getTextChannelById(log);
		if (channel == null || channel.getGuild() != guild) return null;
		return channel;
	}

	public void onRoleRemove(GuildMemberRoleRemoveEvent event)
	{
		TextChannel target = getChannel(GuildSettings.get(event.getGuild()), event.getJDA(), event.getGuild());
		if (target == null) return;
		target.sendMessageAsync(("Removed role(s) `" + joinRoles(event.getRoles()).replace("`", "\\`") + "` from **" + EntityUtil.transform(event.getUser()).replace("*", "\\*") + "**.").replace("@", "@\u0001"), null);
	}

	public void onRoleAdd(GuildMemberRoleAddEvent event)
	{
		TextChannel target = getChannel(GuildSettings.get(event.getGuild()), event.getJDA(), event.getGuild());
		if (target == null) return;
		target.sendMessageAsync(("Added role(s) `" + joinRoles(event.getRoles()).replace("`", "\\`") + "` to **" + EntityUtil.transform(event.getUser()).replace("*", "\\*") + "**.").replace("@", "@\u0001"), null);
	}

	public void onBan(GuildMemberBanEvent event)
	{
		TextChannel target = getChannel(GuildSettings.get(event.getGuild()), event.getJDA(), event.getGuild());
		if (target == null) return;
		target.sendMessageAsync(("_**" + EntityUtil.transform(event.getUser()).replaceAll("(\\*\\*|_)", "\\\\$1") + "** was banned._").replace("@", "@\u0001"),
				m -> tuple.set(event.getUser(), (m != null ? (MessageImpl) m : null)));
	}

	public void onUnBan(GuildMemberUnbanEvent event)
	{
		System.out.println((tuple.time + 30000) + " > " + System.currentTimeMillis());
		if (tuple.user == event.getUser() && tuple.time + 30000 >= System.currentTimeMillis())
		{
			tuple.message.updateMessageAsync(tuple.message.getRawContent().replaceAll("banned\\._$", "softbanned._"), null);
			tuple.set(null, null);
			return;
		}
		TextChannel target = getChannel(GuildSettings.get(event.getGuild()), event.getJDA(), event.getGuild());
		if (target == null) return;
		target.sendMessageAsync(("_**" + EntityUtil.transform(event.getUser()).replaceAll("(\\*\\*|_)", "\\\\$1") + "** was unbanned._").replace("@", "@\u0001"), null);
	}

	public void onNickChange(GuildMemberNickChangeEvent event)
	{
		TextChannel target = getChannel(GuildSettings.get(event.getGuild()), event.getJDA(), event.getGuild());
		if (target == null) return;
		if (event.getNewNick() != null && event.getPrevNick() != null)
			target.sendMessageAsync(("Nick update: **" + event.getPrevNick().replace("*", "\\*") + "** -> **" + event.getNewNick().replace("*", "\\*") + "**.").replace("@", "@\u0001"), null);
		else if (event.getNewNick() == null && event.getPrevNick() != null)
			target.sendMessageAsync(("Nick update: **" + event.getPrevNick().replace("*", "\\*") + "** -> **" + event.getUser().getUsername().replace("*", "\\*") + "**.").replace("@", "@\u0001"), null);
		else if (event.getPrevNick() == null)
			target.sendMessageAsync(("Nick update: **" + event.getUser().getUsername().replace("*", "\\*") + "** -> **" + event.getNewNick().replace("*", "\\*") + "**.").replace("@", "@\u0001"), null);
	}

	private static String joinRoles(List<Role> roles)
	{
		CharSequence[] seq = new String[roles.size()];
		for (int i = 0; i < roles.size(); i++)
		{
			seq[i] = roles.get(i).getName();
		}
		return "[" + String.join(", ", seq) + "]";
	}

	@Override
	public void onEvent(Event event)
	{
		if (event instanceof GenericGuildMemberEvent)
		{
			try
			{
				if (event instanceof GuildMemberNickChangeEvent)
					onNickChange((GuildMemberNickChangeEvent) event);
				else if (event instanceof GuildMemberBanEvent)
					onBan((GuildMemberBanEvent) event);
				else if (event instanceof GuildMemberUnbanEvent)
					onUnBan((GuildMemberUnbanEvent) event);
				else if (event instanceof GuildMemberRoleAddEvent)
					onRoleAdd((GuildMemberRoleAddEvent) event);
				else if (event instanceof GuildMemberRoleRemoveEvent)
					onRoleRemove((GuildMemberRoleRemoveEvent) event);
			} catch (PermissionException ignored)
			{
			} catch (Exception e) // Non-Permission exceptions
			{
				SimpleLog.getLog("ModLog").log(e);
			}
		}
	}

	public ModLogManager(JDA api)
	{
		api.addEventListener(this);
	}

	private class Tuple<T extends User, M extends MessageImpl>
	{

		private T user;
		private long time;
		private M message;

		public void set(T user, M message)
		{
			this.user = user;
			this.time = System.currentTimeMillis();
			this.message = message;
		}

	}

}

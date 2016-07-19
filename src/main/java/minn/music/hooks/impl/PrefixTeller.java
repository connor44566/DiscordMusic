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

package minn.music.hooks.impl;

import minn.music.hooks.MentionListener;
import minn.music.managers.PrefixManager;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

/**
 * Example implementation of MentionListener.<br/>
 * Purpose: Tell user the current guild's prefix.
 */
public class PrefixTeller implements MentionListener
{
	/**
	 * Example implementation of MentionListener
	 * @param event A GuildMessageReceivedEvent (We don't care about private mentions here)
	 */
	@Override
	public void onMention(GuildMessageReceivedEvent event)
	{
		if (event.getMessage().getRawContent().matches("^<@!?" + event.getJDA().getSelfInfo().getId()  +"> prefix$"))
			event.getChannel().sendMessageAsync("My current prefix is: **" + PrefixManager.getPrefix(event.getGuild()) + "**", null);
	}
}

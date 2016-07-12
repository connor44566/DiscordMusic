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

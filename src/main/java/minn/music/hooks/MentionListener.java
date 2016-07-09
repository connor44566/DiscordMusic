package minn.music.hooks;

import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

@FunctionalInterface public interface MentionListener
{
	/**
	 * Get's called when the Bot was mentioned. <b>Don't forget to register this listener.</b>
	 * @param event A GuildMessageReceivedEvent (We don't care about private mentions here)
	 */
	void onMention(GuildMessageReceivedEvent event);

}

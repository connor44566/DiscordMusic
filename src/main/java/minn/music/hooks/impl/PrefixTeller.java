package minn.music.hooks.impl;

import minn.music.MusicBot;
import minn.music.hooks.MentionListener;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

public class PrefixTeller implements MentionListener
{
	@Override
	public void onMention(GuildMessageReceivedEvent event)
	{
		if (event.getMessage().getRawContent().startsWith(event.getJDA().getSelfInfo().getAsMention() + " prefix"))
			event.getChannel().sendMessageAsync("My current prefix is: **" + MusicBot.config.prefix + "**", null);
	}
}

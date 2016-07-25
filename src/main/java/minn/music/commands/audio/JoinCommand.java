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

package minn.music.commands.audio;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.VoiceChannel;

public class JoinCommand extends GenericCommand
{

	private MusicBot bot;

	public JoinCommand(MusicBot bot)
	{
		this.bot = bot;
	}

	public String getAttributes()
	{
		return "<ChannelName>";
	}

	@Override
	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "join";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		VoiceChannel channel = EntityUtil.getVoiceForUser(event.author, event.guild);
		if (event.allArgs.isEmpty() && channel == null)
		{
			event.send("Usage: `" + MusicBot.config.prefix + "join <ChannelName>`");
			return;
		} else if (channel == null)
			channel = EntityUtil.getFirstVoice(event.allArgs, event.guild);
		if (channel == null)
		{
			event.send("I can't see a VoiceChannel called **" + event.allArgs + "**.");
			return;
		}
		if (event.guild.getAudioManager().isAttemptingToConnect())
		{
			event.send("I am currently trying to connect to **" + event.guild.getAudioManager().getQueuedAudioConnection().getName() + "**.");
			return;
		}
		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.VOICE_CONNECT))
		{
			event.send("I am not able to connect to **" + channel.getName() + "**.");
			return;
		}
		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.VOICE_SPEAK))
		{
			event.send("I wouldn't be able to speak in **" + channel.getName() + "**.");
			return;
		}
		if (event.guild.getAudioManager().isConnected())
			event.guild.getAudioManager().moveAudioConnection(channel);
		else event.guild.getAudioManager().openAudioConnection(channel);
		event.send("Joined `" + channel.getName() + "`.");
	}
}

package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.VoiceChannel;

public class JoinCommand extends GenericCommand
{

	private MusicBot bot;

	public JoinCommand(MusicBot bot)
	{
		this.bot = bot;
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
		if (event.allArgs.isEmpty())
		{
			event.send("Usage: `" + bot.config.prefix + "join <ChannelName>`");
			return;
		}
		VoiceChannel channel =
				event.guild.getVoiceChannels().parallelStream().filter(c -> c.getName().equals(event.allArgs)).findFirst().orElse(null);
		if (channel == null)
		{
			event.send("I can't see a VoiceChannel called **" + event.allArgs + "**.");
			return;
		}
		if (event.guild.getAudioManager().isAttemptingToConnect())
		{
			event.send("I am currently trying to connect to **" + event.guild.getAudioManager().getQueuedAudioConnection() + "**.");
			return;
		}
		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.VOICE_CONNECT))
		{
			event.send("I am not able to connect to **" + channel.getName() + "**.");
			return;
		}
		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.VOICE_SPEAK))
		{
			event.send("I wouldn't be able to speak in that channel.");
			return;
		}
		if (event.guild.getAudioManager().isConnected())
			event.guild.getAudioManager().moveAudioConnection(channel);
		else event.guild.getAudioManager().openAudioConnection(channel);
	}
}

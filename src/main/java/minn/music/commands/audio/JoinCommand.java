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
		} else
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
			event.send("I wouldn't be able to speak in that channel.");
			return;
		}
		if (event.guild.getAudioManager().isConnected())
			event.guild.getAudioManager().moveAudioConnection(channel);
		else event.guild.getAudioManager().openAudioConnection(channel);
		event.send("Joined `" + channel.getName() + "`.");
	}
}

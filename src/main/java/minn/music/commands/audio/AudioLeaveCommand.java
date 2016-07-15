package minn.music.commands.audio;

import net.dv8tion.jda.managers.AudioManager;

public class AudioLeaveCommand extends GenericAudioCommand
{
	@Override
	public String getAttributes()
	{
		return "";
	}

	@Override
	public String getAlias()
	{
		return "dc";
	}

	@Override
	public String getInfo()
	{
		return "Disconnects the bot from the current voice channel.";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		AudioManager manager = event.guild.getAudioManager();
		if (manager.isConnected() || manager.isAttemptingToConnect())
			manager.closeAudioConnection();
		else
			event.send("I am not currently connected to a voice channel. If that is wrong contact a dev.");
	}
}

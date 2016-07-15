package minn.music.commands.audio;

import minn.music.commands.GenericCommand;

public abstract class GenericAudioCommand extends GenericCommand
{

	public abstract String getAttributes();

	public abstract String getInfo();

	public boolean isPrivate()
	{
		return false;
	}

}

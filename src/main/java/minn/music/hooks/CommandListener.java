package minn.music.hooks;

import minn.music.commands.GenericCommand;

@FunctionalInterface
public interface CommandListener<Cmd extends GenericCommand>
{
	/**
	 * Called when any command that extends GenericCommand was executed.
	 *
	 * @param command
	 *          Extends GenericCommand.
	 */
	void onCommand(Cmd command);
}

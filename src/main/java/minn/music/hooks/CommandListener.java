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
	 * @param event
	 *          The fired {@link minn.music.commands.GenericCommand.CommandEvent CommandEvent}.
	 */
	void onCommand(Cmd command, GenericCommand.CommandEvent event);
}

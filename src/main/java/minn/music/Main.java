package minn.music;

import minn.music.commands.JoinCommand;
import minn.music.commands.ListCommands;
import minn.music.commands.PingCommand;
import minn.music.commands.PlayCommand;

public class Main
{

	public static void main(String... a)
	{
		new MusicBot(bot -> {
			bot.manager.registerCommand(new PingCommand());
			bot.manager.registerCommand(new JoinCommand(bot));
			bot.manager.registerCommand(new PlayCommand());
			bot.manager.registerCommand(new ListCommands(bot));

/*			bot.manager.registerCommand(new GenericCommand()
			{
				@Override
				public String getAlias()
				{
					return "invite";
				}

				@Override
				public void invoke(CommandEvent event)
				{
					event.send(String.format("**Invite: %s**", event.api.getSelfInfo().getAuthUrl()));
				}
			});*/
		});

	}

}
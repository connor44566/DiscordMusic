package minn.music;

import minn.music.commands.GenericCommand;

public class Main
{

	private static MusicBot bot;

	public static void main(String... a)
	{
		bot = new MusicBot(bot -> {
			bot.manager.registerCommand(new GenericCommand()
			{
				@Override
				public String getAlias()
				{
					return "Ping";
				}

				@Override
				public void invoke(CommandEvent event)
				{
					long time = System.currentTimeMillis();
					event.send("Pong!", m ->
					{
						m.updateMessageAsync("__**Pong:**__ " + (System.currentTimeMillis() - time) + "ms", null);
					});
				}
			});
		});

	}

}
package minn.music.commands.admin;


import minn.music.MusicBot;
import minn.music.commands.GenericCommand;

public class StreamingCommand extends GenericCommand
{

	private MusicBot bot;

	public StreamingCommand(MusicBot bot)
	{
		this.bot = bot;
	}

	public String getAttributes()
	{
		return "<game>";
	}

	@Override
	public String getAlias()
	{
		return "streaming";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(bot.config.owner))
		{
			event.send("You are not allowed to modify this property.");
			return;
		}
		String twitch = " ";
		try
		{
			twitch = (String) bot.config.get("twitch");
			if (twitch == null || twitch.isEmpty())
				twitch = " ";
		} catch (ClassCastException ignored)
		{
		}
		if (event.allArgs.isEmpty())
			event.api.getAccountManager().setGame(null);
		else
			event.api.getAccountManager().setStreaming(event.allArgs, "https://twitch.tv/" + twitch);
	}
}

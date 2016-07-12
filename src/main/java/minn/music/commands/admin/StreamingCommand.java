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
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not allowed to modify this property.");
			return;
		}
		String twitch = " ";
		try
		{
			twitch = (String) MusicBot.config.get("twitch");
			if (twitch == null || twitch.isEmpty())
				twitch = " ";
		} catch (ClassCastException ignored)
		{
		}
		String finalTwitch = twitch;
		bot.managers.parallelStream().forEach(m ->
		{
			if (event.allArgs.isEmpty())
				m.getJDA().getAccountManager().setGame(null);
			else
				m.getJDA().getAccountManager().setStreaming(event.allArgs, "https://twitch.tv/" + finalTwitch);
		});
	}
}

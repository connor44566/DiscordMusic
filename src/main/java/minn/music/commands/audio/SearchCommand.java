package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import minn.music.util.SearchUtil;

public class SearchCommand extends GenericCommand
{

	public String getInfo()
	{
		return "Used to look up given <query> on youtube. Picks first link.";
	}

	public String getAttributes()
	{
		return "<query>";
	}

	@Override
	public String getAlias()
	{
		return "search";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (event.allArgs.isEmpty())
		{
			event.send("Usage: " + getAlias() + " " + getAttributes());
			return;
		}
		event.send("Fetching...", m -> {
			if (m != null) m.updateMessageAsync(SearchUtil.searchYoutube(event.allArgs), null);
		});
	}

}

package minn.music.commands.external;

import minn.music.commands.GenericCommand;
import minn.music.util.SearchUtil;

public class GifCommand extends GenericCommand
{

	public String getAttributes()
	{
		return "<query...>";
	}

	@Override
	public String getAlias()
	{
		return "gif";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		event.send(SearchUtil.getGif(event.args));
	}
}

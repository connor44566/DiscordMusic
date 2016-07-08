package minn.music.commands.media;

import minn.music.commands.GenericCommand;
import minn.music.util.SearchUtil;

public class CatCommand extends GenericCommand
{
	@Override
	public String getAlias()
	{
		return "cat";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		event.send(SearchUtil.getCat());
	}
}

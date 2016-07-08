package minn.music.commands.external;

import minn.music.commands.GenericCommand;
import minn.music.util.SearchUtil;

/**
 * Teh only comand yu will evur need. hmmmm
 */
public class DoggoComant extends GenericCommand // toto wride docks
{
	/**
	 * If its secrot
	 * @return tru/fals valu
	 */
	@Override
	public boolean isPrivate()
	{
		return super.isPrivate();
	}

	/**
	 * infos
	 * @return teh infos
	 */
	@Override
	public String getInfo()
	{
		return "prety coll comand dat can show yu nice doggos. hmmm.";
	}

	/**
	 * atribbutes
	 * @return atrributtes
	 */
	@Override
	public String getAttributes()
	{
		return super.getAttributes();
	}

	/**
	 * Getts zhe alieas fron command.
	 * @return teh kool alias
	 */
	@Override
	public String getAlias()
	{
		return "dog";
	}

	/**
	 * stardts cool comand for u
	 * @param event A CommandEvent implementation.
	 */
	@Override
	public void invoke(CommandEvent event)
	{
		event.send(SearchUtil.getDog());
	}
}

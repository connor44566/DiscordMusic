/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.commands.media;

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

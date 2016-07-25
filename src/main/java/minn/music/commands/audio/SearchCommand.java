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

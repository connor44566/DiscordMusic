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

package minn.music.commands.memes;

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

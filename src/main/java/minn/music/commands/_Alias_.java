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

package minn.music.commands;

public class _Alias_ extends GenericCommand
{

	private String alias;
	private GenericCommand command;
	private boolean isPrivate;

	public _Alias_(String alias, GenericCommand command)
	{
		this(alias, command, false);
	}

	public _Alias_(String alias, GenericCommand command, boolean isPrivate)
	{
		assert alias != null && command != null && !alias.isEmpty();
		this.alias = alias;
		this.command = command;
		this.isPrivate = isPrivate;
	}

	@Override
	public boolean isPrivate()
	{
		return isPrivate;
	}

	@Override
	public String getAlias()
	{
		return alias;
	}

	@Override
	public void invoke(CommandEvent event)
	{
		command.invoke(event);
	}
}

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

import net.dv8tion.jda.utils.SimpleLog;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Alias being the name.
 */
public class Container extends GenericCommand
{
	protected final List<GenericCommand> items = new LinkedList<>();
	protected final String name;
	protected boolean isPrivate = false;
	protected boolean isAdmin = false;

	public Container(GenericCommand cmd, String name)
	{
		assert cmd != null && name != null && !name.isEmpty();
		items.add(cmd);
		this.name = name;
	}

	public Container(String name)
	{
		assert name != null && !name.isEmpty();
		this.name = name;
	}

	public Container setPrivate(boolean is)
	{
		isPrivate = is;
		return this;
	}

	public Container setAdmin(boolean is)
	{
		isAdmin = is;
		return this;
	}

	public String getAlias()
	{
		return name;
	}

	public String getInfo()
	{
		String s = "**" + name.toUpperCase() + "**\n```xml";
		for (GenericCommand c : items)
		{
			s += "\n> " + c.getAlias() + " " + c.getAttributes();
		}
		return s + "```";
	}

	/**
	 * Returns command fitting to given alias. Or null if none fit.
	 *
	 * @param possible alias to match
	 * @return Fitting GenericCommand or null.
	 */
	public GenericCommand getCommand(String possible)
	{
		return items.parallelStream().filter(c -> c.getAlias().equalsIgnoreCase(possible)).findFirst().orElse(null);
	}

	/**
	 * Add command to container.
	 *
	 * @param cmd commands to add
	 */
	public void addItem(GenericCommand... cmd)
	{
		assert cmd != null;
		Collections.addAll(items, cmd);
	}

	public void invoke(CommandEvent event)
	{
		// Can't be invoked.
		SimpleLog.getLog("Container").log(new IllegalAccessException("Container can't be invoked."));
	}

	public boolean isEmpty()
	{
		return items.isEmpty();
	}

	public boolean isAdmin()
	{
		return isAdmin;
	}
}

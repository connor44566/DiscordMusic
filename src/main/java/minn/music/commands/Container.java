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
}

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

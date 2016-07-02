package minn.music.commands;

public class PingCommand extends GenericCommand
{
	@Override
	public String getAlias()
	{
		return "ping";
	}

	@Override
	public void invoke(GenericCommand.CommandEvent event)
	{
		long time = System.currentTimeMillis();
		event.send("Pong!", m ->
		{
			m.updateMessageAsync("__**Pong:**__ " + (System.currentTimeMillis() - time) + "ms", null);
		});
	}
}

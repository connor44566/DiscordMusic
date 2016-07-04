package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.Scanner;

public class CmdCommand extends EvalCommand
{
	private final static SimpleLog LOG = SimpleLog.getLog("CmdCommand");

	public CmdCommand(MusicBot bot)
	{
		super(bot);
	}

	@Override
	public String getAlias()
	{
		return "cmd";
	}

	@Override
	public synchronized void invoke(CommandEvent event)
	{
		if(!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not able to use this command!");
			return;
		}
		try
		{

			// Start process
			Process p = Runtime.getRuntime().exec("cmd /c " + event.allArgs);

			// Create Stream Scanner
			Scanner sc = new Scanner(p.getInputStream());
			Scanner scErr = new Scanner(p.getErrorStream());

			// Read streams
			if (sc.hasNext())
				event.send(read(sc));
			else if(scErr.hasNext())
				event.send("ERROR: " + read(scErr));
			else
				event.send("✅");

			// Destroy Process
			p.waitFor();
			p.destroyForcibly();
			LOG.debug("Process Destroyed");
		} catch (Exception e)
		{
			LOG.log(e);
		}
	}

}

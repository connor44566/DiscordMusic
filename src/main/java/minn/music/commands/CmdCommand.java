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
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not able to use this command!");
			return;
		}
		try
		{
			// Start process
			Process p;
			if (isWin())
				p = Runtime.getRuntime().exec("cmd /c " + event.allArgs);
			else if (isUnix())
				p = Runtime.getRuntime().exec(event.allArgs);
			else if (isMac())
				p = Runtime.getRuntime().exec("sh " + event.allArgs);
			else
			{
				LOG.fatal("OS is not registered to use this command. Contact Minn about your OS.");
				return;
			}

			// Create Stream Scanner
			Scanner sc = new Scanner(p.getInputStream());
			Scanner scErr = new Scanner(p.getErrorStream());

			// Read streams
			if (sc.hasNext())
				event.send(read(sc));
			else if (scErr.hasNext())
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

	private static boolean isWin()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("win"));
	}

	private static boolean isUnix()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("nix") || os.contains("nux") || os.indexOf("aix") > 0);
	}

	private static boolean isMac()
	{
		String os = System.getProperty("os.name").toLowerCase();
		return (os.contains("mac"));
	}

	public static void main(String... a)
	{
		System.out.println(System.getProperty("os.name"));
	}

}

package minn.music.commands.code;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
	public void invoke(GenericCommand.CommandEvent event)
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
			/*ChannelListener listener = new ChannelListener(event.channel, 1, m -> { // input
				try
				{
					p.getOutputStream().write(m.getContent().getBytes());
				} catch (IOException e)
				{
					LOG.log(e);
				}
			});*/

			// Read streams
			Thread t = new Thread(() -> {
				if (sc.hasNext() || scErr.hasNext())
				{
					if (sc.hasNext())
						event.send(read(sc));
					if (scErr.hasNext())
						event.send("ERROR: " + read(scErr));
				} else
					event.send("✅");
			}, "CmdEval-Read");
			t.start();

			// Destroy Process
			if (p.waitFor(1, TimeUnit.MINUTES))
				p.destroyForcibly();
			else
			{
				p.destroyForcibly();
				LOG.warn("Process has been terminated. Exceeded time limit.");
			}
			//listener.shutdown();
			LOG.debug("Process Destroyed");
		} catch (Exception e)
		{
			event.send("Something went wrong trying to eval your query.");
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

}

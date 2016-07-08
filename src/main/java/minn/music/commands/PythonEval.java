package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.utils.SimpleLog;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class PythonEval extends EvalCommand
{
	private File f = new File("PewDie.py");
	private final static SimpleLog LOG = SimpleLog.getLog("PythonEval");

	public PythonEval(MusicBot bot) throws IOException
	{
		super(bot);
		LOG.setLevel(SimpleLog.Level.DEBUG);
	}

	public String getAlias()
	{
		return "py";
	}

	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not able to use this command!");
			return;
		}
		try
		{
			// Create Python file
			f.createNewFile();
			f.deleteOnExit();
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
			stream.write(event.allArgs.getBytes());
			stream.close();

			// Start process
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("python", f.getName());
			Process p = builder.start();

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
			}, "PythonEval-Read");
			t.start();

			// Destroy Process
			if (p.waitFor(1, TimeUnit.MINUTES))
				p.destroy();
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

}

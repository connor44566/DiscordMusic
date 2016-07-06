package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.utils.SimpleLog;

import java.io.*;
import java.util.Scanner;
import java.util.TimerTask;

public class PythonEval extends EvalCommand
{
	private File f = new File("PewDie.py");
	private final static SimpleLog LOG = SimpleLog.getLog("PythonEval");

	public PythonEval(MusicBot bot) throws IOException
	{
		super(bot);
	}

	public String getAlias()
	{
		return "py";
	}

	public synchronized void invoke(CommandEvent event)
	{
		if(!event.author.getId().equals(MusicBot.config.owner))
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

			// Read streams
			if (sc.hasNext())
				event.send(read(sc));
			if(scErr.hasNext())
				event.send("ERROR: " + read(scErr));
			else
				event.send("✅");

			// Start KeepAlive
			timer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					if (!p.isAlive())
						return;
					p.destroyForcibly();
					LOG.warn("Process has been terminated. Exceeded time limit.");
				}
			}, 3000, 100);

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

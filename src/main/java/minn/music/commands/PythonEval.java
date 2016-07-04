package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.utils.SimpleLog;

import java.io.*;
import java.util.Scanner;

public class PythonEval extends EvalCommand
{
	private File f = new File("testing.py");
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
			f.createNewFile();
			f.deleteOnExit();
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
			stream.flush();
			stream.write(event.allArgs.getBytes());
			stream.close();

			ProcessBuilder builder = new ProcessBuilder();
			builder.command("python", f.getName());
			Process p = builder.start();

			Scanner sc = new Scanner(p.getInputStream());
			Scanner scErr = new Scanner(p.getErrorStream());


			if (sc.hasNext())
				event.send(read(sc));
			else if(scErr.hasNext())
				event.send("ERROR: " + read(scErr));
			else
				event.send("Nothing returned.");

			p.destroyForcibly();
			LOG.debug("Process Destroyed");
		} catch (Exception e)
		{
			LOG.log(e);
		}
	}

	private static String read(Scanner out)
	{
		assert out != null;
		String s = "";

		while(out.hasNext() && s.length() < 1000)
		{
			s += out.nextLine() + "\n";
		}
		return s;
	}

}

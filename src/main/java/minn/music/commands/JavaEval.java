package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.utils.SimpleLog;

import java.io.*;
import java.rmi.UnexpectedException;
import java.util.Scanner;

public class JavaEval extends EvalCommand
{
	private final SimpleLog LOG = SimpleLog.getLog("JavaEval");
	private File f = new File("DontUse.java"); // Src
	private File out = new File("DontUse.class"); // Class

	public JavaEval(MusicBot bot)
	{
		super(bot);
	}

	public String getAlias()
	{
		return "java";
	}

	public synchronized void invoke(CommandEvent event)
	{

		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not able to use this command!");
			return;
		}
		try
		{
			// Create Java src file and class
			f.createNewFile();
			f.deleteOnExit();
			OutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
			stream.write(("public class " + f.getName().replace(".java", "") + " {\n\tpublic static void main(String... a)\n{\n\t" + event.allArgs + "\n\t}\n}").getBytes());
			stream.close();
			try
			{
				compile();
			} catch (UnexpectedException e) // Compilation failed with error
			{
				event.send(e.getMessage());
				return;
			}
			out.deleteOnExit();

			// Start process
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("java",  out.getName().replace(".class", ""));
			Process p = builder.start();

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

	private void compile() throws IOException, InterruptedException
	{
		LOG.debug("Starting to compile " + f.getName());
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("javac", f.getName());
		Process p = builder.start();

		Scanner sc = new Scanner(p.getInputStream());
		Scanner scErr = new Scanner(p.getErrorStream());

		if (sc.hasNext())
			LOG.debug(read(sc));
		else if (scErr.hasNext())
			throw new UnexpectedException("ERROR: " + read(scErr));
		else
			LOG.debug("✅");

		p.waitFor();
		p.destroyForcibly();
		LOG.debug("Finished compilation");
	}
}

package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.utils.SimpleLog;

import java.io.*;
import java.rmi.UnexpectedException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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

	public void invoke(CommandEvent event)
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
			stream.write(getBodyWithLines(event.allArgs).getBytes());
			stream.close();
			try
			{
				compile();
			} catch (Exception e) // Compilation failed with error
			{
				event.send(e.getMessage());
				return;
			}
			out.deleteOnExit();

			// Start process
			ProcessBuilder builder = new ProcessBuilder();
			builder.command("java", out.getName().replace(".class", ""));
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
			}, "JavaEval-Read");
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

	private void compile() throws IOException, InterruptedException
	{
		LOG.debug("Starting to compile " + f.getName());
		if (!f.exists())
			throw new UnexpectedException("Unable to compile source file.");
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

	private String getBodyWithLines(String code)
	{
		String body =
				"import java.util.*;\n" +
						"import java.math.*;\n" +
						"import java.net.*;\n" +
						"import java.io.*;\n" +
						"import java.util.concurrent.*;\n" +
						"import java.time.*;\n" +
						"import java.lang.*;\n" +
						"public class " + f.getName().replace(".java", "") + "\n{" +
						"\n\tpublic static void main(String... a) throws Exception" +
						"\n\t{\n";
		String[] lines = code.split("\n");
		for (String line : lines)
			body += "\t\t" + line /*+ ";"*/ + "\n";  // Not appending ; because it breaks if / else without {
		return body + "\n\t}\n}";
	}
}

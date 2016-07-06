package minn.music.commands;

import minn.music.MusicBot;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Scanner;
import java.util.Timer;

public class EvalCommand extends GenericCommand
{

	protected MusicBot bot;
	protected final Timer timer = new Timer(false); // Used for KeepAlive

	public EvalCommand(MusicBot bot)
	{
		this.bot = bot;
	}

	public String getAttributes()
	{
		return "<code>";
	}

	@Override
	public String getAlias()
	{
		return "eval";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if(!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not allowed to perform that command.");
			return;
		}
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("Nashorn");
		engine.put("event", event);
		engine.put("api", event.api);
		engine.put("bot", bot);
		engine.put("me", event.author);
		engine.put("guild", event.guild);
		engine.put("message", event.message);
		engine.put("channel", event.channel);
		Object o;

		try
		{
			engine.eval("EntityUtil = Java.type(\"minn.music.util.EntityUtil\")");
			engine.eval("imports = new JavaImporter(java.util, java.io, java.net)");
			o = engine.eval("(function() {with(imports) {try{" + event.allArgs + "\n}catch(ex){return ex}}})()");
		} catch (Exception e)
		{
			o = e;
		}

		if(o == null)
			o = "No exceptions.";

		event.send("**__Input:__**\n```js\n" + event.allArgs + "```\n**__Output:__ `" + o.toString() + "`**");
	}

	/**
	 * Reads given {@link java.util.Scanner} until the output reaches 1000 characters or more. Or no next is available.
	 * @param in
	 *          A Scanner to an {@link java.io.InputStream InputStream}
	 * @return A String containing the Streams contents.
	 */
	protected static String read(Scanner in)
	{
		assert in != null;
		String s = "";

		while(in.hasNext() && s.length() < 1000)
		{
			s += in.nextLine() + "\n";
		}
		return s;
	}
}

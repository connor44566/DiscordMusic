package minn.music.commands;

import minn.music.MusicBot;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class EvalCommand extends GenericCommand
{

	private MusicBot bot;

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
			engine.eval("imports = new JavaImporter(java.util, java.io, java.net, java.math)");
			o = engine.eval("(function() {with(imports) {" + event.allArgs + "\n}})()");
		} catch (Exception e)
		{
			o = e;
		}

		if(o == null)
			o = "No exceptions.";

		event.send("**__Input:__**\n```js\n" + event.allArgs + "```\n**__Output:__ " + o.toString() + "**");
	}
}

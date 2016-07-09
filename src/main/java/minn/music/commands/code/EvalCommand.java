package minn.music.commands.code;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.events.Event;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.utils.SimpleLog;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Consumer;

public class EvalCommand extends GenericCommand
{

	protected MusicBot bot;
	public static final SimpleLog LOG = SimpleLog.getLog("EvalModule");

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
		if (!event.author.getId().equals(MusicBot.config.owner))
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

		if (o == null || o.toString().isEmpty())
			o = "No exceptions.";

		event.send("**__Input:__**\n```js\n" + (event.allArgs.isEmpty() ? "-" : event.allArgs) + "```\n**__Output:__ `" + o.toString() + "`**");
	}

	/**
	 * Reads given {@link java.util.Scanner} until the output reaches 1000 characters or more. Or no next is available.
	 *
	 * @param in A Scanner to an {@link java.io.InputStream InputStream}
	 * @return A String containing the Streams contents.
	 */
	protected static String read(Scanner in)
	{
		assert in != null;
		String s = "";

		try
		{
			while (in.hasNext() && s.length() < 1000)
			{
				s += in.nextLine() + "\n";
			}
		} catch (IllegalStateException | NoSuchElementException ignored)
		{
		}
		return s;
	}

	/**
	 * Could potentially be used to interact with a sub-process' OutputStream.
	 */
	protected class ChannelListener implements EventListener
	{
		protected boolean isAlive = true;
		protected String id;
		protected int counter;
		protected JDA api;
		protected Consumer<Message> messageConsumer;

		/**
		 * Creates a new instance of ChannelListener.
		 *
		 * @param id    Of MessageChannel
		 * @param count Of messages to listen to until {@link ChannelListener#shutdown()} is called automatically.
		 */
		protected ChannelListener(String id, int count, JDA api, Consumer<Message> messageConsumer)
		{
			assert id != null && !id.isEmpty() && api != null && count > 0 && messageConsumer != null;
			this.messageConsumer = messageConsumer;
			this.id = id;
			this.api = api;
			api.addEventListener(this);
		}

		protected ChannelListener(MessageChannel channel, int count, Consumer<Message> messageConsumer)
		{
			this(channel.getId(), count, channel.getJDA(), messageConsumer);
		}

		@Override
		public synchronized void onEvent(Event event)
		{
			if (event instanceof MessageReceivedEvent)
			{
				if (((MessageReceivedEvent) event).getChannel().getId().equals(id) && ((MessageReceivedEvent) event).getAuthor().getId().equals(MusicBot.config.owner))
				{
					if (messageConsumer != null) messageConsumer.accept(((MessageReceivedEvent) event).getMessage());
					if (--counter <= 0)
						shutdown();
					LOG.debug("Input: " + ((MessageReceivedEvent) event).getMessage().getContent());
				}
			}
		}

		/**
		 * Convenience method.
		 */
		public synchronized void shutdown()
		{
			if (!isAlive)
				return;
			api.removeEventListener(this);
			LOG.debug("Channel closed.");
			isAlive = false;
		}
	}
}

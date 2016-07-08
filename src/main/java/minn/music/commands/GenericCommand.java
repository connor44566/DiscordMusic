package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.function.Consumer;

public abstract class GenericCommand
{
	/**
	 * Returns true if the command is allowed in private channels.
	 *
	 * @return boolean
	 * Default true.
	 */
	public boolean isPrivate()
	{
		return true;
	}

	/**
	 * Used to find commands.
	 *
	 * @return alias
	 */
	public abstract String getAlias();

	/**
	 * Used to get info about the command.
	 * @return Info
	 */
	public String getInfo()
	{
		return getAlias() + " " + getAttributes();
	}

	/**
	 * Must be implemented by sub-class.
	 * @param event A CommandEvent implementation.
	 */
	public abstract void invoke(CommandEvent event);

	/**
	 * Attributes this command requires.
	 * @return String
	 */
	public String getAttributes()
	{
		return "";
	}

	/**
	 * Wrapper to allow shortcuts.
	 */
	public static class CommandEvent
	{

		private final static SimpleLog LOG = SimpleLog.getLog("CommandSender");

		/**
		 * Used if it allows private and guild invokes.
		 */
		public MessageReceivedEvent messageEvent;

		/**
		 * Used if it doesn't allow private invokes.
		 */
		public GuildMessageReceivedEvent guildEvent;


		public final boolean isPrivate;
		public final MessageChannel channel;
		public final Guild guild;
		public final JDA api;
		public final User author;
		public final Message message;

		public final String allArgs;
		public final String[] args;


		public CommandEvent(MessageReceivedEvent event)
		{
			channel = event.getChannel();
			guild = event.getGuild();
			api = event.getJDA();
			author = event.getAuthor();
			message = event.getMessage();
			isPrivate = event.isPrivate();

			String trimmed = event.getMessage().getRawContent().substring(MusicBot.config.prefix.length()).trim();
			String[] parts = trimmed.split("\\s+", 2);

			if(parts.length > 1)
			{
				this.allArgs = parts[1];
				this.args = allArgs.split("\\s+");
			} else
			{
				this.allArgs = "";
				this.args = new String[0];
			}

			messageEvent = event;
		}

		public CommandEvent(GuildMessageReceivedEvent event)
		{
			channel = event.getChannel();
			guild = event.getGuild();
			api = event.getJDA();
			author = event.getAuthor();
			message = event.getMessage();
			isPrivate = false;

			String trimmed = event.getMessage().getRawContent().substring(MusicBot.config.prefix.length()).trim();
			String[] parts = trimmed.split("\\s+", 2);

			if(parts.length > 1)
			{
				this.allArgs = parts[1];
				this.args = allArgs.split("\\s+");
			} else
			{
				this.allArgs = "";
				this.args = new String[0];
			}

			guildEvent = event;
		}

		public void send(String message)
		{
			send(message, null);
		}

		public void send(String message, Consumer<Message> callback)
		{
			try
			{
				message = message.replace(MusicBot.config.token, "<place token here>").replace("@everyone", "@\u0001everyone").replace("@here", "@\u0001here");
				channel.sendMessageAsync(message, callback);
			} catch (Exception e)
			{
				LOG.warn("A message was not sent due to " + e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		}

	}

}

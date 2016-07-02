package minn.music.commands;

import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;

import java.util.function.Consumer;

public abstract class GenericCommand
{
	/**
	 * Returns true if the command is allowed in private channels.
	 * @return boolean
	 *          Default true.
	 */
	public boolean isPrivate()
	{
		return true;
	}

	/**
	 * Used to find commands.
	 * @return alias
	 */
	public abstract String getAlias();

	public abstract void invoke(CommandEvent event);

	/**
	 * Wrapper to allow shortcuts.
	 */
	public static class CommandEvent
	{
		/**
		 * Used if it allows private and guild invokes.
		 */
		public MessageReceivedEvent messageEvent;

		/**
		 * Used if it doesn't allow private invokes.
		 */
		public GuildMessageReceivedEvent guildEvent;

		public final MessageChannel channel;
		public final Guild guild;
		public final JDA api;
		public final User author;
		public final Message message;

		public CommandEvent(MessageReceivedEvent event)
		{
			// TODO: implement
			channel = event.getChannel();
			guild = event.getGuild();
			api = event.getJDA();
			author = event.getAuthor();
			message = event.getMessage();

			messageEvent = event;
		}

		public CommandEvent(GuildMessageReceivedEvent event)
		{
			// TODO: implement
			channel = event.getChannel();
			guild = event.getGuild();
			api = event.getJDA();
			author = event.getAuthor();
			message = event.getMessage();

			guildEvent = event;
		}

		public void send(String message)
		{
			send(message, null);
		}

		public void send(String message, Consumer<Message> callback)
		{
			try {
				channel.sendMessageAsync(message, callback);
			} catch (Exception ignored)
			{

			}
		}

	}

}

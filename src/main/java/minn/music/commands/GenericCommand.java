/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
	 *
	 * @return Info
	 */
	public String getInfo()
	{
		return getAlias() + " " + getAttributes();
	}

	/**
	 * Must be implemented by sub-class.
	 *
	 * @param event A CommandEvent implementation.
	 */
	public abstract void invoke(CommandEvent event);

	/**
	 * Attributes this command requires.
	 *
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


		public CommandEvent(MessageReceivedEvent event, String trimmed)
		{
			channel = event.getChannel();
			guild = event.getGuild();
			api = event.getJDA();
			author = event.getAuthor();
			message = event.getMessage();
			isPrivate = event.isPrivate();

			String[] parts = trimmed.split("\\s+", 2);

			if (parts.length > 1)
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

		public CommandEvent(GuildMessageReceivedEvent event, String trimmed)
		{
			channel = event.getChannel();
			guild = event.getGuild();
			api = event.getJDA();
			author = event.getAuthor();
			message = event.getMessage();
			isPrivate = false;

			String[] parts = trimmed.split("\\s+", 2);

			if (parts.length > 1)
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
				message = message.replace(MusicBot.config.token, "<place token here>").replace("@everyone", "@\u0001everyone").replace("@here", "@\u0001here"); // no mass mentions or token
				channel.sendMessageAsync(message, msg ->
				{
					if (callback != null) new Thread(() ->
					{
						try
						{
							callback.accept(msg);
						} catch (Exception e)
						{
							SimpleLog.getLog("SenderThread").fatal(e);
						}
					}, "Async Callback Accept").start();
				}); // async
			} catch (Exception e)
			{
				LOG.warn("A message was not sent due to " + e.getClass().getSimpleName() + ": " + e.getMessage());
				if (callback != null) new Thread(() -> callback.accept(null), "Async Callback Accept").start();
			}
		}

	}

	public String toString()
	{
		return getAlias() + "(" + getAttributes() + ")";
	}

}

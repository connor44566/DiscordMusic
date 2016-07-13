package minn.music.commands.audio;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioSource;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class PlayerCommand extends GenericCommand
{
	private MusicBot bot;
	private List<PlayerProperty> properties = new LinkedList<>();

	public boolean isPrivate()
	{
		return false;
	}

	public PlayerCommand(MusicBot bot)
	{
		this.bot = bot;

		properties.add(new PlayerProperty("shuffle", (input, guild) ->
		{
			try
			{
				MusicPlayer player = (MusicPlayer) guild.getAudioManager().getSendingHandler();
				if (player == null)
				{
					throw new ClassCastException("Player is not available.");
				}
				Collections.shuffle(player.getAudioQueue());
				throw new NullPointerException("Queue has been shuffled.");
			} catch (ClassCastException e)
			{
				throw new ClassCastException("Player is not available.");
			}
		}));

		properties.add(new PlayerProperty("volume", (input, guild) ->
		{
			try
			{
				float vol = Float.parseFloat(input);
				MusicPlayer player = (MusicPlayer) guild.getAudioManager().getSendingHandler();
				if (player == null)
				{
					player = new MusicPlayer();
					guild.getAudioManager().setSendingHandler(player);
				}
				vol = Math.min(Math.max(0, vol), 1); // Ensure 0 - 1
				player.setVolume(vol);
				throw new NullPointerException("**Volume: " + vol + "**");
			} catch (NumberFormatException e)
			{
				throw new NullPointerException("Invalid input. Example vol 0.5");
			} catch (ClassCastException e)
			{
				throw new ClassCastException("Player is not available.");
			}
		}));

		properties.add(new PlayerProperty("skip", (input, guild) ->
		{
			try
			{
				MusicPlayer player = (MusicPlayer) guild.getAudioManager().getSendingHandler();
				if (player == null)
				{
					throw new NullPointerException("No queued songs.");
				}

				try
				{
					int amount = Integer.parseInt(input);
					int i = 0;
					for (i = 0; i < amount - 1 && i < player.getAudioQueue().size() - 1; i++)
					{
						player.getAudioQueue().remove();
					}
					i++;
					player.skipToNext();
					throw new NullPointerException("Skipped " + i + " song" + ((i == 1) ? "" : "s") + ".");
				} catch (NumberFormatException e)
				{
					player.skipToNext();
					throw new NullPointerException("Skipped current song.");
				}
			} catch (ClassCastException e)
			{
				throw new ClassCastException("Player is not available.");
			}
		}));

		properties.add(new PlayerProperty("leave", (input, guild) ->
		{
			if (guild.getAudioManager().isConnected())
			{
				guild.getAudioManager().closeAudioConnection();
				throw new NullPointerException("Disconnected.");
			} else
				throw new IllegalArgumentException("I'm not currently connected.");
		}));

		properties.add(new PlayerProperty("remove", (input, guild) ->
		{
			MusicPlayer player = (MusicPlayer) guild.getAudioManager().getSendingHandler();
			if (player == null)
				throw new NullPointerException("Your player has no queue songs.");
			try
			{
				if (input.isEmpty())
					throw new NumberFormatException();
				int index = Integer.parseInt(input) - 1;
				if (index >= player.getAudioQueue().size() || index < 0)
					throw new IndexOutOfBoundsException("Index is not listed.");
				AudioSource source = player.getAudioQueue().get(index);
				player.getAudioQueue().remove(index);
				throw new NullPointerException("Removed **" + source.getInfo().getTitle() + "** from the queue.");
			} catch (NumberFormatException e)
			{
				throw new NumberFormatException("Invalid input. Usage: `remove <index>`.\nExample: `remove 5`");
			}

		}));
	}

	public String getInfo()
	{
		String s = "player <property> [input]\n\nProperties:```xml";
		for (PlayerProperty p : properties)
		{
			s += "\n> " + p.name;
		}
		return (s + "```");
	}

	public String getAttributes()
	{
		return "<property> [input]";
	}

	@Override
	public String getAlias()
	{
		return "player";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (event.allArgs.isEmpty())
		{
			String s = "**__Properties:__**\n";
			for (PlayerProperty p : properties)
			{
				s += String.format("\n`%s` ", p.name);
			}
			event.send(s);
			return;
		}

		String[] parts = event.allArgs.split("\\s+", 2);
		for (PlayerProperty p : properties)
		{
			if (p.name.equalsIgnoreCase(event.args[0]))
			{
				String response = p.invoke((parts.length == 2) ? parts[1] : "", event.guild);
				if (response != null && !response.isEmpty())
					event.send(response);
				return;
			}
		}
	}

	private class PlayerProperty
	{
		final String name;
		final BiConsumer<String, Guild> invokable;

		public PlayerProperty(String name, BiConsumer<String, Guild> invokable)
		{
			this.name = name;
			this.invokable = invokable;
		}

		public String invoke(String input, Guild guild)
		{
			try
			{
				invokable.accept(input, guild);
			} catch (Exception e)
			{
				return e.getMessage();
			}
			return ":ok_hand::skin-tone-3:";
		}
	}
}

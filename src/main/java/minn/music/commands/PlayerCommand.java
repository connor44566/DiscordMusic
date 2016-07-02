package minn.music.commands;

import minn.music.MusicBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.player.MusicPlayer;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class PlayerCommand extends GenericCommand
{
	private MusicBot bot;
	private List<PlayerProperty> properties = new LinkedList<>();

	public PlayerCommand(MusicBot bot)
	{
		this.bot = bot;

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
				if (player == null || player.getAudioQueue().isEmpty())
				{
					throw new NullPointerException("No queued songs.");
				}

				player.skipToNext();
				throw new NullPointerException("Skipped current song.");
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
			String s = "**__Properties:__**\n```xml";
			for (PlayerProperty p : properties)
			{
				s += "\n" + p.name;
			}
			event.send(s + "```");
			return;
		}

		String[] parts = event.allArgs.split("\\s+", 2);
		for (PlayerProperty p : properties)
		{
			if (p.name.equalsIgnoreCase(event.args[0]))
			{
				p.invoke((parts.length == 2) ? parts[1] : "", event.guild);
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

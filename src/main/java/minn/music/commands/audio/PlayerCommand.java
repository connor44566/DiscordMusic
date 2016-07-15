package minn.music.commands.audio;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import net.dv8tion.jda.entities.Guild;

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
			throw new UnsupportedOperationException("Please use `shuffle` instead.");
		}));

		properties.add(new PlayerProperty("volume", (input, guild) ->
		{
			throw new UnsupportedOperationException("Please use `volume [float]` instead.");
		}));

		properties.add(new PlayerProperty("skip", (input, guild) ->
		{
			throw new UnsupportedOperationException("Please use `skip [amount]` instead.");
		}));

		properties.add(new PlayerProperty("leave", (input, guild) ->
		{
			throw new UnsupportedOperationException("Please use `dc` instead.");
		}));

		properties.add(new PlayerProperty("remove", (input, guild) ->
		{
			throw new UnsupportedOperationException("Please use `songremove <index>` instead.");
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

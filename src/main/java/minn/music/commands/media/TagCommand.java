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

package minn.music.commands.media;

import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.util.PersistenceUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.ShutdownEvent;
import net.dv8tion.jda.hooks.EventListener;

import java.util.HashMap;

public class TagCommand extends GenericCommand
{
	private static HashMap<String, String> tagMap = new HashMap<>();
	private static boolean init = false;

	private synchronized static void init(MusicBot bot)
	{
		if (init)
			return;
		init = true;
		Object object = PersistenceUtil.retrieve("tagMap");
		if (object != null)
			tagMap = (HashMap<String, String>) object;
		bot.managers.get(0).getJDA().addEventListener((EventListener) event ->
		{
			if (event instanceof ShutdownEvent)
			{
				PersistenceUtil.save(tagMap, "tagMap");
			}
		});
	}

	public TagCommand(MusicBot bot)
	{
		init(bot);
	}

	enum Mode
	{
		EDIT
				{
					@Override
					public void invoke(String[] args, String allArgs)
					{
						if (args.length < 2)
							throw new IllegalArgumentException("Usage: edit <key> <value>");
						String key = args[0];
						String input = allArgs.split("\\s+", 3)[2];
						if (!tagMap.containsKey(key))
							throw new IllegalArgumentException("There is no such tag.");
						tagMap.put(key, input);
						PersistenceUtil.save(tagMap, "tagMap");
					}
				},
		ADD
				{
					@Override
					public void invoke(String[] args, String allArgs)
					{
						if (args.length < 2)
							throw new IllegalArgumentException("Usage: add <key> <value>");
						String key = args[0];
						if (key.equalsIgnoreCase("add") || key.equalsIgnoreCase("delete") || key.equalsIgnoreCase("edit"))
							throw new IllegalArgumentException("Illegal key name.");
						String input = allArgs.split("\\s+", 3)[2];
						if (tagMap.containsKey(key))
							throw new IllegalArgumentException("Tag with given key already exists.");
						tagMap.put(key, input);
						PersistenceUtil.save(tagMap, "tagMap");
					}
				},
		DELETE
				{
					@Override
					public void invoke(String[] args, String allArgs)
					{
						if (args.length < 1)
							throw new IllegalArgumentException("Usage: delete <key>");
						String key = args[0];
						if (!tagMap.containsKey(key))
							throw new IllegalArgumentException("There is no such tag.");
						tagMap.remove(key);
						PersistenceUtil.save(tagMap, "tagMap");
					}
				};

		public static CharSequence[] toArray()
		{
			String[] arr = new String[values().length];
			for (int i = 0; i < arr.length; i++)
			{
				arr[i] = values()[i].toString();
			}
			return arr;
		}

		public abstract void invoke(String[] args, String allArgs);

		public String toString()
		{
			return name();
		}
	}

	public boolean isPrivate()
	{
		return false;
	}

	public String getAttributes()
	{
		return "<mode/tag> [input]";
	}

	public String getInfo()
	{
		String s = "";
		for (Mode m : Mode.values())
		{
			s += "\n>" + m.toString();
		}
		return "Another tag command :thinking:\n Modes: " + s;
	}

	@Override
	public String getAlias()
	{
		return "tag";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (event.allArgs.isEmpty())
		{
			StringBuilder b = new StringBuilder("Tags: ");
			new HashMap<>(tagMap).forEach((key, val) ->
					b.append("`").append(key).append("` "));
			event.send(b.toString());
			return;
		}
		if (event.args.length > 1)
		{
			String mode = event.args[0].toUpperCase();
			for (Mode m : Mode.values())
			{
				if (!mode.equals(m.toString()))
					continue;
				/*if (!canModifyTags(event.author, event.guild)) // Restrictions
				{
					event.send("You need the role `Tag Manager` in order to modify tags.");
					return;
				}*/
				String[] arr = new String[event.args.length - 1];
				System.arraycopy(event.args, 1, arr, 0, arr.length);
				try
				{
					m.invoke(arr, event.allArgs);
					event.send("\uD83D\uDC4C\uD83C\uDFFD");
				} catch (IllegalArgumentException e)
				{
					event.send(e.getMessage());
				}
				return;
			}
		}
		if (!tagMap.containsKey(event.args[0]))
		{
			event.send("No tag with that name found.");
			return;
		}
		event.send(String.format("`%s`: %s", event.args[0], tagMap.get(event.args[0])));
	}

	public boolean canModifyTags(User user, Guild guild)
	{
		return !(user == null || guild == null)
				&& (guild.getRolesForUser(user).parallelStream().filter(r -> r.getName().equalsIgnoreCase("Tag Manager")).findFirst().orElse(null) != null
				|| guild.getOwnerId().equals(user.getId())
				|| MusicBot.config.owner.equals(user.getId()));
	}

}

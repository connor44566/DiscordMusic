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

package minn.music.commands.settings;

import minn.music.commands.GenericCommand;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.utils.PermissionUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TodoCommand extends GenericCommand
{

	public String getInfo()
	{
		return "Creates/Writes to a todo list in a channel with the name #todo.\n" +
				"Setup: Create a channel with the name #todo and give the bot send/read access. (Including read history).\n" +
				"Only users with **MANAGE_SERVER** have access to the todo list.";
	}

	public String getAttributes()
	{
		return "<method> [input]";
	}

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "todo";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!PermissionUtil.checkPermission(event.author, Permission.MANAGE_SERVER, event.guild))
		{
			event.send("You can't access the todo list without **MANAGE_SERVER** permission.");
			return;
		}
		TextChannel channel = EntityUtil.getFirstText("todo", event.guild);
		if (channel == null)
		{
			event.send("Please create a channel named #todo. And give me full access to it.");
			return;
		}

		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.MESSAGE_HISTORY))
		{
			event.send("I can't read the message history in " + channel.getAsMention() + ".");
			return;
		}
		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.MESSAGE_READ))
		{
			event.send("I can't read the messages sent in " + channel.getAsMention() + ".");
			return;
		}
		if (!channel.checkPermission(event.api.getSelfInfo(), Permission.MESSAGE_WRITE))
		{
			event.send("I can't send a message in " + channel.getAsMention() + ".");
			return;
		}

		List<Message> untouched = channel
				.getHistory()
				.retrieve(10);
		List<Message> hist = (untouched == null ? new LinkedList<Message>() : untouched)
				.parallelStream()
				.filter(m -> m.getAuthor() == event.api.getSelfInfo())
				.collect(Collectors.toList());

		try
		{
			todo(event.args, event.allArgs, hist, channel);
		} catch (IllegalArgumentException e)
		{
			event.send(e.getMessage());
		}
	}

	private synchronized void todo(String[] args, String allArgs, List<Message> hist, TextChannel channel)
	{
		List<String> lines = new LinkedList<>();
		String[][] chunks = new String[0][];
		if (hist == null)
			hist = new LinkedList<>();
		else
		{
			Collections.reverse(hist);
			chunks = getChunks(hist);
			for (String[] chunk : chunks) Collections.addAll(lines, chunk);
		}
		switch (args[0].toLowerCase())
		{
			default:
			{
				throw new IllegalArgumentException("Invalid input. Method **" + args[0] + "** undefined. (Add/Remove/Edit/Strike/Clear)");
			}
			case "empty":
			case "clear":
			{

				for (Iterator<Message> it = hist.iterator(); it.hasNext(); )
				{
					it.next().deleteMessage();
					if (it.hasNext())
						try
						{
							Thread.sleep(2000);
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					else break;
				}
				break;
			}
			case "insert":
			case "add":
			{
				if (args.length < 2)
					throw new IllegalArgumentException("Missing parameters. Read command info for instructions.");
				if (hitLimit(lines))
					throw new IllegalArgumentException("Your todo list has hit the cap, soz fam.");
				String message = allArgs.split("\\s+", 2)[1].replaceAll("(\n|~~)", "");
				if (message == null)
					throw new IllegalArgumentException("Message has no actual content.");
				lines.add(message);
				if (lines.size() % 10 == 1 || hist.isEmpty())
					channel.sendMessageAsync(lines.size() + ") " + message, null);
				else
					hist.get(hist.size() - 1).updateMessageAsync(String.join("\n", (CharSequence[]) chunks[chunks.length - 1]) + "\n" + lines.size() + ") " + message, null);
				break;
			}
			case "update":
			case "alter":
			case "edit":
			{
				if (args.length < 3)
					throw new IllegalArgumentException("Missing parameters. Read command info for instructions.");
				int index = getIndex(args[1]);
				if (index < 0 || index >= lines.size())
					throw new IllegalArgumentException("Index is out of range (1-" + lines.size() + ").");
				int chunkID = index / 10; // Affected chunk
				int chunkLine = index % 10; // Affected Line in chunk
				Message m = hist.get(chunkID);
				String message = allArgs.split("\\s+", 3)[2].replaceAll("(\n|~~)", "");
				chunks[chunkID][chunkLine] = lines.get(index).replaceAll("^((?:~~)?\\d+\\) ).+$", "$1" + message);
				String newContent = String.join("\n", (CharSequence[]) chunks[chunkID]);
				m.updateMessageAsync(newContent, null);
				break;
			}
			case "delete":
			case "rm":
			case "remove":
			{
				if (args.length < 2)
					throw new IllegalArgumentException("Missing parameters. Read command info for instructions.");
				int index = getIndex(args[1]);
				if (index < 0 || index >= lines.size())
					throw new IllegalArgumentException("Index is out of range (1-" + lines.size() + ").");
				int chunkID = index / 10;
				List<Message> affectedMessages = hist.subList(chunkID, hist.size());
				lines.remove(index);
				List<String> prevLines = lines.subList(chunkID * 10, index);
				List<String> newLines = lines.subList(index, lines.size());
				List<String> updatedLines = newLines
						.stream()
						.map(newLine -> newLine.replaceAll("(~~)?(\\d+)(.+)", String.format("$1%d$3", Integer.parseInt(newLine.replaceAll("(?:~~)?(\\d+).+", "$1")) - 1)))
						.collect(Collectors.toCollection(LinkedList::new));
				for (Message m : affectedMessages)
				{
					if (!prevLines.isEmpty())
					{
						List<String> newContent = new LinkedList<>(prevLines);
						prevLines.clear();
						while (!updatedLines.isEmpty() && newContent.size() < 10)
						{
							newContent.add(updatedLines.get(0));
							updatedLines.remove(0);
						}
						String content = "";
						for (int i = 0; i < newContent.size(); i++)
						{
							if (i == 0)
								content += newContent.get(i);
							else content += "\n" + newContent.get(i);
						}
						m.updateMessageAsync(content, null);
					} else
					{
						List<String> newContent = new LinkedList<>();
						while (!updatedLines.isEmpty() && newContent.size() < 10)
						{
							newContent.add(updatedLines.get(0));
							updatedLines.remove(0);
						}
						String content = "";
						for (int i = 0; i < newContent.size(); i++)
						{
							if (i == 0)
								content += newContent.get(i);
							else content += "\n" + newContent.get(i);
						}
						m.updateMessageAsync(content, null);
					}
				}
				break;
			}
			case "mark":
			case "check":
			case "strike":
			{
				if (args.length < 2)
					throw new IllegalArgumentException("Missing parameters. Read command info for instructions.");
				int index = getIndex(args[1]);
				if (index < 0 || index >= lines.size())
					throw new IllegalArgumentException("Index is out of range (1-" + lines.size() + ").");
				int chunkID = index / 10;
				int lineOfChunk = index % 10;

				Message msg = hist.get(chunkID);
				if (!chunks[chunkID][lineOfChunk].matches("~~.+~~"))
					chunks[chunkID][lineOfChunk] = "~~" + chunks[chunkID][lineOfChunk] + "~~";
				else
					chunks[chunkID][lineOfChunk] = chunks[chunkID][lineOfChunk].replaceAll("~~(.+)~~", "$1");
				msg.updateMessageAsync(String.join("\n", (CharSequence[]) chunks[chunkID]), null);
				break;
			}
		}
	}

	private int getIndex(String arg)
	{
		if (!arg.matches("\\d+"))
			throw new IllegalArgumentException("Invalid index. Must be an integer.");
		return Integer.parseInt(arg) - 1;
	}

	private boolean hitLimit(List<String> lines)
	{
		if (lines.isEmpty())
			return false;
		int index = getIndex(lines.get(0).replaceAll("(?:~~)?(\\d+)\\).+", "$1"));
		return index != 0;
	}

	private String[][] getChunks(List<Message> hist)
	{
		String[][] chunks = new String[hist.size()][];
		for (int i = 0; i < chunks.length; i++)
			chunks[i] = hist.get(i).getRawContent().split("\n");
		return chunks;
	}

}

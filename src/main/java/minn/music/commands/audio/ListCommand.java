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

package minn.music.commands.audio;

import minn.music.commands.GenericCommand;
import minn.music.util.PersistenceUtil;
import minn.music.util.TimeUtil;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class ListCommand extends GenericCommand
{

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "list";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		event.send("Fetching...", m ->
		{
			if (m == null)
				return;
			MusicPlayer player = null;
			long total = 0L;
			try
			{
				player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			} catch (ClassCastException ignored)
			{
			}
			if (player == null)
			{
				m.updateMessageAsync("No queued songs.", null);
				return;
			}
			if (player.getCurrentAudioSource() == null && player.getAudioQueue().isEmpty())
			{
				m.updateMessageAsync("No queued songs.", null);
				return;
			}
			String response = "__Current:__ " +
					"`[" + player.getCurrentTimestamp().getTimestamp() + "/" + player.getCurrentAudioSource().getInfo().getDuration().getTimestamp() + "]` ** " +
					player.getCurrentAudioSource().getInfo().getTitle().replaceAll("[*]{2}", "\\*\\*") + "**";

			total += player.getCurrentAudioSource().getInfo().getDuration().getTotalSeconds() - player.getCurrentTimestamp().getTotalSeconds();

			if (!player.getAudioQueue().isEmpty())
			{
				StringBuilder b = new StringBuilder("\n__Queue: **").append(player.getAudioQueue().size()).append("** entries.__\n");
				for (int i = 0; i < player.getAudioQueue().size(); i++)
				{
					AudioInfo info = player.getAudioQueue().get(i).getInfo();
					if (i < 5)
						b.append("\n**").append(i + 1).append(")** `[").append(info.getDuration().getTimestamp()).append("]` ").append(info.getTitle().replaceAll("[*]{2}", "\\*\\*"));
					total += info.getDuration().getTotalSeconds();
				}
				if (player.getAudioQueue().size() > 5)
				{
					b.append("\n...");
				}
				response += b.toString();
			}
			response += "\n" + (player.getAudioQueue().size() > 5 ? "\nFull list at: " + getDocument(player, event.guild) : "") + "\n" + TimeUtil.time(total) + " left.";

			m.updateMessageAsync(response, null);
		});
	}

	private String getDocument(MusicPlayer player, Guild guild)
	{
		assert player != null;
		AudioSource prev = player.getPreviousAudioSource();
		AudioSource curr = player.getCurrentAudioSource();
		List<AudioSource> queue = player.getAudioQueue();
		String content = "Created at: " + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/dd/MM hh:mm:ss a")) + " © MinnDevelopment\n\nPlaylist for: " + guild.getName() + "\n";
		if (prev != null)
			content += "\nPrevious song: " + prev.getInfo().getTitle();
		if (curr != null)
			content += "\nCurrent song: " + curr.getInfo().getTitle() + " [" + player.getCurrentTimestamp().getTimestamp()  +"/" + curr.getInfo().getDuration().getTimestamp() + "]";
		if (!queue.isEmpty())
		{
			int spaces = ("" + queue.size()).length();
			content += "\n\n\nQueue:\n";
			int i = 0;
			StringBuilder b = new StringBuilder(content);
			for (AudioSource s : new LinkedList<>(queue))
			{
				b.append("[").append(adjust(++i, spaces)).append("] [").append(s.getInfo().getDuration().getTimestamp()).append("] ").append(s.getInfo().getTitle()).append("\n");
			}
			content = b.toString();
		}

		try
		{
			return PersistenceUtil.hastebin(content) + ".dos";
		} catch (Exception e)
		{
			return "*document unavailable*";
		}
	}

	private String adjust(int index, int amount)
	{
		String s = "" + index;
		while (s.length() < amount)
		{
			s = " " + s;
		}
		return s;
	}


}

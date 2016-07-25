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

import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioSource;

public class RemoveSongCommand extends GenericAudioCommand
{
	@Override
	public String getAlias()
	{
		return "songremove";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
		if (player == null)
		{
			event.send("Your player has no queue songs.");
			return;
		}
		try
		{
			int index = Integer.parseInt(event.args[0]) - 1;
			if (index >= player.getAudioQueue().size() || index < 0)
			{
				event.send("Index is not listed.");
				return;
			}
			AudioSource source = player.getAudioQueue().get(index);
			player.getAudioQueue().remove(index);
			event.send("Removed **" + source.getInfo().getTitle() + "** from the queue.");
		} catch (NumberFormatException | IndexOutOfBoundsException e)
		{
			event.send("Invalid input. Usage: `remove <index>`.\nExample: `remove 5`");
		}
	}

	@Override
	public String getAttributes()
	{
		return "<index>";
	}

	@Override
	public String getInfo()
	{
		return "Removes the entry in the current queue fitting to given <index>.";
	}
}

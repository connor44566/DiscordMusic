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

import java.util.Collections;

public class PlayerShuffleCommand extends GenericAudioCommand
{
	@Override
	public String getAttributes()
	{
		return "";
	}

	@Override
	public String getAlias()
	{
		return "shuffle";
	}

	@Override
	public String getInfo()
	{
		return "Shuffles current queue.";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		try
		{
			MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			if (player == null)
				event.send("Queue is empty.");
			else
			{
				Collections.shuffle(player.getAudioQueue());
				event.send("Queue has been shuffled.");
			}
		} catch (ClassCastException e)
		{
			event.send("Player is not available.");
		}
	}
}

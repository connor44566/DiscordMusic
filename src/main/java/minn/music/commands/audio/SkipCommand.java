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

public class SkipCommand extends GenericAudioCommand
{

	public String getAttributes()
	{
		return "[amount/all]";
	}

	public String getInfo()
	{
		return "Skips [amount/all] songs. (Including current in amount)";
	}

	@Override
	public String getAlias()
	{
		return "skip";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		try
		{
			MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			if (player == null)
			{
				event.send("No queued songs.");
				return;
			} else if (event.allArgs.equalsIgnoreCase("all"))
			{
				player.getAudioQueue().clear();
				player.skipToNext();
				event.send("Skipped all songs.");
				return;
			}
			try
			{
				int amount = Integer.parseInt(event.args[0]);
				int i;
				for (i = 0; i < amount - 1 && !player.getAudioQueue().isEmpty(); i++)
				{
					player.getAudioQueue().remove();
				}
				player.skipToNext();
				event.send("Skipped " + ++i + " song" + ((i == 1) ? "" : "s") + ".");
			} catch (IndexOutOfBoundsException e)
			{
				player.skipToNext();
				event.send("Skipped current song.");
			} catch (NumberFormatException e)
			{
				event.send("Invalid amount. Must be an integer value. (i.e. 2 or 4 and not 2.4)");
			}
		} catch (ClassCastException e)
		{
			event.send("Player is not available.");
		}
	}
}

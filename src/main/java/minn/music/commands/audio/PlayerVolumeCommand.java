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

public class PlayerVolumeCommand extends GenericAudioCommand
{
	@Override
	public String getAttributes()
	{
		return "[float]";
	}

	@Override
	public String getAlias()
	{
		return "volume";
	}

	@Override
	public String getInfo()
	{
		return "Sets the player's volume to given float value. (Example float value: 0.5)";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		try
		{
			MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
			if (player == null)
			{
				player = new MusicPlayer();
				event.guild.getAudioManager().setSendingHandler(player);
			}

			if (event.allArgs.isEmpty())
			{
				event.send("Current volume **" + player.getVolume() + "**.");
				return;
			}

			float vol = Float.parseFloat(event.args[0]);
			vol = Math.min(Math.max(0, vol), 1); // Ensure 0 - 1
			player.setVolume(vol);
			event.send("**Volume: " + vol + "**");
		} catch (NumberFormatException e)
		{
			event.send("Invalid input. Example volume 0.5");
		} catch (ClassCastException e)
		{
			event.send("Player is not available.");
		}
	}
}

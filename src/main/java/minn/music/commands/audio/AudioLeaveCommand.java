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

import net.dv8tion.jda.managers.AudioManager;

public class AudioLeaveCommand extends GenericAudioCommand
{
	@Override
	public String getAttributes()
	{
		return "";
	}

	@Override
	public String getAlias()
	{
		return "dc";
	}

	@Override
	public String getInfo()
	{
		return "Disconnects the bot from the current voice channel.";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		AudioManager manager = event.guild.getAudioManager();
		if (manager.isConnected() || manager.isAttemptingToConnect())
			manager.closeAudioConnection();
		else
			event.send("I am not currently connected to a voice channel. If that is wrong contact a dev or change the voice region of this server.");
	}
}

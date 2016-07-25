/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.audio;

import net.dv8tion.jda.audio.AudioReceiveHandler;
import net.dv8tion.jda.audio.AudioSendHandler;
import net.dv8tion.jda.audio.CombinedAudio;
import net.dv8tion.jda.audio.UserAudio;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.concurrent.LinkedBlockingQueue;

public class AudioBride implements AudioReceiveHandler, AudioSendHandler
{

	private String userID;
	private LinkedBlockingQueue<byte[]> packages = new LinkedBlockingQueue<>(1); // Only a placeholder for one package
	private float volume = .5f;

	/**
	 * Send the audio from one user to another VoiceChannel if it's not in the same guild of course.
	 */
	public AudioBride(/*User user,*/)
	{
		/*this.userID = user.getId();*/
	}

	public void setVolume(float volume)
	{
		this.volume = Math.min(Math.max(volume, 0), 1);
	}

	public void setUser(User user)
	{
		this.userID = user.getId();
	}

	@Override
	public boolean canReceiveCombined()
	{
		return true;
	}

	@Override
	public boolean canReceiveUser()
	{
		return false;
	}

	@Override
	public synchronized void handleCombinedAudio(CombinedAudio combinedAudio)
	{
		packages.offer(combinedAudio.getAudioData(volume));
	}

	@Override
	public synchronized void handleUserAudio(UserAudio userAudio)
	{
		if (userAudio.getUser().getId().equals(userID))
			packages.offer(userAudio.getAudioData(volume));
		else SimpleLog.getLog("AudioBridge").trace("Rejected Package (Muted)");
	}

	@Override
	public void handleUserTalking(User user, boolean talking)
	{
	}

	@Override
	public boolean canProvide()
	{
		return !packages.isEmpty();
	}

	@Override
	public byte[] provide20MsAudio()
	{
		return packages.poll();
	}
}

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

package minn.music.util;

import net.dv8tion.jda.player.source.AudioTimestamp;

public class PlayerUtil
{

	public enum Symbol
	{
		PLAY("▶"),
		CURRENT("\uD83D\uDD18"),
		LOW_VOLUME("\uD83D\uDD08"),
		MED_VOLUME("\uD83D\uDD09"),
		LOUD_VOLUME("\uD83D\uDD0A"),
		MUTE("\uD83D\uDD07"),
		SPACE("▬");

		private String value;

		Symbol(String input)
		{
			this.value = input;
		}

		public String toString()
		{
			return value;
		}

	}

	public static String convert(AudioTimestamp totalSeconds, AudioTimestamp current, float vol)
	{
		String bar = " **[`" + current.getTimestamp() + "`/`" + totalSeconds.getTimestamp() + "`]** ";
		bar += Symbol.PLAY.toString();
		int percentage = (int) (((double) current.getTotalSeconds() / totalSeconds.getTotalSeconds()) * 10);
		int i;
		for(i = 0; i < percentage; i++)
		{
			bar += Symbol.SPACE;
		}

		bar += Symbol.CURRENT;

		while(i < 10)
		{
			bar += Symbol.SPACE;
			i++;
		}

		bar += (vol > .25f) ? vol > .5f ? Symbol.LOUD_VOLUME : Symbol.MED_VOLUME : vol < .1f ? Symbol.MUTE : Symbol.LOW_VOLUME;

		return bar;
	}

	public static void main(String... a)
	{
		System.out.println(convert(new AudioTimestamp(0, 4, 35, 0), new AudioTimestamp(0, 2, 2, 0), .56f));
	}

}

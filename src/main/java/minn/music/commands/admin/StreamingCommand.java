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

package minn.music.commands.admin;


import minn.music.MusicBot;
import minn.music.commands.GenericCommand;

public class StreamingCommand extends GenericCommand
{

	private MusicBot bot;

	public StreamingCommand(MusicBot bot)
	{
		this.bot = bot;
	}

	public String getAttributes()
	{
		return "<game>";
	}

	@Override
	public String getAlias()
	{
		return "streaming";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You are not allowed to modify this property.");
			return;
		}
		String twitch = " ";
		try
		{
			twitch = (String) MusicBot.config.get("twitch");
			if (twitch == null || twitch.isEmpty())
				twitch = " ";
		} catch (ClassCastException ignored)
		{
		}
		String finalTwitch = twitch;
		bot.managers.parallelStream().forEach(m ->
		{
			if (event.allArgs.isEmpty())
				m.getJDA().getAccountManager().setGame(null);
			else
				m.getJDA().getAccountManager().setStreaming(event.allArgs, "https://twitch.tv/" + finalTwitch);
		});
	}
}

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
import minn.music.managers.CommandManager;
import net.dv8tion.jda.JDA;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DetermineShards extends GenericCommand
{
	private final MusicBot bot;

	public DetermineShards(MusicBot bot)
	{
		this.bot = bot;
	}

	@Override
	public String getAlias()
	{
		return "shards";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("You can't use admin commands.");
			return;
		}
		List<CommandManager> list = new LinkedList<>(bot.managers);
		for (int i = 0; i < list.size(); i++)
		{
			list.get(i).getJDA().getAccountManager().setGame("Shard: " + i);
		}
		event.send("Determined shards. Look at bot's status.\n" + getStats());
	}

	@SuppressWarnings("MismatchedReadAndWriteOfArray")
	private String getStats()
	{
		Map<Integer, JDA> shards = bot.getShards();
		String stats = "Stats for **" + shards.size() + "** shard" + (shards.size() == 1 ? "" : "s") + ".\n```xl\n";
		CharSequence[] lines = new String[shards.size() + 1];
		int[] totalUsers = new int[]{0}, totalGuilds = new int[]{0}, totalTCs = new int[]{0}, totalVCs = new int[]{0}, totalPCs = new int[]{0};

		shards.forEach((index, api) ->
		{
			lines[index] = "Shard " + index + " has "
					+ api.getUsers().size() + " User(s), "
					+ api.getGuilds().size() + " Guild(s) with "
					+ api.getTextChannels().size() + " TC(s) and " + api.getVoiceChannels().size() + " VC(s) and "
					+ api.getPrivateChannels().size() + " open PrivateChannels.";

			totalGuilds[0] += api.getGuilds().size();
			totalUsers[0] += api.getUsers().size();
			totalTCs[0] += api.getTextChannels().size();
			totalVCs[0] += api.getVoiceChannels().size();
			totalPCs[0] += api.getPrivateChannels().size();
		});
		lines[lines.length - 1] = "In Total: "
				+ totalUsers[0] + " User(s), "
				+ totalGuilds[0] + " Guild(s) with "
				+ totalTCs[0] + " TC(s) and " + totalVCs[0] + " VC(s) and "
				+ totalPCs[0] + " open PrivateChannels.";

		return stats + String.join("\n", lines) + "```";
	}
}


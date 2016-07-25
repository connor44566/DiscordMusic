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

import com.mashape.unirest.http.Unirest;
import minn.music.MusicBot;
import minn.music.commands.GenericCommand;
import minn.music.managers.CommandManager;
import minn.music.util.EntityUtil;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.utils.AvatarUtil;

import java.io.InputStream;

public class AvatarCommand extends GenericCommand
{

	public String getAttributes()
	{
		return "<mention>";
	}

	public String getInfo()
	{
		return "\n*mention* accepts both pinging mentions(@user) and tag mentions (user#9999).";
	}

	@Override
	public String getAlias()
	{
		return "av";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		if (!event.author.getId().equals(MusicBot.config.owner))
		{
			event.send("This command has been removed from public use. (This is why we can't have nice things)");
			return;
		}
		event.send("Fetching...", message ->
		{
			boolean sent = message != null;
			if (event.message.getAttachments().isEmpty() || !event.message.getAttachments().get(0).isImage())
				try
				{
					event.api.getAccountManager().setAvatar(EntityUtil.getAvatarFromUser(EntityUtil.resolveUser(event.allArgs, event.api))).update();
					if (sent) message.deleteMessage();
					event.send("Updated Avatar.");
				} catch (Exception e)
				{
					if (sent)
						message.updateMessageAsync("ERROR: `" + e.toString() + "`" + (e.getStackTrace().length > 0 ? " [" + e.getStackTrace()[0].getLineNumber() + "]" : ""), null);
				}
			else
			{
				Message.Attachment a = event.message.getAttachments().get(0);
				try
				{
					InputStream in = Unirest.get(a.getUrl()).asBinary().getBody();
					event.api.getAccountManager().setAvatar(AvatarUtil.getAvatar(in)).update();
					if(sent) message.deleteMessage();
					event.send("Updated Avatar.");
				} catch (Exception e)
				{
					CommandManager.LOG.log(e);
					if(sent) message.updateMessageAsync("Something went wrong with that Avatar. (" + e.getClass().getSimpleName() + ")", null);
				}
			}
		});
	}
}

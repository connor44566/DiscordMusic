package minn.music.commands;

import com.mashape.unirest.http.Unirest;
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

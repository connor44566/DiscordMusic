package minn.music;

import minn.music.commands.*;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.utils.SimpleLog;

import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

public class Main
{
	private final static SimpleLog LOG = SimpleLog.getLog("Main");

	public static String getTimestamp()
	{
		return OffsetTime.now().format(DateTimeFormatter.ofPattern("[HH:mm:ss]"));
	}

	public static void main(String... a)
	{
		LOG.info("JDA-Version: " + JDAInfo.VERSION);
		new MusicBot(bot -> {
			bot.manager.registerCommand(new PingCommand());
			bot.manager.registerCommand(new JoinCommand(bot));
			bot.manager.registerCommand(new PlayCommand());
			bot.manager.registerCommand(new ListCommands(bot));
			bot.manager.registerCommand(new EvalCommand(bot));
			bot.manager.registerCommand(new StreamingCommand(bot));
			bot.manager.registerCommand(new ListCommand());
			bot.manager.registerCommand(new PlayerCommand(bot));

			bot.manager.registerCommand(new GenericCommand()
			{
				@Override
				public String getAlias()
				{
					return "invite";
				}

				@Override
				public void invoke(CommandEvent event)
				{
					event.send(String.format("**Invite: %s**",
							event.api.getSelfInfo().getAuthUrl(
									Permission.MESSAGE_WRITE,
									Permission.MESSAGE_READ,
									Permission.VOICE_CONNECT,
									Permission.VOICE_SPEAK)));
				}
			});
		});

	}

}
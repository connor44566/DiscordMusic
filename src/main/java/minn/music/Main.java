package minn.music;

import com.mashape.unirest.http.Unirest;
import minn.music.commands.*;
import minn.music.commands.external.CatCommand;
import minn.music.commands.external.GifCommand;
import minn.music.managers.CommandManager;
import minn.music.settings.Config;
import minn.music.util.PlayerUtil;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.impl.GameImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.SelfInfoImpl;
import net.dv8tion.jda.events.ShutdownEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.managers.AccountManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main
{
	private final static SimpleLog LOG = SimpleLog.getLog("Main");

	public static String getTimestamp()
	{
		return OffsetTime.now().format(DateTimeFormatter.ofPattern("[HH:mm:ss]"));
	}

	public static void main(String... a) throws IOException
	{
		LOG.info("JDA-Version: " + JDAInfo.VERSION);
		Config cfg = new Config("Base.json", true);
		int shards = 1;
		if (cfg.get("shards") != null && cfg.get("shards") instanceof Integer)
		{
			shards = (Integer) cfg.get("shards");
			LOG.info("Shards: " + shards);
		}
		final int[] i = {0};
		try
		{
			new MusicBot(manager -> {
				UptimeCommand.start = System.currentTimeMillis();
				AtomicReference<GenericCommand> command = new AtomicReference<>();

				manager.registerCommand(new PingCommand());
				manager.registerCommand(new JoinCommand(manager.bot));
				manager.registerCommand(new PlayCommand());


				manager.registerCommand(new StreamingCommand(manager.bot));
				manager.registerCommand(new PlayerCommand(manager.bot));
				manager.registerCommand(new UptimeCommand());
				manager.registerCommand(new AvatarCommand());
				manager.registerCommand(new GifCommand());
				manager.registerCommand(new CatCommand());

				command.set(new ListCommand());
				manager.registerCommand(command.get());
				manager.registerCommand(new _Alias_("current", new GenericCommand()
				{
					private Map<String, Boolean> running = new HashMap<>();

					@Override
					public String getAlias()
					{
						return " ";
					}

					@Override
					public void invoke(CommandEvent event)
					{
						MusicPlayer player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
						if (running.containsKey(event.guild.getId()) && running.get(event.guild.getId()))
						{
							event.send("Already updating.");
							return;
						}
						final boolean[] isReturned = {true};
						if (player == null)
							event.send("Nothing playing.");
						else
							event.send(player.getCurrentAudioSource().getInfo().getTitle() + "\n\n" + PlayerUtil.convert(player.getCurrentAudioSource().getInfo().getDuration(), player.getCurrentTimestamp(), player.getVolume()), m ->
							{
								new Thread(() -> {
									LOG.info("Debug: Returned");

									running.put(event.guild.getId(), true);
									isReturned[0] = m != null;
									for (int i = 0; i < 5; i++)
									{
										try
										{
											Thread.sleep(10000);
											if (isReturned[0])
												m.updateMessageAsync(
														player.getCurrentAudioSource().getInfo().getTitle() + "\n\n" + PlayerUtil.convert(player.getCurrentAudioSource().getInfo().getDuration(), player.getCurrentTimestamp(), player.getVolume())
														, ms -> isReturned[0] = true);
											else
												break;
										} catch (InterruptedException e)
										{
											LOG.log(e);
										}
										LOG.info("Debug: Updated");
									}
									LOG.info("Debug: Finished");
									running.put(event.guild.getId(), false);
								}, "Current").start();
							});
					}
				}, false));

				command.set(new ListCommands(manager.bot));
				manager.registerCommand(command.get());
				manager.registerCommand(new _Alias_("help", command.get()));

				manager.registerCommand(new GenericCommand()
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
				manager.registerCommand(new GenericCommand()
				{
					@Override
					public String getAlias()
					{
						return "exit";
					}

					@Override
					public void invoke(CommandEvent event)
					{
						if (!event.author.getId().equals(MusicBot.config.owner))
						{
							event.send("You cannot use this command.");
							return;
						}
						event.send("Shutting down...", msg -> {
							final int[] i = {0, 1};
							if (cfg.get("shards") != null)
								i[1] = (int) cfg.get("shards");
							manager.bot.managers.forEach(m -> {
								m.getJDA().addEventListener((EventListener) event1 -> {
									if (event1 instanceof ShutdownEvent && ++i[0] == i[1])
									{
										try
										{
											Unirest.shutdown();
										} catch (IOException e)
										{
											LOG.log(e);
										}
										System.exit(1);
									}
								});
								m.getJDA().shutdown();
							});
						});
					}
				});

				manager.registerCommand(new GenericCommand()
				{
					public String getAttributes()
					{
						return "<title> <url>";
					}

					@Override
					public String getAlias()
					{
						return "srt";
					}

					public String getInfo()
					{
						return "Used to test if url supports streaming.";
					}

					@Override
					public void invoke(CommandEvent event)
					{
						if (event.args.length < 2 || !event.author.getId().equals(MusicBot.config.owner))
						{
							event.send("Not supported");
							return;
						}
						new AccountManager((JDAImpl) event.api)
						{
							@Override
							public void setStreaming(String title, String url)
							{
								((SelfInfoImpl) api.getSelfInfo()).setCurrentGame(new GameImpl(title, url, Game.GameType.TWITCH));
								updateStatusAndGame();
							}
						}.setStreaming(event.args[0], event.args[1]);
					}
				});

				addCustom(manager.bot, manager);

				try
				{
					manager.registerCommand(new EvalCommand(manager.bot));
					manager.registerCommand(new PythonEval(manager.bot));
					manager.registerCommand(new CmdCommand(manager.bot));
					manager.registerCommand(new JavaEval(manager.bot));
				} catch (IOException e)
				{
					LOG.log(e);
				}

				LOG.info((++i[0]) + " shards ready!");
			}, shards, cfg);
		} catch (Exception e)
		{
			LOG.fatal(e);
			System.exit(-1);
		}
	}

	public static void addCustom(MusicBot bot, CommandManager manager)
	{
		if (MusicBot.config.get("custom") != null && MusicBot.config.get("custom") instanceof JSONArray)
		{
			JSONArray arr = (JSONArray) MusicBot.config.get("custom");
			arr.forEach(o ->
			{
				try
				{
					JSONObject object = (JSONObject) o;
					if (object.isNull("alias") || object.isNull("response"))
						throw new NullPointerException("Custom Command invalid: \n" + object.toString(2));
					manager.registerCommand(new GenericCommand()
					{
						@Override
						public String getAlias()
						{
							return object.getString("alias");
						}

						@Override
						public void invoke(CommandEvent event)
						{
							event.send(object.getString("response"));
						}
					});
				} catch (Exception e)
				{
					LOG.warn(e.toString());
				}
			});
		}
	}

}
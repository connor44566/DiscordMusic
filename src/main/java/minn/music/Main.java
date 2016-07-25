/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package minn.music;

import com.mashape.unirest.http.Unirest;
import minn.music.audio.send.QueueManager;
import minn.music.commands.*;
import minn.music.commands.admin.*;
import minn.music.commands.audio.*;
import minn.music.commands.code.*;
import minn.music.commands.media.*;
import minn.music.commands.mod.*;
import minn.music.commands.settings.*;
import minn.music.hooks.Logger;
import minn.music.hooks.impl.*;
import minn.music.managers.*;
import minn.music.settings.*;
import minn.music.util.PlayerUtil;
import net.dv8tion.jda.JDAInfo;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Game;
import net.dv8tion.jda.entities.impl.GameImpl;
import net.dv8tion.jda.entities.impl.JDAImpl;
import net.dv8tion.jda.entities.impl.SelfInfoImpl;
import net.dv8tion.jda.events.ShutdownEvent;
import net.dv8tion.jda.events.guild.GuildJoinEvent;
import net.dv8tion.jda.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.hooks.EventListener;
import net.dv8tion.jda.managers.AccountManager;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class Main
{
	public final static SimpleLog LOG = SimpleLog.getLog("Main");

	public static void main(String... a) throws IOException
	{
		SimpleLog.getLog("AudioBridge").setLevel(SimpleLog.Level.TRACE);
		LOG.info("JDA-Version: " + JDAInfo.VERSION);
		Config cfg = new Config("Base.json", true);
		int shards = 1;
		if (cfg.get("shards") != null && cfg.get("shards") instanceof Integer)
		{
			shards = (Integer) cfg.get("shards");
			LOG.info("Shards: " + shards);
		}
		final int[] i = {0};
		Logger.init();
		AtomicReference<CarbonAPIManager> carbonAPIManager = new AtomicReference<>(null);
		try
		{
			new MusicBot(manager ->
			{
				if (carbonAPIManager.get() == null) carbonAPIManager.set(new CarbonAPIManager(manager.getJDA()));
				AtomicReference<GenericCommand> command = new AtomicReference<>();

				// Moderation
				command.set(new Container(new BanCommand(), "mod").setPrivate(false));
				((Container) command.get()).addItem(new SoftbanCommand());
				((Container) command.get()).addItem(new MuteCommand());
				((Container) command.get()).addItem(new ClearCommand());
				((Container) command.get()).addItem(new FlushCommand());
				manager.registerContainer((Container) command.get());

				// Audio
				command.set(new Container(new PlayCommand(), "audio").setPrivate(false));
				((Container) command.get()).addItem(new JoinCommand(manager.bot));
				((Container) command.get()).addItem(new SkipCommand());
				((Container) command.get()).addItem(new RemoveSongCommand());
				((Container) command.get()).addItem(new AudioLeaveCommand());
				((Container) command.get()).addItem(new PlayerVolumeCommand());
				((Container) command.get()).addItem(new PlayerShuffleCommand());
				((Container) command.get()).addItem(new ListCommand());
				manager.registerContainer((Container) command.get());

				// Media
				command.set(new Container(new GifCommand(), "media"));
				((Container) command.get()).addItem(new CatCommand());
				((Container) command.get()).addItem(new DoggoComant());
				((Container) command.get()).addItem(new SpamifyCommand());
				((Container) command.get()).addItem(new TagCommand(manager.bot));
				((Container) command.get()).addItem(new SearchCommand());
				manager.registerContainer((Container) command.get());
				manager.registerCommand(new _Alias_("t", ((Container) command.get()).getCommand("tag"), false));

				// Admin only
				command.set(new Container(new GenericCommand()
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
						event.send("Shutting down...", msg ->
						{
							final int[] i = {manager.bot.getShards().size()};
							manager.bot.managers.forEach(m ->
							{
								m.getJDA().addEventListener((EventListener) event1 ->
								{
									if (event1 instanceof ShutdownEvent)
									{
										if (--i[0] <= 0)
										{
											try
											{
												Unirest.shutdown();
											} catch (IOException e)
											{
												LOG.log(e);
											}
											QueueManager.save();
											GuildSettings.save();
											System.exit(0);
										}
									}
								});
								QueueManager.save(m.getJDA());
								m.getJDA().shutdown();
							});
						});
					}
				}, "admin").setAdmin(true));
				((Container) command.get()).addItem(new GenericCommand()
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
				((Container) command.get()).addItem(new StreamingCommand(manager.bot));
				((Container) command.get()).addItem(new DetermineShards(manager.bot));
				((Container) command.get()).addItem(new AvatarCommand());
				((Container) command.get()).addItem(new DetermineUsage());
				((Container) command.get()).addItem(new IgnoreCommand());
				((Container) command.get()).addItem(new LogCommand());
				((Container) command.get()).addItem(new StatsCommand(manager.bot));
				manager.registerContainer((Container) command.get());

				manager.registerCommand(new _Alias_("current", new GenericCommand()
				{
					private Map<String, Boolean> running = new HashMap<>();

					@Override
					public String getAlias()
					{
						return null;
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
							event.send(player.getCurrentAudioSource().getInfo().getTitle() + "\n\n" + PlayerUtil.convert(player.getCurrentAudioSource().getInfo().getDuration(), player.getCurrentTimestamp(), player.getVolume()), m -> new Thread(() ->
							{
								LOG.debug("Debug: Returned");

								running.put(event.guild.getId(), true);
								isReturned[0] = m != null;
								for (int i = 0; i < 5; i++)
								{
									try
									{
										Thread.sleep(10000); // 10 seconds
										if (isReturned[0])
											m.updateMessageAsync(
													player.getCurrentAudioSource().getInfo().getTitle() + "\n\n" + PlayerUtil.convert(player.getCurrentAudioSource().getInfo().getDuration(), player.getCurrentTimestamp(), player.getVolume())
													, ms -> isReturned[0] = true);
										else
											break;
									} catch (InterruptedException e)
									{
										LOG.log(e);
									} catch (NullPointerException ignored)
									{
									}
									LOG.debug("Debug: Updated");
								}
								LOG.debug("Debug: Finished");
								running.put(event.guild.getId(), false);
							}, "Current").start());
					}
				}));

				// With Alias
				command.set(new ListCommands(manager.bot));
				manager.registerCommand(command.get());
				manager.registerCommand(new _Alias_("help", command.get(), true));

				// Settings
				command.set(new Container(new PrefixCommand(), "settings").setPrivate(false));
				((Container) command.get()).addItem(new NickCommand());
				((Container) command.get()).addItem(new TodoCommand());
				((Container) command.get()).addItem(new ModLogCommand());
				((Container) command.get()).addItem(new WelcomeCommand());
				((Container) command.get()).addItem(new ProfileCommand());
				manager.registerContainer((Container) command.get());

				// Implemented
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


				// Custom
				addCustom(manager);


				// Evals
				try
				{
					command.set(new Container(new EvalCommand(manager.bot), "code"));
					((Container) command.get()).addItem(new PythonEval(manager.bot));
					((Container) command.get()).addItem(new CmdCommand(manager.bot));
					((Container) command.get()).addItem(new JavaEval(manager.bot));
					manager.registerContainer((Container) command.get());
				} catch (IOException e)
				{
					LOG.log(e);
				}

				// General
				manager.registerCommand(new PingCommand());
				manager.registerCommand(new UptimeCommand(manager.getJDA()));
				manager.registerCommand(new DiscrimCommand(manager.bot));

				manager.registerMentionListener(new PrefixTeller());
				manager.getJDA().addEventListener((EventListener) event ->
				{
					if (event instanceof GuildJoinEvent)
						SimpleLog.getLog("Guild").info("Joined " + ((GuildJoinEvent) event).getGuild().getName());
					else if (event instanceof GuildLeaveEvent)
						SimpleLog.getLog("Guild").info("Left " + ((GuildLeaveEvent) event).getGuild().getName());
				});
				new ModLogManager(manager.getJDA());
				new WelcomeManager(manager.getJDA());
				QueueManager.resume(manager.getJDA());
				carbonAPIManager.get().setBot(manager.bot);
				carbonAPIManager.get().listenTo(manager.getJDA());
				LOG.info((++i[0]) + " shards ready!");
			}, shards, cfg);
		} catch (Exception e)
		{
			LOG.fatal(e);
			System.exit(-1);
		}
	}

	public static void addCustom(CommandManager manager)
	{
		if (MusicBot.config.get("custom") != null && MusicBot.config.get("custom") instanceof JSONArray)
		{
			Container container = new Container("custom");
			JSONArray arr = (JSONArray) MusicBot.config.get("custom");
			arr.forEach(o ->
			{
				try
				{
					JSONObject object = (JSONObject) o;
					if (object.isNull("alias") || object.isNull("response"))
						throw new NullPointerException("Custom Command invalid: \n" + object.toString(2));
					container.addItem(new GenericCommand()
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
			if (!container.isEmpty())
				manager.registerContainer(container);
		}
	}

}
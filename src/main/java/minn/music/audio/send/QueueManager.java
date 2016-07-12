package minn.music.audio.send;

import minn.music.util.PersistenceUtil;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.Playlist;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class QueueManager
{

	private static HashMap<String, LinkedList<String>> queueCache = new HashMap<>();

	public static void resume(JDA api)
	{
		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 5, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r ->
		{
			Thread t = new Thread(r, "Resuming Playlist");
			t.setDaemon(true);
			return t;
		});

		//SimpleLog.getLog("QueueManager").setLevel(SimpleLog.Level.DEBUG);

		queueCache = (HashMap<String, LinkedList<String>>) PersistenceUtil.retrieve("queueCache");
		if (queueCache == null)
		{
			queueCache = new HashMap<>();
			return;
		}
		queueCache.forEach((id, list) ->
		{
			SimpleLog.getLog("QueueManager").debug("Resuming: " + id + list.toString());
			VoiceChannel c = api.getVoiceChannelById(id);
			if (c == null)
			{
				SimpleLog.getLog("QueueManager").debug("Channel is null.");
				return;
			}
			executor.submit(() ->
			{
				SimpleLog.getLog("QueueManager").debug("Thread started!");
				Guild g = c.getGuild();
				MusicPlayer player = new MusicPlayer();
				g.getAudioManager().setSendingHandler(player);
				g.getAudioManager().openAudioConnection(c);
				list.parallelStream().forEach(s ->
				{
					try
					{
						Playlist playlist = Playlist.getPlaylist(s);
						playlist.getSources().stream().filter(source -> {
							AudioInfo info = source.getInfo();
							if (info.getError() != null)
								SimpleLog.getLog("QueueManager").debug("ERROR " + info.getError());
							return source.getInfo().getError() == null;
						}).forEach(source ->
						{
							player.getAudioQueue().add(source);
							if (!player.isPlaying())
								player.play();
						});
					} catch (Exception e)
					{
						SimpleLog.getLog("QueueManager").debug(e);
					}
				});
				SimpleLog.getLog("QueueManager").debug("Resumed: " + c.toString() + " " + player.getAudioQueue().toString());
			});
		});
	}

	public static void save(JDA jda)
	{
		jda.getGuilds().parallelStream().filter(g -> g.getAudioManager().isConnected()).forEach(g ->
		{
			try
			{
				MusicPlayer player = (MusicPlayer) g.getAudioManager().getSendingHandler();
				if (player == null || player.getAudioQueue().isEmpty())
					return;
				LinkedList<String> list = new LinkedList<>();
				player.getAudioQueue().parallelStream().forEach(s ->
						list.add(s.getInfo().getOrigin()));
				queueCache.put(g.getAudioManager().getConnectedChannel().getId(), list);
			} catch (Exception ignored)
			{
			}
		});
	}

	public static void save()
	{
		PersistenceUtil.save(queueCache, "queueCache");
	}

}
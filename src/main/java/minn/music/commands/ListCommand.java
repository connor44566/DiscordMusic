package minn.music.commands;

import net.dv8tion.jda.player.MusicPlayer;
import net.dv8tion.jda.player.source.AudioInfo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListCommand extends GenericCommand
{

	public boolean isPrivate()
	{
		return false;
	}

	@Override
	public String getAlias()
	{
		return "list";
	}

	@Override
	public void invoke(CommandEvent event)
	{
		MusicPlayer player = null;
		long total = 0L;
		try
		{
			player = (MusicPlayer) event.guild.getAudioManager().getSendingHandler();
		} catch (ClassCastException ignored)
		{
		}
		if (player == null)
		{
			event.send("No queued songs.");
			return;
		}
		if (player.getCurrentAudioSource() == null && player.getAudioQueue().isEmpty())
		{
			event.send("No queued songs.");
			return;
		}
		String response = "__Current:__ " +
				"`[" + player.getCurrentTimestamp().getTimestamp() + "/" + player.getCurrentAudioSource().getInfo().getDuration().getTimestamp() + "]` ** " +
				player.getCurrentAudioSource().getInfo().getTitle().replaceAll("[*]{2}", "\\*\\*") + "**";

		total += player.getCurrentAudioSource().getInfo().getDuration().getTotalSeconds() - player.getCurrentTimestamp().getTotalSeconds();

		if (!player.getAudioQueue().isEmpty())
		{
			response += "\n__Queue: **" + player.getAudioQueue().size() + "** entries.__\n";
			for (int i = 0; i < player.getAudioQueue().size(); i++)
			{
				AudioInfo info = player.getAudioQueue().get(i).getInfo();
				if (i < 5)
					response += "\n**" + (i + 1) + ")** `[" + info.getDuration().getTimestamp() + "]` " + info.getTitle().replaceAll("[*]{2}", "\\*\\*");
				total += info.getDuration().getTotalSeconds();
			}
			if (player.getAudioQueue().size() >= 5)
			{
				response += "\n...";
			}
		}
		response += "\n\n" + time(total) + " left.";

		event.send(response);
	}

	private static String time(long inSeconds) {
		List<String> times = new LinkedList<>();

		long days = TimeUnit.SECONDS.toDays(inSeconds);
		inSeconds -= TimeUnit.DAYS.toSeconds(days);

		long hours = TimeUnit.SECONDS.toHours(inSeconds);
		inSeconds -= TimeUnit.HOURS.toSeconds(hours);

		long minutes = TimeUnit.SECONDS.toMinutes(inSeconds);

		long seconds = inSeconds - TimeUnit.MINUTES.toSeconds(minutes);

		if (days > 0) {
			times.add(String.format("**%d** day%s", days, days != 1 ? "s" : ""));
		}
		if (hours > 0) {
			times.add(String.format("**%d** hour%s", hours, hours != 1 ? "s" : ""));
		}
		if (minutes > 0) {
			times.add(String.format("**%d** minute%s", minutes, minutes != 1 ? "s" : ""));
		}
		if (seconds > 0) {
			times.add(String.format("**%d** second%s", seconds, seconds != 1 ? "s" : ""));
		}

		String uptime = "";

		for (int i = 0; i < times.size() - 1; i++) {
			uptime += times.get(i) + ", ";
		}

		if (times.size() != 1 && uptime.length() > 2)
			return uptime.substring(0, uptime.length() - 2) + " and " + times.get(times.size() - 1);
		else
			return times.get(0);
	}
}

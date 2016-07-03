package minn.music.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeUtil
{

	public static String uptime(long inMillis)
	{
		List<String> times = new LinkedList<>();

		long days = TimeUnit.MILLISECONDS.toDays(inMillis);
		inMillis -= TimeUnit.DAYS.toMillis(days);

		long hours = TimeUnit.MILLISECONDS.toHours(inMillis);
		inMillis -= TimeUnit.HOURS.toMillis(hours);

		long minutes = TimeUnit.MILLISECONDS.toMinutes(inMillis);
		inMillis -= TimeUnit.MINUTES.toMillis(minutes);

		long seconds = TimeUnit.MINUTES.toSeconds(inMillis);

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

	public static String time(long inSeconds) {
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

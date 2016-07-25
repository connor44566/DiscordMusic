/*
 *      Copyright 2016 Florian Spieß (Minn).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package minn.music.hooks;

import net.dv8tion.jda.utils.SimpleLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Logger implements SimpleLog.LogListener
{

	private List<LogEntry> list = new FixedSizeList<>(500);
	private static Logger logger;

	public static void init()
	{
		new File("Logs").mkdirs();
		logger = new Logger();
		SimpleLog.addListener(logger);
	}

	public static File log()
	{
		return log(logger.list.size());
	}

	public static File log(int amount)
	{
		if (logger.list.isEmpty())
			return null;
		File target = new File("Logs/" + System.currentTimeMillis() / 10000 + ".log");
		try
		{
			if (!target.createNewFile())
				return null;
		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
		try (FileOutputStream out = new FileOutputStream(target))
		{
			out.write(stringify(logger.list.subList(0, Math.min(amount, logger.list.size()))).getBytes());
			return target;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private static String stringify(Throwable err)
	{
		StackTraceElement[] elements = err.getStackTrace();
		CharSequence[] parts = new CharSequence[elements.length];
		Arrays.stream(elements).map(StackTraceElement::toString).collect(Collectors.toList()).toArray(parts);
		return err.getClass().getName() + ": " + err.getMessage() + "\n\t" + String.join("\n\t", parts);
	}


	private static String stringify(List<LogEntry> list)
	{
		return String.join("\n", list.parallelStream().map(LogEntry::toString).collect(Collectors.toList()).toArray(new CharSequence[list.size()]));
	}

	private Logger()
	{
	}

	@Override
	public void onLog(SimpleLog log, SimpleLog.Level logLevel, Object message)
	{
		if (!log.name.equals("CommandManager")
				&& !message.toString().startsWith("PRESENCE_UPDATE")
				&& !message.toString().startsWith("READY")
				&& !message.toString().startsWith("TYPING_START")
				&& !message.toString().startsWith("MESSAGE_CREATE")
				&& !message.toString().startsWith("MESSAGE_DELETE")
				&& !message.toString().startsWith("MESSAGE_UPDATE")
				&& !message.toString().contains("transmitting audio"))
			list.add(new LogEntry(log, logLevel, (
					message.toString().startsWith("GUILD_CREATE") ? "GUILD_CREATE {...}" :
							(message.toString().startsWith("GUILD_MEMBERS_CHUNK") ? "GUILD_MEMBERS_CHUNK {...}" :
									message))));
	}

	@Override
	public void onError(SimpleLog log, Throwable err)
	{
		list.add(new LogEntry(log, err));
	}

	private class LogEntry
	{

		private SimpleLog log;
		private SimpleLog.Level level;
		private Object message;

		LogEntry(SimpleLog log, SimpleLog.Level level, Object message)
		{
			this.log = log;
			this.level = level;
			this.message = message;
		}

		LogEntry(SimpleLog log, Throwable err)
		{
			this(log, SimpleLog.Level.FATAL, err);
		}

		public String toString()
		{
			return String.format("[%s] [%s]: %s", log.name, level.name(), (message instanceof Throwable ? "ERROR " + stringify((Throwable) message) : message.toString()));
		}

	}

	private class FixedSizeList<T> extends LinkedList<T>
	{

		private final int maxSize;

		FixedSizeList(int maxSize)
		{
			this.maxSize = maxSize;
		}

		@Override
		public boolean add(T type)
		{
			if (size() + 1 >= maxSize)
				remove();
			super.add(size(), type);
			return true;
		}

	}

}

package minn.music.util;

import net.dv8tion.jda.utils.SimpleLog;

import java.io.*;

public class PersistenceUtil
{

	public static final String BASE_URI = "Objects/";
	public static final SimpleLog LOG = SimpleLog.getLog("PersistenceUtil");

	public static synchronized void save(Serializable object, String name)
	{
		assert name != null && !name.isEmpty() && object != null;
		ensureDir();
		try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(BASE_URI + name))))
		{
			out.writeObject(object);
			out.close();
		} catch (IOException e)
		{
			LOG.fatal(e);
		}
	}

	public static Object retrieve(String name)
	{
		assert name != null && !name.isEmpty();
		ensureDir();
		try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(BASE_URI + name))))
		{
			Object object = in.readObject();
			in.close();
			return object;
		} catch (IOException | ClassNotFoundException e)
		{
			LOG.fatal(e);
			return null;
		}
	}

	private static void ensureDir()
	{
		File dir = new File(BASE_URI);
		dir.mkdirs();
	}

}

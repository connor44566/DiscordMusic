package minn.music.settings;

import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Config
{
	// Generic information
	public final static String BASE_URI = "Settings/";
	protected final static SimpleLog LOG = SimpleLog.getLog("ConfigReader");
	// Instance information
	protected File cfg;
	protected JSONObject object;
	protected Map<String, Object> keys = new HashMap<>();
	// After reading
	protected boolean isBase;
	public String prefix;
	public String token;
	public String owner;
	public String home;
	public String logChan;

	public Config(String location, boolean isBase) throws IOException
	{
		File folder = new File(BASE_URI);
		folder.mkdirs();

		cfg = new File(BASE_URI + location);

		this.isBase = isBase;

		readObjectKeys(); // Reads json internally
	}

	/**
	 * Creates a new Config file in folder "{@value BASE_URI}".
	 *
	 * @param object   Object to fill in file. Can be null and extra fields will be added.
	 * @param location Location to append to "{@value BASE_URI}".
	 * @return boolean -
	 * Whether file was created or not.
	 */
	public static boolean createConfig(JSONObject object, String location)
	{
		try
		{
			location = BASE_URI + location;
			File f = new File(location);
			f.createNewFile();
			Files.write(Paths.get(f.toURI()), object.toString(4).getBytes());
			return true;
		} catch (IOException e)
		{
			LOG.log(e);
			return false;
		}
	}

	private void readJSON(File file) throws IOException
	{
		try
		{
			if(isBase && !file.exists())
			{
				Config.createConfig(new JSONObject()
						.put("prefix", "")
						.put("token", "")
						.put("owner", "")
						.put("home", "")
						.put("logChan", ""), "Base.json");
				LOG.fatal("Config file is missing. It has been generated and you need to populate it.");
				System.exit(1);
			}
			readJSON(new JSONObject(new String(Files.readAllBytes(Paths.get(file.toURI())))));
		} catch (IOException e)
		{
			LOG.log(e);
			throw e;
		}
	}

	private void readJSON(JSONObject object)
	{
		this.object = object;
		if (isBase)
		{
			if (object.isNull("token") || object.isNull("owner") || object.isNull("prefix"))
			{
				LOG.log(new IllegalArgumentException("Config file \"" + cfg + "\" is missing required fields. Please delete it and restart."));
				Config.createConfig(new JSONObject()
						.put("prefix", "")
						.put("token", "")
						.put("owner", "")
						.put("home", "")
						.put("logChan", ""), "Base.json");
				return;
			}
			token = object.getString("token");
			owner = object.getString("owner");
			prefix = object.getString("prefix");
		}
		if (object.has("home") && !object.isNull("home"))
			home = object.getString("home");
		if (object.has("logChan") && !object.isNull("logChan"))
			logChan = object.getString("logChan");
	}

	/**
	 * Used to refresh config information if file was changed.
	 */
	public void readObjectKeys() throws IOException
	{
		readJSON(cfg);
		for (Iterator<String> it = object.keys(); it.hasNext(); )
		{
			String key = it.next();
			this.keys.put(key, object.get(key));
		}
	}

	/**
	 * Used to get dynamic json object keys. Refresh keys using {@link Config#readObjectKeys()}.
	 *
	 * @param key Key to look for in cached information.
	 * @return Object contained with given key or null.
	 */
	public Object get(String key)
	{
		if (keys.containsKey(key))
			return keys.get(key);
		return null;
	}
}

package minn.music.util;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class SearchUtil
{
	public static final SimpleLog LOG = SimpleLog.getLog("SearchUtil");

	public static String getDog()
	{
		try
		{
			String response = Unirest.get("http://random.dog/woof").asStringAsync().get(1, TimeUnit.MINUTES).getBody();
			return "http://random.dog/" + response;
		} catch (Exception e)
		{
			LOG.log(e);
			return "Request Timed Out.";
		}
	}

	public static String getCat()
	{
		try
		{
			String response = Unirest.get("http://random.cat/meow").asJsonAsync().get(1, TimeUnit.SECONDS).getBody().getObject().getString("file");
			return response.isEmpty() ? "Something went wrong with the api." : response;
		} catch (Exception e)
		{
			LOG.log(e);
			return "Request Timed Out.";
		}
	}

	@SuppressWarnings("deprecation")
	public static String getGif(String... query)
	{
		String tags;
		if (query == null || query.length < 1)
			tags = "";
		else if (query.length > 1)
			tags = URLEncoder.encode(String.join(" ", query));
		else
			tags = URLEncoder.encode(query[0]);
		try
		{
			JSONObject o = Unirest.get("http://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tags=" + tags).asJsonAsync().get(1, TimeUnit.SECONDS).getBody().getObject();
			if (o.isNull("data"))
				return "";
			o = o.getJSONObject("data");
			if (o.isNull("image_url"))
				return "";
			return (tags.isEmpty() ? "" : "`Query: " + tags + "`\n") + o.getString("image_url");
		} catch (Exception e)
		{
			LOG.log(e);
			return "Request Timed Out.";
		}
	}

}

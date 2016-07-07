package minn.music.util;

import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;

import java.net.URLEncoder;

public class SearchUtil
{

	public static String getCat()
	{
		try
		{
			return Unirest.get("http://random.cat/meow").asJson().getBody().getObject().getString("file");
		} catch (Exception e)
		{
			return e.toString();
		}
	}

	public static String getGif(String... query)
	{
		String tags;
		if (query == null || query.length < 1)
			tags = "";
		else if (query.length > 1)
			tags = String.join(" ", query);
		else
			tags = query[0];
		try
		{
			//noinspection deprecation
			JSONObject o = Unirest.get("http://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tags=" + URLEncoder.encode(tags)).asJson().getBody().getObject();
			if (o.isNull("data"))
				return "";
			o = o.getJSONObject("data");
			if (o.isNull("image_url"))
				return "";
			return o.getString("image_url");
		} catch (Exception e)
		{
			return e.toString();
		}
	}

}

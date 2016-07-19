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

package minn.music.util;

import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.player.source.AudioInfo;
import net.dv8tion.jda.player.source.AudioSource;
import net.dv8tion.jda.player.source.RemoteSource;
import net.dv8tion.jda.utils.SimpleLog;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ALL")
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

	public static String searchYoutube(String query)
	{
		try
		{
			AudioSource source = new RemoteSource("https://www.youtube.com/results?search_query=" + URLEncoder.encode(query));
			AudioInfo info = source.getInfo();
			if (info.getError() == null)
				return info.getOrigin();
		} catch (Exception e)
		{
		}
		return "Nothing found.";
	}

	/**
	 * Same as {@link SearchUtil#searchYoutube(String)} but returns {@link net.dv8tion.jda.player.source.RemoteSource RemoteSource}.
	 * @param query
	 * @return
	 */
	public static AudioSource getRemoteSource(String query)
	{
		try
		{
			AudioSource source = new RemoteSource("https://www.youtube.com/results?search_query=" + URLEncoder.encode(query));
			AudioInfo info = source.getInfo();
			if (info.getError() == null)
				return source;
		} catch (Exception e)
		{
		}
		return null;
	}

	/**
	 * Checks whether given String is a valid URL.
	 * @param url String
	 * @return True or False
	 */
	public static boolean isURL(String url)
	{
		return url.matches("^https?://(www\\.)?[^\\s]+\\.[^\\s]+$");
	}

	public static void main(String... a)
	{
		System.out.println(searchYoutube("Test"));
	}

}

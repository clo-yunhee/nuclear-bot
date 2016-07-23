package nuclearbot.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

/*
 * Copyright (C) 2016 NuclearCoder
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Static class for HTTP POST requests.
 */
public class HTTP {

	/**
	 * Fetches data from the specified URL with given parameters.
	 * The output is treated as JSON, parsed as the given class.
	 * The parameters must be in a URL format. (a=x&b=y...)
	 * @param targetUrl the URL target
	 * @param urlParameters the request parameters
	 * @param class the class of the returned object
	 * @return the parsed output
	 */
	public static <T> T fetchData(String targetUrl, String urlParameters, Class<T> clazz)
	{
		HttpURLConnection connection = null;
		try
		{
			URL url = new URL(targetUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST"); // for higher param length limit
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.getBytes().length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			// send post data
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(urlParameters);
			out.close();
			
			// now the response
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			return new Gson().fromJson(reader, clazz);
		}
		catch (Exception e)
		{
			Logger.error("An error occurred while sending an HTTP request.");
			Logger.printStackTrace(e);
			return null;
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}
	
}

package nuclearbot.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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
 * Static class for HTTP POST requests.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
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
	public static <T> T fetchData(final String targetUrl, final String urlParameters, final Class<T> clazz)
	{
		HttpURLConnection connection = null;
		try
		{
			final String paramData = URLEncoder.encode(urlParameters, "UTF-8");
			final int paramLength = paramData.getBytes().length;
			
			final URL url = new URL(targetUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST"); // for higher param length limit
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", String.valueOf(paramLength));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			
			// send post data
			final DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(paramData);
			out.close();
			
			// now the response
			final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

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

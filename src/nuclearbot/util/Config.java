package nuclearbot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
 * Static class for configuration.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class Config {

	private static final Properties prop;
	private static final File configFile;
	
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread(new ConfigShutdownHook()));
		
		configFile = new File("config.properties");
		
		if (configFile.isDirectory())
		{
			Logger.error("Couldn't write to config.properties in the program's directory.");
			System.exit(1);
		}
		if (!configFile.exists()) // copy the default file if it doesn't exist
		{
			FileOutputStream out = null;
			InputStream in = null;
			try
			{
				in = Config.class.getResourceAsStream("/config.properties");
				out = new FileOutputStream(configFile);
				
				byte[] buf = new byte[128];
				while (in.read(buf) != -1)
				{
					out.write(buf);
				}
				
				out.close();
				in.close();
			}
			catch (IOException e)
			{
				Logger.error("An error occurred while writing default config.");
				Logger.printStackTrace(e);
				try
				{
					out.close();
				}
				catch (IOException silent) {}
				try
				{
					in.close();
				}
				catch (IOException silent) {}
				System.exit(1);
			}
		}

		prop = new Properties();
		
		FileReader in = null;
		try
		{
			in = new FileReader(configFile);
			prop.load(in);
		}
		catch (IOException e)
		{
			Logger.error("An error occurred while loading config.");
			Logger.printStackTrace(e);
		}
		finally
		{
			if (in != null)
			{
				try
				{
					in.close();
				}
				catch (IOException e) {}
			}
		}
		
	}
	
	/**
	 * Writes the configuration into the file.
	 * @throws IOException
	 * if the file exists but is a directory
	 * rather than a regular file, does not exist but cannot be 
	 * created, or cannot be opened for any other reason
	 */
	public static void saveConfig() throws IOException
	{
		final FileWriter writer = new FileWriter(configFile);
		prop.store(writer, "please do not attempt to edit anything manually unless explicitly directed otherwise");
		try
		{
			writer.close();
		}
		catch (IOException e) {}
	}
	
	/**
	 * Reloads the configuration from the file.
	 * @throws IOException
	 * if the file does not exist, is 
	 * a directory rather than a regular file, or for some other 
	 * reason cannot be opened for reading.
	 */
	public static void reloadConfig() throws IOException
	{
		final FileReader reader = new FileReader(configFile);
		prop.load(reader);
		try
		{
			reader.close();
		}
		catch (IOException e) {}
	}
	
	/**
	 * Returns the property with the specified key in this 
	 * configuration. If the key is not found in the list, 
 	 * the method returns an empty string and the property is set.
	 * @param key the property key
	 * @return the value in this property list with the specified key
	 */
	public static String get(final String key)
	{
		if (prop.containsKey(key))
		{
			return prop.getProperty(key);
		}
		else
		{
			prop.setProperty(key, "");
			return "";
		}
	}
	
	/**
	 * Sets the property with the specified key in this
	 * configuration with the specified value. This method
	 * returns the previous value, or null if there was none.
	 * @param key the property key
	 * @param value the new value
	 * @return the previous value, or null
	 */
	public static String set(final String key, final Object value)
	{
		return (String) prop.setProperty(key, String.valueOf(value));
	}
	
	private static class ConfigShutdownHook implements Runnable {
		
		@Override
		public void run()
		{
			Logger.info("(Exit) Saving config...");
			try
			{
				saveConfig();
			}
			catch (IOException e)
			{
				Logger.error("(Exit) Couldn't save config.");
				Logger.printStackTrace(e);
			}
		}
		
	}
	
}

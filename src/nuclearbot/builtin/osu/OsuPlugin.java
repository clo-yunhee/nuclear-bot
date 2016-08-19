package nuclearbot.builtin.osu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import nuclearbot.client.ChatClient;
import nuclearbot.client.ChatOut;
import nuclearbot.client.ImplChatOut;
import nuclearbot.plugin.Plugin;
import nuclearbot.util.Config;
import nuclearbot.util.HTTP;
import nuclearbot.util.Logger;

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
 * osu! client with basic commands.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class OsuPlugin implements Plugin {
	
	private static final String SERVER = "cho.ppy.sh";
	private static final int PORT = 6667;
	
	private Thread m_shutdownHook;
	
	private String m_apiKey;
	private String m_ircKey;
	private String m_username;
	
	private Socket m_socket;
	private BufferedReader m_reader;
	private ChatOut m_chatOut;
	
	private PingRunnable m_pingThread;
	
	// osu data fetchers
	private <T> T get(final String page, final String urlParameters, final Class<T> clazz)
	{
		return HTTP.fetchData("http://osu.ppy.sh/api/" + page, "k=" + m_apiKey + '&' + urlParameters, clazz);
	}
	
	/**
	 * Fetches data for a beatmap set with the specified id.
	 * Returns an empty array if there is no such map set.
	 * @param beatmapsetId the set id
	 * @return the beatmap set
	 */
	public DataBeatmap[] getBeatmapset(final int beatmapsetId)
	{
		return get("get_beatmaps", "s=" + beatmapsetId, DataBeatmap[].class);
	}
	
	/**
	 * Fetches data about a beatmap with the specified id.
	 * Returns null if there is no such beatmap.
	 * @param beatmapId the beatmap id
	 * @return data about the beatmap
	 */
	public DataBeatmap getBeatmap(final int beatmapId)
	{
		try
		{
			return get("get_beatmaps", "b=" + beatmapId, DataBeatmap[].class)[0];
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**
	 * Fetches data about a user with the specified username.
	 * Returns null if there is no such user.
	 * @param username the username
	 * @return data about the user
	 */
	public DataUser getUser(final String username)
	{
		try
		{
			return get("get_user", "type=string&u=" + (username != null ? username : m_username), DataUser[].class)[0];
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}
	
	/**
	 * Queues a osu! private message.
	 * The message will be sent to the host,
	 * basically sending a message to oneself.
	 * @param message the message to send
	 * @throws IOException if the writer throws an IOException
	 */
	public void sendPrivateMessage(final String msg)
	{
		m_chatOut.write("PRIVMSG " + m_username + " :" + msg + "\r\n");
	}
	
	/**
	 * Queues a message to send to the osu! IRC server.
	 * @param message the message to send
	 */
	public void sendMessage(final String msg)
	{
		m_chatOut.write(msg + "\r\n");
	}
	
	@Override
	public void onLoad(final ChatClient client) throws IOException
	{
		m_apiKey = Config.get("osu_api_key");
		m_ircKey = Config.get("osu_irc_key");
		m_username = Config.get("osu_user");
		m_socket = null;
		m_reader = null;
		m_chatOut = null;
		
		client.registerCommand("np", "!np", new CommandNowPlaying()).setDescription("Displays the song playing in osu!. Only works when in game.");
		client.registerCommand("req", "!req <url> [comments]", new CommandRequest(this)).setDescription("Requests a song to be played with optional comments.");
		client.registerCommand("stats", "!stats [user]", new CommandStats(this)).setDescription("Displays info about an osu! player.");
	}

	@Override
	public void onStart(final ChatClient client) throws IOException
	{
		String line = null;
		
		Logger.info("(osu!) Connecting...");
		
		// open connection and I/O objects
		Runtime.getRuntime().addShutdownHook(m_shutdownHook = new Thread(new OsuClientShutdownHook()));
		m_socket = new Socket(SERVER, PORT);
		m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
		m_chatOut = new ImplChatOut(m_socket.getOutputStream(), "osu");
		
		// send connection data
		sendMessage("PASS " + m_ircKey);
		sendMessage("NICK " + m_username);
		
		// wait for response
		while ((line = m_reader.readLine()) != null)
		{
			// skip the prefix which is ':cho.ppy.sh ' (12 characters long)
			if (line.startsWith("376", 12)) // this is the code for the last line of MOTD
			{
				Logger.info("(osu!) Connected!");
				break; // we're in
			}
			else if (line.startsWith("464", 12))
			{
				Logger.info("(osu!) Bad authentication token.");
				m_chatOut.close();
				return;
			}
		}

		m_pingThread = new PingRunnable();
		m_pingThread.start();
	}

	@Override
	public void onStop(final ChatClient client) throws IOException
	{
		Logger.info("(osu!) Releasing resources...");
		
		// close resources and socket
		m_pingThread.stop();
		m_chatOut.close();
		m_reader.close();
		m_socket.close();
		m_socket = null;
		m_reader = null;
		m_chatOut = null;
		
		// we exited properly, unregister shutdown hook
		Runtime.getRuntime().removeShutdownHook(m_shutdownHook);
	}

	@Override
	public void onMessage(final ChatClient client, final String username, final String message) throws IOException
	{
	}

	private class PingRunnable implements Runnable {
		
		private Thread m_thread;
		
		private volatile boolean m_running;
		
		public void start()
		{
			m_running = true;
			m_thread = new Thread(this, "osu ping");
			m_thread.start();
		}
		
		public void stop()
		{
			m_running = false;
		}
		
		@Override
		public void run()
		{
			try
			{
				while (m_running)
				{
					if (m_reader.ready())
					{
						final String line = m_reader.readLine();
						if (line.startsWith("PING")) // ping request
						{
							sendMessage("PONG " + line.substring(5));
						}
					}
					else
					{
						try
						{
							// this thread has very low priority, hence will almost never be active.
							Thread.sleep(800L);
						}
						catch (InterruptedException e) {}
					}
				}
			}
			catch (IOException e)
			{
				Logger.error("(osu!) Exception caught in ping thread:");
				Logger.printStackTrace(e);
			}
		}
		
	}
	
	private class OsuClientShutdownHook implements Runnable {
		
		@Override
		public void run()
		{	
			Logger.info("(Exit) (osu!) Closing resources...");
			if (m_chatOut != null)
			{
				m_chatOut.close();
			}
			try
			{
				if (m_socket != null)
				{
					m_socket.close();
				}
			}
			catch (IOException e) {} // we don't really care
		}
		
	}
	
}

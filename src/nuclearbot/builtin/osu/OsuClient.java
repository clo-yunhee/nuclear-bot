package nuclearbot.builtin.osu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nuclearbot.client.ChatClient;
import nuclearbot.client.ChatListener;
import nuclearbot.client.ChatOut;
import nuclearbot.client.ImplChatOut;
import nuclearbot.utils.HTTP;
import nuclearbot.utils.Logger;

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
 * osu! client with basic commands.
 */
public class OsuClient implements ChatListener {
	
	private static final Pattern REGEX_BEATMAP_URL = Pattern.compile("^https?://osu\\.ppy\\.sh/(b|s)/([0-9]+)(&.*)?$"); // 

	private static final String SERVER = "cho.ppy.sh";
	private static final int PORT = 6667;
	
	// Twitch message formats
	
	private static final String MSG_STATS_UNKNOWN_USER = "There is no such user.";
	private static final String MSG_STATS = "Stats for %s: %,.2f pp, rank #%,d, accuracy %.2f%%";
	private static final String MSG_REQUEST_NOT_FOUND = "No beatmap found.";
	private static final String MSG_REQUEST_OTHER = "Request: %s";
	private static final String MSG_REQUEST_BEATMAPSET = "Request: %s - %s (%d diffs)";
	private static final String MSG_REQUEST_BEATMAP = "Request: %s - %s [%s] (creator %s) \u00A6 BPM %d \u00A6 AR %.1f \u00A6 %.2f stars";
	
	// osu! message formats
	
	private static final String PRIVMSG_REQUEST_OTHER = "Request from %s: %s";
	private static final String PRIVMSG_REQUEST_BEATMAPSET = "Request from %s: [%s %s - %s] (%d diffs)";
	private static final String PRIVMSG_REQUEST_BEATMAP = "Request from %s: [%s %s - %s] \u00A6 BPM %d \u00A6 AR %.1f \u00A6 %.2f stars";
	private static final String PRIVMSG_REQUEST_MESSAGE = "(%s)";
	
	private final Thread m_shutdownHook;
	
	private final String m_apiKey;
	private final String m_ircKey;
	private final String m_username;
	
	private Socket m_socket;
	private BufferedReader m_in;
	private ChatOut m_out;
	
	private PingRunnable m_pingThread;
	
	/**
	 * Instantiates an osu! client with specified credentials.
	 * @param apiKey the osu! API key
	 * @param osuName the osu! username
	 * @param ircKey the osu! IRC key
	 */
	public OsuClient(final String apiKey, final String osuName, final String ircKey)
	{
		Runtime.getRuntime().addShutdownHook(m_shutdownHook = new Thread(new OsuClientShutdownHook()));
		// final and intern strings for memory efficiency
		m_apiKey = apiKey.intern();
		m_ircKey = ircKey.intern();
		m_username = osuName.intern();
		m_socket = null;
		m_in = null;
		m_out = null;
	}
	
	/**
	 * Queues a osu! private message.
	 * The message will be sent to the host,
	 * basically sending a message to oneself.
	 * @param message the message to send
	 * @throws IOException if the writer throws an IOException
	 */
	public void queuePrivateMessage(final String msg)
	{
		queueMessage("PRIVMSG " + m_username + " :" + msg);
	}
	
	/**
	 * Queues a message to send to the osu! IRC server.
	 * @param message the message to send
	 */
	public void queueMessage(final String msg)
	{
		m_out.write(msg + "\r\n");
	}
	
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
			return get("get_user", "type=string&u=" + username, DataUser[].class)[0];
		}
		catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	@Override
	public void onMessage(final ChatClient client, final String username, final String message) throws IOException
	{
	}

	@Override
	public void onCommand(final ChatClient client, final String username, final String command, final String[] params) throws IOException
	{
		if (command.equalsIgnoreCase("np"))
		{
			client.sendMessage(NowPlaying.getCurrentSong());
		}
		else if (command.equalsIgnoreCase("req"))
		{
			if (params.length < 2)
			{
				client.sendMessage("Invalid usage of command: !req <link> [message]");
			}
			else
			{
				final String beatmapUrl = params[1].toLowerCase();
				final Matcher matcher = REGEX_BEATMAP_URL.matcher(beatmapUrl);
				if (matcher.matches())
				{
					final boolean isBeatmapset = matcher.group(1).equalsIgnoreCase("s");
					if (isBeatmapset)
					{
						final int beatmapsetId = Integer.parseInt(matcher.group(2));
						final DataBeatmap[] beatmapset = getBeatmapset(beatmapsetId);
						if (beatmapset == null || beatmapset.length == 0)
						{
							client.sendMessage(MSG_REQUEST_NOT_FOUND);
							return;
						}
						else
						{
							final String artist = beatmapset[0].getArtist();
							final String title = beatmapset[0].getTitle();
							client.sendMessage(String.format(MSG_REQUEST_BEATMAPSET, artist, title, beatmapset.length));
							queuePrivateMessage(String.format(PRIVMSG_REQUEST_BEATMAPSET, username, beatmapUrl, artist, title, beatmapset.length));
						}
					}
					else
					{
						final int beatmapId = Integer.parseInt(matcher.group(2));
						final DataBeatmap beatmap = getBeatmap(beatmapId);
						if (beatmap == null)
						{
							client.sendMessage(MSG_REQUEST_NOT_FOUND);
							return;
						}
						else
						{
							client.sendMessage(String.format(MSG_REQUEST_BEATMAP, beatmap.getArtist(), beatmap.getTitle(), beatmap.getVersion(), beatmap.getCreator(), beatmap.getBPM(), beatmap.getDiffAR(), beatmap.getDifficultyRating()));
							queuePrivateMessage(String.format(PRIVMSG_REQUEST_BEATMAP, username, beatmapUrl, beatmap.getArtist(), beatmap.getTitle(), beatmap.getBPM(), beatmap.getDiffAR(), beatmap.getDifficultyRating()));
						}
					}
				}
				else
				{
					client.sendMessage(String.format(MSG_REQUEST_OTHER, beatmapUrl));
					queuePrivateMessage(String.format(PRIVMSG_REQUEST_OTHER, username, beatmapUrl));
				}
				if (params.length > 2)
				{
					final StringBuffer sb = new StringBuffer();
					for (int i = 2; i < params.length; i++)
					{
						sb.append(params[i]);
						sb.append(' ');
					}
					queuePrivateMessage(String.format(PRIVMSG_REQUEST_MESSAGE, sb.toString().trim()));
				}
			}
		}
		else if (command.equalsIgnoreCase("stats"))
		{
			if (params.length > 2)
			{
				client.sendMessage("Invalid usage of command: !stats [user]");
			}
			else
			{
				final DataUser user = getUser(params.length == 2 ? params[1] : m_username);
				if (user == null)
				{
					client.sendMessage(MSG_STATS_UNKNOWN_USER);
				}
				else
				{
					client.sendMessage(String.format(Locale.US, MSG_STATS, user.getName(), user.getPP(), user.getRank(), user.getAccuracy()));
				}
			}
		}
	}
	
	@Override
	public void onStart(final ChatClient client) throws IOException
	{
		String line = null;
		
		Logger.info("(osu!) Connecting...");
		
		// open connection and I/O objects
		m_socket = new Socket(SERVER, PORT);
		m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
		m_out = new ImplChatOut(m_socket.getOutputStream(), "osu");
		
		// send connection data
		queueMessage("PASS " + m_ircKey);
		queueMessage("NICK " + m_username);
		
		// wait for response
		while ((line = m_in.readLine()) != null)
		{
			// skip the prefix which is ':cho.ppy.sh ' (12 characters long)
			if (line.startsWith("376", 12)) // this is the code for the last line of MOTD
			{
				Logger.info("(osu!) Connected!");
				break; // we're in
			}
			else if (line.startsWith("433"))
			{
				Logger.info("(osu!) Nickname is already in use.");
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
		m_out.close();
		m_in.close();
		m_socket.close();
		m_socket = null;
		m_in = null;
		m_out = null;
		
		// call garbage collector for memory efficiency
		System.gc();
		
		// we exited properly, unregister shutdown hook
		Runtime.getRuntime().removeShutdownHook(m_shutdownHook);
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
					if (m_in.ready())
					{
						final String line = m_in.readLine();
						if (line.startsWith("PING")) // ping request
						{
							queueMessage("PONG " + line.substring(5));
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
			if (m_out != null)
			{
				m_out.close();
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

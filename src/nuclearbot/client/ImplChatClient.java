package nuclearbot.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)<br>
 * <br>
 * Implementation of the bot client.
 */
public class ImplChatClient implements ChatClient {
	
	// only compile the regex once
	private static final Pattern REGEX_MESSAGE = Pattern.compile("^:([a-zA-Z0-9_]+)\\![a-zA-Z0-9_]+@[a-zA-Z0-9_]+\\.tmi\\.twitch\\.tv PRIVMSG #[a-zA-Z0-9_]+ :(.+)$");
	
	private static final String SERVER = "irc.chat.twitch.tv";
	private static final int PORT = 6667;
	
	private final Thread m_shutdownHook;
	
	private final String m_username;
	private final String m_authToken;
	private final String m_channel;
	private final ChatListener m_chatListener;
	private final List<StateListener> m_stateListeners;
	
	private Socket m_socket;
	private BufferedReader m_in;
	private ChatOut m_out;
	
	private boolean m_reconnect; // true to attempt to reconnect when the socket is closed
	
	private volatile boolean m_stop; // if true, the client will exit at next loop.
	
	/**
	 * Instantiates a Twitch client with specified Twitch IRC account.
	 * There can be only one listener during a client's lifetime.
	 * Undefined behavior if the listener is forcefully changed using reflection.
	 * @param userName the Twitch username
	 * @param authToken the Twitch oauth token
	 * @param listener the client listener
	 */
	public ImplChatClient(final String userName, final String authToken, final ChatListener listener)
	{
		Runtime.getRuntime().addShutdownHook(m_shutdownHook = new Thread(new ChatClientShutdownHook()));
		// final and intern strings for memory efficiency
		// user name and channel must be lower-case
		m_username = userName.toLowerCase().intern();
		m_authToken = authToken.intern();
		m_channel = ('#' + m_username).intern();
		m_chatListener = listener;
		m_stateListeners = new LinkedList<StateListener>();
		m_socket = null;
		m_in = null;
		m_out = null;
		m_reconnect = false;
		m_stop = false;
	}
	
	private void notifyStateConnected()
	{
		for (StateListener listener : m_stateListeners)
		{
			listener.onConnected(this);
		}
	}
	
	private void fireStateDisconnected()
	{
		for (StateListener listener : m_stateListeners)
		{
			listener.onDisconnected(this);
		}
	}
	
	@Override
	public void addStateListener(final StateListener listener)
	{
		m_stateListeners.add(listener);
		Logger.info("(Twitch) Registered state listener.");
	}
	
	@Override
	public void removeStateListener(final StateListener listener)
	{
		m_stateListeners.remove(listener);
		Logger.info("(Twitch) Unregistered state listener.");
	}
	
	@Override
	public void removeAllStateListeners()
	{
		m_stateListeners.clear();
		Logger.info("(Twitch) Cleared all state listeners.");
	}

	// shorthand to skip the ':tmi.twitch.tv ' part (will be inlined in released versions)
	private boolean startsWithTmi(final String str, final String prefix)
	{
		return str.startsWith(prefix, 15);
	}
	
	// shorthand to skip the ':user.tmi.twitch.tv ' part (will be inlined in released versions)
	private boolean startsWithUserTmi(final String str, final String prefix)
	{
		return str.startsWith(prefix, m_username.length() + 16);
	}
	
	private void writeln(final String msg) throws IOException
	{
		m_out.write(msg + "\r\n");
	}
	
	@Override
	public void sendMessage(final String msg) throws IOException
	{
		m_out.write("PRIVMSG " + m_channel + " :" + msg + "\r\n");
	}
	
	@Override
	public void stop()
	{
		m_stop = true;
	}
	
	@Override
	public void connect() throws IOException
	{
		String line = null;
		
		do
		{
			Logger.info("(Twitch) Connecting...");
			
			// open connection and I/O objects
			m_socket = new Socket(SERVER, PORT);
			m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
			m_out = new ImplChatOut(m_socket.getOutputStream(), "twitch");
			m_reconnect = false;
			
			// send connection data
			writeln("PASS " + m_authToken);
			writeln("NICK " + m_username);
			
			// wait for response
			while ((line = m_in.readLine()) != null)
			{
				// skip the prefix which is ':tmi.twitch.tv ' (15 characters long)
				if (startsWithTmi(line, "376")) // this is the code of MOTD's last line
				{
					Logger.info("(Twitch) Connected!");
					break; // we're in
				}
				else if (startsWithTmi(line, "433"))
				{
					Logger.info("(Twitch) Nickname is already in use.");
					return;
				}
			}
			
			Logger.info("(Twitch) Requesting commands capability...");
			// ask for commands, allows for RECONNECT message
			writeln("CAP REQ :twitch.tv/commands");
			
			Logger.info("(Twitch) Joining channel...");
			// join the user's channel
			writeln("JOIN " + m_channel);
			
			sendMessage("Bot running...");
			
			try
			{
				// call the start listener
				m_chatListener.onStart(this);
			}
			catch (Exception e) // catch exceptions here to not leave the loop
			{
				Logger.error("(Twitch) Exception in listener onStart:");
				Logger.printStackTrace(e);
			}

			notifyStateConnected();
			
			while (!m_stop)
			{
				if (!m_in.ready())
				{
					Thread.yield();
					continue;
				}
				
				line = m_in.readLine();
				
				if (line.startsWith("PING")) // ping request
				{
					writeln("PONG " + line.substring(5));
				}
				else if (line.startsWith("RECONNECT")) // twitch reconnect message
				{
					m_reconnect = true;
					Logger.warning("(Twitch) Received a reconnect notice!");
				}
				else if (startsWithTmi(line, "CAP * ACK"))
				{
					Logger.info("(Twitch) Request for commands capability validated.");
				}
				else
				{
					final Matcher matcher = REGEX_MESSAGE.matcher(line);
					if (matcher.matches()) // if the message is a chat message
					{
						final String username = matcher.group(1);
						final String message = matcher.group(2);
						
						if (message.charAt(0) == '!') // if it's a command
						{
							final String[] params = message.split("\\s+");
							// strip the ! from the first argument
							final String command = params[0].substring(1);
							
							Logger.info(String.format("(Twitch) Command from %s: %s", username, Arrays.toString(params)));
							
							// system calls (like in the Alicization arc SAO, lol)
							if (username.equalsIgnoreCase(m_username))
							{
								if (message.equalsIgnoreCase("!restart"))
								{
									Logger.info("(Twitch) Restart command issued.");
									m_reconnect = true;
									m_stop = true;
								}
								else if (message.equalsIgnoreCase("!stop"))
								{
									Logger.info("(Twitch) Stop command issued.");
									m_reconnect = false;
									m_stop = true;
								}
							}
						
							try
							{
								// call the command listener
								m_chatListener.onCommand(this, username, command, params);
							}
							catch (Exception e) // catch exceptions here to not leave the loop
							{
								Logger.error("(Twitch) Exception in listener onCommand:");
								Logger.printStackTrace(e);
							}
						
						}
						else
						{
							Logger.info(String.format("(Twitch) Message from %s: %s", username, message));
							try
							{
								// call the message listener
								m_chatListener.onMessage(this, username, message);
							}
							catch (Exception e) // catch exceptions here to not leave the loop
							{
								Logger.error("(Twitch) Exception in listener onMessage:");
								Logger.printStackTrace(e);
							}
						}
					}
					else if (startsWithUserTmi(line, "353")
							|| startsWithUserTmi(line, "366")
							|| startsWithTmi(line, "ROOMSTATE")
							|| startsWithTmi(line, "USERSTATE")) // types of messages to ignore
					{
					}
					else
					{
						Logger.info("(Twitch) " + line);
					}
				}
			}

			sendMessage(m_reconnect ? "Restarting bot..." : "Stopping bot...");
			
			try
			{
				// call the stop listener
				m_chatListener.onStop(this);
			}
			catch (Exception e) // catch exceptions here to not leave the method 
			{
				Logger.error("(Twith) Exception in listener onStop:");
				Logger.printStackTrace(e);
			}
			
			try
			{
				Thread.sleep(800L); // give it some time to finish tasks
			}
			catch (InterruptedException e) {}
			
			Logger.info("(Twitch) Releasing resources...");
			
			// close resources and socket
			m_out.close();
			m_in.close();
			m_socket.close();
			m_socket = null;
			m_in = null;
			m_out = null;
			
			// call garbage collector for memory efficiency
			System.gc();
			
			fireStateDisconnected();
			
		} while (m_reconnect);
		
		// we exited properly, unregister shutdown hook.
		Runtime.getRuntime().removeShutdownHook(m_shutdownHook);
		
		Logger.info("(Twitch) Exiting client loop...");
	}
	
	private class ChatClientShutdownHook implements Runnable {
		
		@Override
		public void run()
		{	
			Logger.info("(Exit) (Twitch) Closing resources...");
		
			if (m_out != null)
			{
				m_out.close(); // attempt to close output thread cleanly
			}
			try
			{
				if (m_socket != null)
				{
					m_socket.close(); // close socket
				}
			}
			catch (IOException e) {}
		}
		
	}
	
}

package nuclearbot.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nuclearbot.plugin.CommandExecutor;
import nuclearbot.plugin.Plugin;
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
 * Implementation of the bot client.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
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
	private final Plugin m_chatListener;
	private final List<StateListener> m_stateListeners;
	private final Map<String, CommandExecutor> m_commands;
	
	private final CommandExecutor m_systemCallExecutor;
	
	private final int m_usernameLength;
	
	private Socket m_socket;
	private BufferedReader m_reader;
	private ChatOut m_chatOut;
	
	private boolean m_doReconnect; // true to attempt to reconnect when the socket is closed
	private volatile boolean m_doStop; // if true, the client will exit at next loop.
	
	/**
	 * Instantiates a Twitch client with specified Twitch IRC account.
	 * There can be only one listener during a client's lifetime.
	 * Undefined behavior if the listener is forcefully changed using reflection.
	 * @param userName the Twitch username
	 * @param authToken the Twitch oauth token
	 * @param listener the client listener
	 */
	public ImplChatClient(final String userName, final String authToken, final Plugin listener)
	{
		Runtime.getRuntime().addShutdownHook(m_shutdownHook = new Thread(new ChatClientShutdownHook()));
		// final and intern strings for memory efficiency
		// user name and channel must be lower-case
		m_username = userName.toLowerCase().intern();
		m_usernameLength = userName.length();
		m_authToken = authToken.intern();
		m_channel = ('#' + m_username).intern();
		m_chatListener = listener;
		m_stateListeners = new LinkedList<StateListener>();
		m_commands = new HashMap<String, CommandExecutor>();
		m_systemCallExecutor = new CommandSystemCalls();
		m_socket = null;
		m_reader = null;
		m_chatOut = null;
		m_doReconnect = false;
		m_doStop = false;
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
	public void registerCommand(final String command, final CommandExecutor executor)
	{
		if (m_commands.containsKey(command))
		{
			throw new IllegalArgumentException("Registered an already registered command \"" + command + "\".");
		}
		m_commands.put(command, executor);
		Logger.info("(Twitch) Registered command \"" + command + "\".");
	}
	
	@Override
	public void unregisterCommand(final String command)
	{
		if (!m_commands.containsKey(command))
		{
			throw new IllegalArgumentException("Unregistered not-registered command \"" + command + "\".");
		}
		m_commands.remove(command);
		Logger.info("(Twitch) Unregistered command \"" + command + "\".");
	}
	
	@Override
	public void registerStateListener(final StateListener listener)
	{
		if (m_stateListeners.contains(listener))
		{
			throw new IllegalArgumentException("Registered an already registered StateListener.");
		}
		m_stateListeners.add(listener);
		Logger.info("(Twitch) Registered state listener.");
	}
	
	@Override
	public void unregisterStateListener(final StateListener listener)
	{
		if (!m_stateListeners.contains(listener))
		{
			throw new IllegalArgumentException("Unregistered a not-registered StateListener.");
		}
		m_stateListeners.remove(listener);
		Logger.info("(Twitch) Unregistered state listener.");
	}
	
	@Override
	public void unregisterAllStateListeners()
	{
		m_stateListeners.clear();
		Logger.info("(Twitch) Cleared all state listeners.");
	}
	
	private void send(final String msg) throws IOException
	{
		m_chatOut.write(msg + "\r\n");
	}
	
	@Override
	public void sendMessage(final String msg) throws IOException
	{
		m_chatOut.write("PRIVMSG " + m_channel + " :" + msg + "\r\n");
	}
	
	@Override
	public void stop()
	{
		m_doStop = true;
	}
	
	@Override
	public void connect() throws IOException
	{
		String line = null;
		
		m_commands.clear();
		
		registerCommand("stop", m_systemCallExecutor);
		registerCommand("restart", m_systemCallExecutor);
		
		try
		{
			// call the load listener
			m_chatListener.onLoad(this);
		}
		catch (Exception e) // catch exceptions here to not leave the loop
		{
			Logger.error("(Twitch) Exception in listener onLoad:");
			Logger.printStackTrace(e);
		}

		do
		{
			Logger.info("(Twitch) Connecting...");
			
			// open connection and I/O objects
			m_socket = new Socket(SERVER, PORT);
			m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
			m_chatOut = new ImplChatOut(m_socket.getOutputStream(), "twitch");
			m_doReconnect = false;
			m_doStop = true;
			
			// send connection data
			send("PASS " + m_authToken);
			send("NICK " + m_username);
			
			// wait for response
			while ((line = m_reader.readLine()) != null)
			{
				// skip the prefix which is ':tmi.twitch.tv ' (15 characters long)
				if (line.startsWith("376", 15)) // this is the code of MOTD's last line
				{
					Logger.info("(Twitch) Connected!");
					m_doStop = false;
					break; // we're in
				}
				else if (line.startsWith("433", 15))
				{
					Logger.error("(Twitch) Nickname is already in use.");
					break;
				}
			}
			if (!m_doStop)
			{
				Logger.info("(Twitch) Requesting reconnect message capability...");
				// ask for commands, allows for RECONNECT message
				send("CAP REQ :twitch.tv/commands");
				
				Logger.info("(Twitch) Joining channel...");
				// join the user's channel
				send("JOIN " + m_channel);
				
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
				
				while (!m_doStop)
				{
					if (!m_reader.ready())
					{
						Thread.yield();
						continue;
					}
					
					line = m_reader.readLine();
					
					if (line.startsWith("PING")) // ping request
					{
						send("PONG " + line.substring(5));
					}
					else if (line.startsWith("RECONNECT")) // twitch reconnect message
					{
						m_doReconnect = true;
						Logger.info("(Twitch) Received a reconnect notice!");
					}
					else if (line.startsWith("CAP * ACK", 15))
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
							
								try
								{
									// call the command listener
									final CommandExecutor executor = m_commands.get(command);
									if (executor != null)
									{
										executor.onCommand(this, username, command, params);
									}
									else
									{
										Logger.warning("(Twitch) Unknown command.");
									}
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
						else if (line.startsWith("353", 16 + m_usernameLength)
								|| line.startsWith("366", 16 + m_usernameLength)
								|| line.startsWith("ROOMSTATE", 15)
								|| line.startsWith("USERSTATE", 15)) // types of messages to ignore
						{
						}
						else
						{
							Logger.info("(Twitch) " + line);
						}
					}
				}
	
				sendMessage(m_doReconnect ? "Restarting bot..." : "Stopping bot...");
				
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
			}
			
			Logger.info("(Twitch) Releasing resources...");
			
			// close resources and socket
			m_chatOut.close();
			m_reader.close();
			m_socket.close();
			m_socket = null;
			m_reader = null;
			m_chatOut = null;
			
			// call garbage collector for memory efficiency
			System.gc();
			
			fireStateDisconnected();
			
		} while (m_doReconnect);
		
		// we exited properly, unregister shutdown hook.
		Runtime.getRuntime().removeShutdownHook(m_shutdownHook);
		
		Logger.info("(Twitch) Exiting client loop...");
	}
	
	public class CommandSystemCalls implements CommandExecutor {

		@Override
		public void onCommand(final ChatClient client, final String username, final String command, final String[] params) throws IOException
		{
			// system calls (like in the Alicization arc SAO, lol)
			if (username.equalsIgnoreCase(m_username))
			{
				if (command.equalsIgnoreCase("restart"))
				{
					Logger.info("(Twitch) Restart command issued.");
					m_doReconnect = true;
					m_doStop = true;
				}
				else if (command.equalsIgnoreCase("stop"))
				{
					Logger.info("(Twitch) Stop command issued.");
					m_doReconnect = false;
					m_doStop = true;
				}
			}
			else
			{
				Logger.warning("Unauthorized command.");
				sendMessage("Unauthorized command.");
			}
		}

	}
	
	private class ChatClientShutdownHook implements Runnable {
		
		@Override
		public void run()
		{	
			Logger.info("(Exit) (Twitch) Closing resources...");
		
			if (m_chatOut != null)
			{
				m_chatOut.close(); // attempt to close output thread cleanly
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

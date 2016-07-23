package nuclearbot.client;

import java.io.IOException;

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
 * Public API interface for a chat listener.
 */
public interface ChatListener {

	/**
	 * Listener for a chat message.
	 * @param client the Twitch client
	 * @param username the sender's username
	 * @param message the message
	 * @throws IOException delegate exception handling to the client 
	 */
	public void onMessage(ChatClient client, String username, String message) throws IOException;

	/**
	 * Listener for a chat command.
	 * The command name is the word after the exclamation mark.
	 * The argument array contains the command name first.
	 * @param client the Twitch client
	 * @param username the sender's username
	 * @param command the command name
	 * @param args the argument array
	 * @throws IOException delegate exception handling to the client 
	 */
	public void onCommand(ChatClient client, String username, String command, String[] params) throws IOException;
	
	/**
	 * Listener for client start.
	 * This method is called before entering the client loop, after joining the channel.
	 * @param client the Twitch client
	 * @throws IOException delegate exception handling to the client 
	 */
	public void onStart(ChatClient client) throws IOException;
	
	/**
	 * Listener for client stop.
	 * This method is called before the Twitch chat connection is closed,
	 * the client is not listening anymore at this point.
	 * @param client the Twitch client
	 * @throws IOException delegate exception handling to the client 
	 */
	public void onStop(ChatClient client) throws IOException;
	
	/**
	 * A default listener that doesn't do anything.
	 */
	public static final ChatListener DUMMY = new ChatListener() {
		public void onMessage(ChatClient client, String username, String message) throws IOException {}
		public void onCommand(ChatClient client, String username, String command, String[] params) throws IOException {}
		public void onStart(ChatClient client) throws IOException {}
		public void onStop(ChatClient client) throws IOException {}
	};
	
}

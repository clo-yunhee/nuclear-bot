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
 * Public API interface for the bot client.
 */
public interface ChatClient {
	
	/**
	 * Registers a new state listener.
	 * @param stateListener the listener to add
	 */
	public void addStateListener(StateListener stateListener);
	
	/**
	 * Unregisters the given state listener.
	 * Fails silently if it was not present.
	 * @param stateListener the listener to remove
	 */
	public void removeStateListener(StateListener stateListener);
	
	/**
	 * Removes all state listeners.
	 */
	public void removeAllStateListeners();

	/**
	 * Sends a chat message to the Twitch channel.
	 * @param message the message to send
	 * @throws IOException if the writer throws an IOException
	 */
	public void sendMessage(String msg) throws IOException;
	
	/**
	 * Initiates the connection with the Twitch chat.
	 * @throws IOException if the socket or I/O connection failed, or if any of the inputs/outputs fail
	 */
	public void connect() throws IOException;

	/**
	 * Notifies the client to stop.
	 */
	public void stop();
		
}

package nuclearbot.client;

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
 * Public API interface for a chat listener.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface ChatListener {
	
	/**
	 * Listener for client connected.
	 * This method is called after the plugin's onStart method, before entering the client loop.
	 * @param client the Twitch client
	 */
	public void onConnected(ChatClient client);
	
	/**
	 * Listener for client disconnected.
	 * This method is called after the plugin's onStop method, and after resources are released.
	 * @param client the Twitch client
	 */
	public void onDisconnected(ChatClient client);
	
	/**
	 * Listener for client chat message.
	 * This method is called after the plugin's onMessage method.
	 * @param client the Twitch client
	 * @param username the sender's username
	 * @param message the message
	 */
	public void onChat(ChatClient client, String username, String message);
	
}

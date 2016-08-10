package nuclearbot.plugin;

import java.io.IOException;

import nuclearbot.client.ChatClient;

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
 * Public API interface for a plugin.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface Plugin {
	
	/**
	 * Listener for a chat message.
	 * @param client the Twitch client
	 * @param username the sender's username
	 * @param message the message
	 * @throws IOException delegate exception handling to the client 
	 */
	public void onMessage(ChatClient client, String username, String message) throws IOException;

	/**
	 * Listener for client load.
	 * Please avoid using the constructor, as it may cause problems with not-built-in plugins.
	 * @param client the Twitch client
	 * @throws IOException delegate exception handling to the client
	 */
	public void onLoad(ChatClient client) throws IOException;
	
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
	
}

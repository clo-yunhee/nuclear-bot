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
 * Public API interface for a state listener.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface StateListener {
	
	/**
	 * Listener for client connected.
	 * This method is called after the chat listener onStart method, before entering the client loop.
	 * @param client the Twitch client
	 */
	public void onConnected(ChatClient client);
	
	/**
	 * Listener for client disconnected.
	 * This method is called after the chat listener onStop method, and after resources are released.
	 * @param client the Twitch client
	 */
	public void onDisconnected(ChatClient client);
	
}

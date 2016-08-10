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
 * Public API interface for a chat command executor.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface CommandExecutor {

	/**
	 * Listener for the commands this executor is bound to.
	 * @param client the Twitch client
	 * @param username the sender's username
	 * @param command the command name
	 * @param args the argument array
	 * @throws IOException delegate exception handling to the client 
	 */
	public void onCommand(ChatClient client, String username, String command, String[] params) throws IOException;
	
}

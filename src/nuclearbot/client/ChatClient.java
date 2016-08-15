package nuclearbot.client;

import java.io.IOException;

import nuclearbot.plugin.CommandExecutor;

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
 * Public API interface for the bot client.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface ChatClient {
	
	/**
	 * Registers a new command.
	 * @param label the command name
	 * @param usage the command usage
	 * @param executor the command executor
	 * @throws IllegalArgumentException if the command was already registered
	 */
	public void registerCommand(String label, String usage, CommandExecutor executor);
	
	/**
	 * Unregisters a command.
	 * @param label the command instance
	 * @throws IllegalArgumentException if the command was not registered
	 */
	public void unregisterCommand(String label);
	
	/**
	 * Registers a new state listener.
	 * @param stateListener the listener
	 * @throws IllegalArgumentException if the listener was already registered
	 */
	public void registerChatListener(ChatListener stateListener);
	
	/**
	 * Unregisters the given state listener.
	 * @param stateListener the listener
	 * @throws IllegalArgumentException if the listener was not registered
	 */
	public void unregisterChatListener(ChatListener stateListener);
	
	/**
	 * Unregisters all state listeners.
	 */
	public void unregisterAllChatListeners();

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

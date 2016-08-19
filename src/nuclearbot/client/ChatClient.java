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
	 * Returns the command which has the specified name.
	 * The method returns null if there is no match.
	 * @param label the command name
	 * @return the command or null
	 */
	public Command getCommand(String label);
	
	/**
	 * Registers a new command.
	 * @param label the command name
	 * @param usage the command usage
	 * @param executor the command executor
	 * @return the newly-created command
	 * @throws IllegalArgumentException if the command was already registered
	 */
	public Command registerCommand(String label, String usage, CommandExecutor executor);
	
	/**
	 * Unregisters a command.
	 * @param label the command instance
	 * @throws IllegalArgumentException if the command was not registered
	 */
	public void unregisterCommand(String label);
	
	/**
	 * Registers a new client listener.
	 * @param listener the listener
	 * @throws IllegalArgumentException if the listener was already registered
	 */
	public void registerClientListener(ClientListener listener);
	
	/**
	 * Unregisters the given client listener.
	 * @param listener the listener
	 * @throws IllegalArgumentException if the listener was not registered
	 */
	public void unregisterClientListener(ClientListener listener);
	
	/**
	 * Unregisters all client listeners.
	 */
	public void unregisterAllClientListeners();

	/**
	 * Sends a chat message to the Twitch channel.
	 * @param message the message to send
	 */
	public void sendMessage(String message);
	
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

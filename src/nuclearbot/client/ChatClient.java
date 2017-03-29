package nuclearbot.client;

import nuclearbot.plugin.CommandExecutor;

import java.io.IOException;

/*
 * Copyright (C) 2017 NuclearCoder
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
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface ChatClient {

    /**
     * Returns the command which has the specified name.
     * The method returns null if there is no match.
     *
     * @param label the command name
     * @return the command or null
     */
    Command getCommand(String label);

    /**
     * Registers a new command.
     *
     * @param label    the command name
     * @param usage    the command usage
     * @param executor the command executor
     * @return the newly-created command
     * @throws IllegalArgumentException if the command was already registered
     */
    Command registerCommand(String label, String usage, CommandExecutor executor);

    /**
     * Unregisters a command.
     *
     * @param label the command name
     * @throws IllegalArgumentException if the command was not registered
     */
    void unregisterCommand(String label);

    /**
     * Returns true if the command is registered, false otherwise.
     *
     * @param name the command name
     * @return true if the command is registered, false otherwise
     */
    boolean isCommandRegistered(String name);

    /**
     * Registers a new client listener.
     *
     * @param listener the listener
     * @throws IllegalArgumentException if the listener was already registered
     */
    void registerClientListener(ClientListener listener);

    /**
     * Unregisters the given client listener.
     *
     * @param listener the listener
     * @throws IllegalArgumentException if the listener was not registered
     */
    void unregisterClientListener(ClientListener listener);

    /**
     * Unregisters all client listeners.
     */
    void unregisterAllClientListeners();

    /**
     * Sends a chat message to the Twitch channel.
     *
     * @param message the message to send
     */
    void sendMessage(String message);

    /**
     * Initiates the connection with the Twitch chat.
     *
     * @throws IOException if the socket or I/O connection failed, or if any of the inputs/outputs fail
     */
    void connect() throws IOException;

    /**
     * Notifies the client to stop.
     */
    void stop();

}

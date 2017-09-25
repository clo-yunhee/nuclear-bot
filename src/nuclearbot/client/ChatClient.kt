package nuclearbot.client

import nuclearbot.plugin.CommandExecutor

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
 * Public API interface for the bot client.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
interface ChatClient {

    /**
     * Returns the command which has the specified name.
     * The method returns null if there is no match.
     *
     * @param label the command name
     *
     * @return the command or null
     */
    fun getCommand(label: String): Command?

    /**
     * Registers a new command.
     *
     * @param label    the command name
     * @param executor the command executor
     *
     * @return the newly-created command
     *
     * @throws IllegalArgumentException if the command was already registered
     */
    fun registerCommand(label: String, executor: CommandExecutor): Command

    /**
     * Registers a new command.
     *
     * @param label    the command name
     * @param executor the command executor
     *
     * @return the newly-created command
     *
     * @throws IllegalArgumentException if the command was already registered
     */
    fun registerCommand(label: String,
                        executor: (ChatClient, String,
                                   Command, String,
                                   Array<String>) -> Boolean) = registerCommand(label, object : CommandExecutor {
        override fun onCommand(client: ChatClient, username: String,
                               command: Command, label: String,
                               args: Array<String>): Boolean =
                executor(client, username, command, label, args)
    })

    /**
     * Unregisters a command.
     *
     * @param name the command name
     *
     * @throws IllegalArgumentException if the command was not registered
     */
    fun unregisterCommand(name: String)

    /**
     * Unregisters all commands.
     */
    fun unregisterAllCommands()

    /**
     * Returns true if the command is registered, false otherwise.
     *
     * @param name the command name
     *
     * @return true if the command is registered, false otherwise
     */
    fun isCommandRegistered(name: String): Boolean

    /**
     * Registers a new client listener.
     *
     * @param listener the listener
     *
     * @throws IllegalArgumentException if the listener was already registered
     */
    fun registerClientListener(listener: ClientListener)

    /**
     * Unregisters the given client listener.
     *
     * @param listener the listener
     *
     * @throws IllegalArgumentException if the listener was not registered
     */
    fun unregisterClientListener(listener: ClientListener)

    /**
     * Unregisters all client listeners.
     */
    fun unregisterAllClientListeners()

    /**
     * Sends a chat message to the Twitch channel.
     *
     * @param message the message to send
     */
    fun sendMessage(message: String)

    /**
     * Initiates the connection with the Twitch chat.
     */
    fun connect()

    /**
     * Notifies the client to stop.
     */
    fun stop()

}

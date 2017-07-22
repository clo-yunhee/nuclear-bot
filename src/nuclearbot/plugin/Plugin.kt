package nuclearbot.plugin

import nuclearbot.client.ChatClient

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
 * Public API interface for a plugin.<br></br>
 * To write an external plugin, you must implement this interface
 * and include it in a JAR file.<br></br>
 * The class that implements `Plugin` must have no constructor.
 * Do your initialization in the `onLoad` method.<br></br>
 * For the plugin loader to actually recognize the file as a plugin,
 * you need to create a "plugin.properties" at the root of the archive.<br></br>
 * The only required key is "main", whose value must be the qualified name
 * of the class that implements the `Plugin` interface.<br></br>
 * You can also provide optional entries. As of August 19, 2016 there are
 * two optional entries supported:
 *
 *  * "name"
 *  * "version"
 * <br></br>
 * The name is used instead of the class name in the GUI.<br></br>
 * The version is not yet used by any thing.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
interface Plugin {

    /**
     * Name of the plugin.
     */
    val name: String get() = javaClass.name

    /**
     * Version of the plugin.
     */
    val version: String get() = "unknown"

    /**
     * Listener for a chat message.
     *
     * @param client   the Twitch client
     * @param username the sender's username
     * @param message  the message
     */
    fun onMessage(client: ChatClient, username: String, message: String)

    /**
     * Listener for client load.
     * Please avoid using the constructor, as it may cause problems with not-built-in plugins.
     *
     * @param client the Twitch client
     */
    fun onLoad(client: ChatClient)

    /**
     * Listener for client start.
     * This method is called before entering the client loop, if the connection was successful.
     * It is called again in case of soft-restart.
     *
     * @param client the Twitch client
     */
    fun onStart(client: ChatClient)

    /**
     * Listener for client stop.
     * This method is called before the connection is closed.
     * It is also called before soft-restarts.
     *
     * @param client the Twitch client
     */
    fun onStop(client: ChatClient)

}

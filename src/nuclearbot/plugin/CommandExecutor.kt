package nuclearbot.plugin

import nuclearbot.client.ChatClient
import nuclearbot.client.Command

import java.io.IOException

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
 * Public API interface for a chat command executor.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
@FunctionalInterface interface CommandExecutor {

    /**
     * Listener for the commands this executor is bound to.
     *
     * @param client   the Twitch client
     * @param username the sender's username
     * @param command  the command instance
     * @param label    the command name
     * @param args     the argument array
     *
     * @return true if the command succeeded
     *
     * @throws IOException delegate exception handling to the client
     */
    fun onCommand(client: ChatClient, username: String, command: Command, label: String, args: Array<String>): Boolean
}

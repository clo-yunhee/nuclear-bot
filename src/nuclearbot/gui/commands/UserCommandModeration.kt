package nuclearbot.gui.commands

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

import nuclearbot.client.ChatClient
import nuclearbot.client.Command
import nuclearbot.client.Moderators
import nuclearbot.plugin.CommandExecutor
import java.awt.EventQueue
import java.util.*

/**
 * Command registered by the GUI for moderation of user-defined commands.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class UserCommandModeration(private val commands: UserCommandManager) : CommandExecutor {

    override fun onCommand(client: ChatClient, username: String, command: Command, label: String, args: Array<String>): Boolean {
        if (!Moderators.isModerator(username)) {
            // fail silently
            return true
        }

        if (label.equals("cmdadd", ignoreCase = true) && args.size >= 3) {
            // usage: !cmdadd <name> <response>
            val response = Arrays.copyOfRange(args, 2, args.size).joinToString(" ")
            addCommand(client, username, args[1], response)
        } else if (label.equals("cmdrem", ignoreCase = true) && args.size >= 2) {
            // usage: !cmdrem <name>
            removeCommand(client, username, args[1])
        } else if (label.equals("cmdusage", ignoreCase = true) && args.size >= 3) {
            // usage: !cmdusage <name> <usage>
            val usage = Arrays.copyOfRange(args, 2, args.size).joinToString(" ")
            setUsage(client, username, args[1], usage)
        } else if (label.equals("cmddesc", ignoreCase = true) && args.size >= 3) {
            // usage: !cmddesc <name> <description>
            val description = Arrays.copyOfRange(args, 2, args.size).joinToString(" ")
            setDescription(client, username, args[1], description)
        } else {
            return false
        }

        return true
    }

    private fun addCommand(client: ChatClient, username: String, command: String, response: String) {
        if (client.isCommandRegistered(command)) {
            client.sendMessage("Command already exists, @" + username)
        } else {
            eventQueueCreate(command, "!" + command, "Nothing here!", response)

            client.sendMessage("Command created, @" + username)
        }
    }

    private fun removeCommand(client: ChatClient, username: String, command: String) {
        if (!commands.contains(command)) {
            client.sendMessage("Command doesn't exist, @" + username)
        } else {
            EventQueue.invokeLater { commands.removeCommand(command, true) }

            client.sendMessage("Command removed, @" + username)
        }
    }

    private fun setUsage(client: ChatClient, username: String, command: String, usage: String) {
        if (!client.isCommandRegistered(command)) {
            client.sendMessage("Command doesn't exist, @" + username)
        } else {
            commands.getCommand(command)!!.let { eventQueueCreate(command, usage, it.description, it.response) }

            client.sendMessage("Command usage updated, @" + username)
        }
    }

    private fun setDescription(client: ChatClient, username: String, command: String, description: String) {
        if (!client.isCommandRegistered(command)) {
            client.sendMessage("Command doesn't exist, @" + username)
        } else {
            commands.getCommand(command)!!.let { eventQueueCreate(command, it.usage, description, it.response) }

            client.sendMessage("Command description updated, @" + username)
        }
    }

    private fun eventQueueCreate(label: String, usage: String, description: String, response: String) {
        // UserCommandManager alters GUI components, so we need to call it from the EDT
        EventQueue.invokeLater { commands.createUserCommand(label, usage, description, response, true) }
    }

}

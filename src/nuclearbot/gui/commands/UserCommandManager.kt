package nuclearbot.gui.commands

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import nuclearbot.gui.NuclearBotGUI
import nuclearbot.util.Logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import javax.swing.JComboBox

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
 * Manager for user-defined commands.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class UserCommandManager(private val gui: NuclearBotGUI, private val combo: JComboBox<String>) {

    private val commands = mutableMapOf<String, CommandInfo>()

    // contains the commands that failed to register to the client
    private val failedRegister = mutableSetOf<String>()

    private val dialogs = gui.dialogs
    private val file = File(FILE_NAME).also {
        if (!it.exists() && it.mkdirs() && it.delete()) {
            try {
                if (it.createNewFile()) {
                    FileWriter(it, false).use { it.write("[]") }
                }
            } catch (e: IOException) {
                Logger.warning("(uCmd) Could not create \"$FILE_NAME\" for persistence.")
                Logger.warning("(uCmd) User-defined commands will only last one lifetime.")
                Logger.printStackTrace(e)
            }
        }
    }

    fun createUserCommand(name: String, usage: String, description: String, response: String, silent: Boolean) {
        if (commands.containsKey(name)) {
            Logger.info("(uCmd) Updating command \"$name\"...")
            if (gui.isClientRunning) {
                gui.client.unregisterCommand(name)
            }
            commands.remove(name)
            combo.removeItem(name)
        } else {
            Logger.info("(uCmd) Creating command \"$name\"...")
        }

        if (gui.isClientRunning) {
            if (!gui.client.isCommandRegistered(name)) {
                gui.client.registerCommand(name, UserCommand(response)).apply {
                    this.usage = usage
                    this.description = description
                }
                failedRegister.remove(name)
            } else {
                Logger.warning("(uCmd) Command \"$name\" is already registered.")
                if (!silent) {
                    dialogs.warning("Command \"$name\" has already been registered.", "Command already registered")
                }
                failedRegister.add(name)
            }
        } else {
            Logger.warning("(uCmd) Command \"$name\" will be registered when the client starts.")
            if (!silent) {
                dialogs.warning("Command \"$name\" will be registered when the client starts.", "Client is not running")
            }
        }

        commands.put(name, CommandInfo(name, usage, description, response))
        combo.addItem(name)

        saveCommands(silent)

        Logger.info("(uCmd) Command \"$name\" created successfully.")
        if (!silent) {
            dialogs.info("Command \"$name\" created.", "Command created")
        }
    }

    private fun saveCommands(silent: Boolean) {
        try {
            FileWriter(file, false).use { Gson().toJson(commands.values, Collection::class.java, it) }
        } catch (e: IOException) {
            Logger.error("(uCmd) Couldn't save persistent user command:")
            Logger.printStackTrace(e)
            if (!silent) {
                dialogs.error("Couldn't save persistent user command. Check console for details.", "Couldn't save config")
            }
        }

    }

    fun loadCommands() {
        commands.clear()

        try {
            FileReader(file).use { reader ->
                Gson().fromJson<List<CommandInfo>>(reader, object : TypeToken<List<CommandInfo>>() {}.type).forEach {
                    createUserCommand(it.name, it.usage, it.description, it.response, true)
                }
            }
        } catch (e: JsonSyntaxException) {
            Logger.error("(uCmd) Error while loading user commands:")
            Logger.printStackTrace(e)
            dialogs.error("Error in the user commands configuration. Check console for details.", "JSON syntax error")
        } catch (e: IOException) {
            Logger.error("(uCmd) Error while loading user commands:")
            Logger.printStackTrace(e)
            dialogs.error("Error in the user commands configuration. Check console for details.", "JSON syntax error")
        }

    }

    fun getCommand(name: String): CommandInfo? {
        return commands[name]
    }

    fun removeCommand(name: String, silent: Boolean) {
        if (commands.containsKey(name)) {
            if (gui.isClientRunning && !failedRegister.contains(name))
                gui.client.unregisterCommand(name)

            commands.remove(name)
            combo.removeItem(name)
            if (!silent) {
                Logger.info("(uCmd) Command \"$name\" removed successfully.")
                dialogs.info("Command \"$name\" removed successfully.", "Command removed")
            }
        } else {
            if (!silent) {
                Logger.info("(uCmd) Command \"$name\" is not a user command.")
                dialogs.warning("Command \"$name\" is not a user command.", "Not a user command")
            }
        }
    }

    operator fun contains(command: String): Boolean {
        return commands.containsKey(command)
    }

    fun registerCommands() {
        failedRegister.clear()
        commands.forEach { name, command ->
            val executor = UserCommand(command.response)

            try {
                gui.client.registerCommand(command.name, executor).apply {
                    usage = command.usage
                    description = command.description
                }
            } catch (e: IllegalArgumentException) {
                Logger.warning("(uCmd) User command \"${command.name}\" was already registered by something else.")
                dialogs.warning("User command \"${command.name}\" was already registered by something else.", "Command already registered")
                failedRegister.add(name)
            }
        }
    }

    class CommandInfo(val name: String, val usage: String, val description: String, val response: String)

    companion object {
        private const val FILE_NAME = "commands.json"
    }

}

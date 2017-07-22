package nuclearbot.gui.components.commands

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.gui.commands.UserCommandManager
import nuclearbot.gui.commands.UserCommandModeration
import nuclearbot.gui.utils.VerticalLayout
import nuclearbot.plugin.CommandExecutor
import java.awt.FlowLayout
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

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
 * The GUI panel for the command edition panel.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class CommandEditPanel(private val gui: NuclearBotGUI) : JPanel(VerticalLayout()) {

    private val commandCombo = JComboBox<String>().apply {
        isEditable = true

        addActionListener { updateCommandInfo() }
        addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED)
                updateCommandInfo()
        }
        addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                updateCommandInfo()
            }
        })
    }

    private val usageField = JTextField(12)
    private val descriptionField = JTextField(16)
    private val responseField = JTextField(16)

    private val commands: UserCommandManager

    private val modCommands: CommandExecutor

    init {
        border = BorderFactory.createTitledBorder("Create/Update")

        val namePanel = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Name:</u></html>"))
            add(commandCombo)
        }

        val usagePanel = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Usage:</u></html>"))
            add(usageField)
        }

        val descriptionPanel = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Description:</u></html>"))
            add(descriptionField)
        }

        val responsePanel = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Response:</u></html>"))
            add(responseField)
        }

        val submitPanel = JPanel(FlowLayout()).apply {
            val createButton = JButton("Create/Update").apply { addActionListener { createCommand() } }
            val removeButton = JButton("Remove").apply { addActionListener { removeCommand() } }
            val helpButton = JButton("Help...").apply { addActionListener { gui.dialogs.info(HELP_TEXT, "Command creation help") } }

            add(createButton)
            add(removeButton)
            add(helpButton)
        }

        commands = UserCommandManager(gui, commandCombo)
        modCommands = UserCommandModeration(commands)

        add(namePanel)
        add(usagePanel)
        add(descriptionPanel)
        add(responsePanel)
        add(submitPanel)

        commands.loadCommands()
    }

    fun registerCommands() {
        gui.client.run {
            registerCommand("cmdadd", modCommands).apply {
                usage = "!cmdadd <name> <response>"
                description = "Adds a command with the given name and response."
            }

            registerCommand("cmdrem", modCommands).apply {
                usage = "!cmdrem <name>"
                description = "Removes a command with the given name."
            }

            registerCommand("cmdusage", modCommands).apply {
                usage = "!cmdusage <name> <usage>"
                description = "Sets the command usage."
            }

            registerCommand("cmddesc", modCommands).apply {
                usage = "!cmddesc <name> <usage>"
                description = "Sets the command description."
            }
        }

        commands.registerCommands()
    }

    private fun updateCommandInfo() {
        val label = commandCombo.selectedItem.toString().trim().toLowerCase()
        val command = commands.getCommand(label)

        usageField.text = command?.usage ?: ""
        descriptionField.text = command?.description ?: ""
        responseField.text = command?.response ?: ""
    }

    private fun createCommand() {
        val name = (commandCombo.selectedItem as String).trim().toLowerCase()
        if (name.isNotEmpty()) {
            val usage = usageField.text.trim()
            val description = descriptionField.text.trim()
            val response = responseField.text.trim()

            commands.createUserCommand(name, usage, description, response, false)
        }
    }

    private fun removeCommand() {
        val name = (commandCombo.selectedItem as String).trim().toLowerCase()
        if (name.isNotEmpty()) {
            commands.removeCommand(name, false)
        }
    }

    companion object {

        private const val HELP_TEXT =
                """The convention for usage format is the following:
- angle brackets for required arguments;
- square brackets for optional arguments;
but the GUI creator doesn't support optional arguments yet.
Please only write arguments without the command in the usage field.

To include arguments in the response, use "${"$"}n" or "{${"$"}n}", where n is the argument index.
The zero-th argument is the sender's username.
For instance, to create a hug command, put "!hug <target>" in usage and "$0 hugs $1." in response.

By default the commands are created persistent, which means they will be created again on start.
Non-persistent commands are lost once the bot stops or restarts."""

    }

}

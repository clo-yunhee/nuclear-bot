package nuclearbot.gui.components.commands

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.gui.utils.VerticalLayout
import java.awt.FlowLayout
import java.awt.event.ItemEvent
import javax.swing.BorderFactory
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

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
 * The GUI panel for the command overview panel.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class CommandOverviewPanel(private val m_gui: NuclearBotGUI) : JPanel(VerticalLayout()) {

    private val commandList = JComboBox<String>().apply {
        isEditable = false
        addItemListener { e ->
            if (e.stateChange == ItemEvent.SELECTED)
                updateCommandInfo()
        }
    }

    private val usageLabel = JLabel("(no such command)").apply {
        toolTipText = "Angle brackets for required arguments\nSquare brackets for optional arguments"
    }

    private val descriptionLabel = JLabel("(no such command)")

    init {
        border = BorderFactory.createTitledBorder("Overview")

        val namePanel = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Name:</u></html>"))
            add(commandList)
        }

        val usagePanel = JPanel(FlowLayout()).apply {
            val usagePrefixLabel = JLabel("<html><u>Usage:</u></html>").apply {
                toolTipText = "Angle brackets for required arguments\nSquare brackets for optional arguments"
            }

            add(usagePrefixLabel)
            add(usageLabel)
        }

        val descriptionPanel = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Description:</u></html>"))
            add(descriptionLabel)
        }

        add(namePanel)
        add(usagePanel)
        add(descriptionPanel)
    }

    fun addCommand(name: String) {
        commandList.addItem(name)
        updateCommandInfo()
    }

    fun removeCommand(name: String) {
        commandList.removeItem(name)
        updateCommandInfo()
    }

    fun clearCommandList() {
        commandList.removeAllItems()
    }

    private fun updateCommandInfo() {
        val label = commandList.selectedItem as String
        val command = m_gui.client.getCommand(label)

        val usage = command?.usage ?: "(no such command)"
        val description = command?.description ?: "(no such command)"

        usageLabel.text = usage
        descriptionLabel.text = description
    }

}

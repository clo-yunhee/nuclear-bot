package nuclearbot.gui.components

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.gui.utils.VerticalLayout
import nuclearbot.util.JavaWrapper
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.EmptyBorder

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
 * The GUI panel for the status tab.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class StatusPanel(gui: NuclearBotGUI) : JPanel(VerticalLayout()) {

    private val statusLabel = JLabel("Not running").apply {
        border = EmptyBorder(0, 0, 0, 3)
        font = font.deriveFont(Font.ITALIC)
    }

    private val startButton = JButton("Start").apply {
        isEnabled = true
        addActionListener {
            gui.startClient()
        }
    }

    private val stopButton = JButton("Stop").apply {
        isEnabled = false
        addActionListener {
            gui.doRestartClient = false
            gui.stopClient()
        }
    }

    private val restartButton = JButton("Restart").apply {
        isEnabled = false
        addActionListener {
            gui.doRestartClient = true
            gui.stopClient()
        }
    }

    private val pluginLabel = JLabel().apply {
        font = font.deriveFont(Font.PLAIN)
        horizontalAlignment = SwingConstants.CENTER
        componentPopupMenu = gui.popupMenu
    }

    init {
        val controls = JPanel(FlowLayout()).apply {
            add(statusLabel)
            add(startButton)
            add(stopButton)
            add(restartButton)
        }
        val currentPlugin = JPanel(FlowLayout()).apply {
            add(JLabel("<html><u>Current plugin:</u></html>"))
            add(pluginLabel)
        }

        add(controls)
        add(currentPlugin)
    }

    fun setStatusText(text: String) {
        statusLabel.text = text
    }

    fun setPluginText(text: String, tooltipText: String) {
        pluginLabel.text = text
        pluginLabel.toolTipText = tooltipText
    }

    var isStartEnabled by JavaWrapper(startButton::isEnabled, startButton::setEnabled)
    var isStopEnabled by JavaWrapper(stopButton::isEnabled, stopButton::setEnabled)
    var isRestartEnabled by JavaWrapper(restartButton::isEnabled, restartButton::setEnabled)

}

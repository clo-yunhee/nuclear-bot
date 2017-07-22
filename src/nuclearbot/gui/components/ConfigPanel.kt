package nuclearbot.gui.components

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.gui.plugin.configuration.HasConfigPanel
import nuclearbot.gui.plugin.configuration.PluginConfigPanel
import nuclearbot.gui.utils.VerticalLayout
import nuclearbot.plugin.Plugin
import nuclearbot.util.Config
import nuclearbot.util.Logger
import java.awt.FlowLayout
import java.io.IOException
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane

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
 * GUI panel for the configuration tab.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ConfigPanel(private val gui: NuclearBotGUI) : JScrollPane() {

    private val configPanel = JPanel(VerticalLayout()).apply {
        val clientPanel = PluginConfigPanel("twitch").apply {
            addTextField("Twitch user name", "user", "")
            addPasswordField("Twitch OAuth token", "oauth_key", "")
        }

        val buttonsPanel = JPanel(FlowLayout()).apply {
            val resetButton = JButton("Reset fields").apply { addActionListener { resetFields() } }
            val saveButton = JButton("Save config").apply { addActionListener { saveConfig() } }
            val reloadButton = JButton("Reload config").apply { addActionListener { reloadConfig() } }

            add(resetButton)
            add(saveButton)
            add(reloadButton)
        }

        add(buttonsPanel)
        add(clientPanel)
    }

    private var pluginPanel: PluginConfigPanel? = null

    init {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        setViewportView(configPanel)

        resetFields()
    }

    fun setPluginPanel(handle: Plugin) {
        pluginPanel?.let(configPanel::remove)
        if (handle is HasConfigPanel) {
            try {
                (handle as HasConfigPanel).getConfigPanel().resetFields()
                configPanel.add(pluginPanel)
            } catch (e: Exception) {
                Logger.error("(GUI) Exception caught while changing plugin configuration panel:")
                Logger.printStackTrace(e)
                gui.dialogs.error("Exception caught while changing plugin configuration panel. Check console for details.",
                        "Exception while updating config panel")
            }
        } else {
            pluginPanel = null
        }

    }

    private fun setFields() {
        synchronized(configPanel.treeLock) {
            configPanel.components.forEach {
                (it as? PluginConfigPanel)?.saveFields()
            }
        }
    }

    private fun resetFields() {
        synchronized(configPanel.treeLock) {
            configPanel.components.forEach {
                (it as? PluginConfigPanel)?.resetFields()
            }
        }
    }

    private fun saveConfig() {
        try {
            setFields()
            Config.saveConfig()
            gui.dialogs.info("Config saved successfully.", "Config saved")
        } catch (e: IOException) {
            Logger.error("(GUI) Exception while saving config:")
            Logger.printStackTrace(e)
            gui.selectTab(NuclearBotGUI.TAB_CONSOLE)
            gui.dialogs.error("Exception while saving config. Check console for details.", "Couldn't save config")
        }

    }

    private fun reloadConfig() {
        try {
            Config.reloadConfig()
            gui.dialogs.info("Config reloaded successfully.", "Config reloaded")
        } catch (e: IOException) {
            Logger.error("(GUI) Exception while reloading config:")
            Logger.printStackTrace(e)
            gui.selectTab(NuclearBotGUI.TAB_CONSOLE)
            gui.dialogs.error("Exception while reloading config. Check console for details.", "Couldn't reload config")
        }

    }

}

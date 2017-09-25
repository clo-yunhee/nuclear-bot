package nuclearbot.gui.components.plugins

import nuclearbot.builtin.DummyPlugin
import nuclearbot.gui.NuclearBotGUI
import nuclearbot.gui.utils.VerticalLayout
import nuclearbot.util.Logger
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Font
import java.io.File
import javax.swing.*
import kotlin.reflect.jvm.jvmName

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
 * The GUI panel for loading plugins.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class PluginPanel(private val gui: NuclearBotGUI) : JScrollPane() {

    private val pluginLoader = gui.pluginLoader

    private val fileDialog = PluginFileDialog(gui.container)

    private val pluginLabel = JLabel().apply {
        horizontalAlignment = SwingConstants.CENTER
        font = font.deriveFont(Font.PLAIN)
        componentPopupMenu = gui.popupMenu
    }

    private val pathTextField = JTextField(30).apply {
        componentPopupMenu = gui.popupMenu
        addActionListener { chooseFile() }
    }

    private var builtinCombo = JComboBox(pluginLoader.builtinPlugins).apply {
        isEditable = false
        selectedItem = DummyPlugin::class.jvmName
        font = font.deriveFont(Font.PLAIN)
    }

    init {
        val container = JPanel(VerticalLayout()).apply {
            val loadedPluginPanel = JPanel(FlowLayout()).apply {
                border = BorderFactory.createTitledBorder("Current plugin")
                add(pluginLabel, BorderLayout.CENTER)
            }

            val externalPanel = JPanel(FlowLayout()).apply {
                val browseButton = JButton("Browse...").apply {
                    addActionListener { chooseFile() }
                }
                val externalButton = JButton("Load").apply {
                    addActionListener { loadExternal() }
                }

                border = BorderFactory.createTitledBorder("Load an external plugin")
                add(browseButton)
                add(pathTextField)
                add(externalButton)
            }

            val builtinPanel = JPanel(FlowLayout()).apply {
                val builtinButton = JButton("Load").apply {
                    addActionListener { loadBuiltin() }
                }

                border = BorderFactory.createTitledBorder("Load a built-in plugin")
                add(builtinCombo)
                add(builtinButton)
            }

            add(loadedPluginPanel)
            add(externalPanel)
            add(builtinPanel)
        }

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        setViewportView(container)
    }

    fun setPluginText(text: String, tooltipText: String) {
        pluginLabel.text = text
        pluginLabel.toolTipText = tooltipText
    }

    fun dispose() {
        fileDialog.dispose()
    }

    private fun chooseFile() {
        Logger.info("(GUI) Opening plugin file dialog...")

        fileDialog.isVisible = true
        val filename = fileDialog.file
        if (filename != null) {
            pathTextField.text = File(fileDialog.directory, filename).absolutePath
        }
    }

    private fun loadExternal() {
        val file = File(pathTextField.text)
        if (file.isFile) {
            gui.pluginChanged(if (pluginLoader.loadPlugin(file)) pluginLoader.plugin else null)
        } else {
            Logger.error("(GUI) Provided path \"${file.absolutePath}\" that wasn't a file.")
            gui.dialogs.error("This is not a file!", "Not a file")
        }
    }

    private fun loadBuiltin() {
        if (pluginLoader.loadPlugin(builtinCombo.selectedItem as String))
            gui.pluginChanged(pluginLoader.plugin)
        else
            gui.pluginChanged(null)
    }

}

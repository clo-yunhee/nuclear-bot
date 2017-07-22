package nuclearbot.gui.plugin.configuration

import nuclearbot.gui.plugin.configuration.components.ConfigEntry
import nuclearbot.gui.plugin.configuration.components.EntryCheckBox
import nuclearbot.gui.plugin.configuration.components.EntryPasswordField
import nuclearbot.gui.plugin.configuration.components.EntryTextField
import nuclearbot.gui.utils.VerticalLayout
import nuclearbot.util.Config
import java.awt.Component
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
 * Custom panel for configuration.<br></br>
 * Does not support adding components using the add methods.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class PluginConfigPanel(private val prefix: String) : JPanel(VerticalLayout()) {

    private val entries = mutableSetOf<ConfigEntry>()

    private fun addComponent(label: String, entry: ConfigEntry) {
        val rowPanel = JPanel().apply {
            add(JLabel(label + ':'))
            add(entry.getComponent<Component>())
        }
        super.addImpl(rowPanel, null, -1)
        entries.add(entry)
    }

    override fun addImpl(component: Component, constraints: Any, index: Int) {
        throw IllegalStateException("Tried to add a component using add method. Please use the addXXX(String, String) methods")
    }

    /**
     * Sets the fields back to the current configuration.
     */
    fun resetFields() {
        entries.forEach {
            val key = prefix + '_' + it.key
            it.value = Config[key, it.defaultValue]
        }
    }

    /**
     * Sets the configuration to the values of the fields.
     */
    fun saveFields() {
        entries.forEach {
            val key = prefix + '_' + it.key
            Config[key] = it.value
        }
    }

    /**
     * Adds a text field with the specified label,
     * associated to the given configuration key and default value.
     *
     * @param label        the label to display next to this component
     * @param key          the configuration key associated to this field
     * @param defaultValue the default value if the entry does not exist
     */
    fun addTextField(label: String, key: String, defaultValue: String) {
        addComponent(label, EntryTextField(key, defaultValue))
    }

    /**
     * Adds a password field with the specified label,
     * associated to the given configuration key and default value.
     *
     * @param label        the label to display next to this component
     * @param key          the configuration key associated to this field
     * @param defaultValue the default value if the entry does not exist
     */
    fun addPasswordField(label: String, key: String, defaultValue: String) {
        addComponent(label, EntryPasswordField(key, defaultValue))
    }

    /**
     * Adds a check box with the specified label,
     * associated to the given configuration key and default value.
     *
     * @param label        the label to display next to this component
     * @param key          the configuration key associated to this field
     * @param defaultValue the default value if the entry does not exist
     */
    fun addCheckBox(label: String, key: String, defaultValue: Boolean) {
        addComponent(label, EntryCheckBox(key, defaultValue.toString()))
    }

}

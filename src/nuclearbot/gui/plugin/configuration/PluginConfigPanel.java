package nuclearbot.gui.plugin.configuration;

import nuclearbot.gui.plugin.configuration.components.ConfigEntry;
import nuclearbot.gui.plugin.configuration.components.EntryCheckBox;
import nuclearbot.gui.plugin.configuration.components.EntryPasswordField;
import nuclearbot.gui.plugin.configuration.components.EntryTextField;
import nuclearbot.gui.utils.VerticalLayout;
import nuclearbot.util.Config;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

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
 * Custom panel for configuration.<br>
 * Does not support adding components using the add methods.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public final class PluginConfigPanel extends JPanel {

    private static final long serialVersionUID = 7023349359010074929L;

    private final String m_prefix;
    private final Set<ConfigEntry> m_entries;

    /**
     * Constructs a configuration panel with
     * the specified config key prefix.
     *
     * @param prefix the key prefix
     */
    public PluginConfigPanel(final String prefix)
    {
        m_prefix = prefix;
        m_entries = new HashSet<>();

        setLayout(new VerticalLayout());
    }

    private void addComponent(final String label, final ConfigEntry entry)
    {
        final JPanel rowPanel = new JPanel();
        {
            rowPanel.add(new JLabel(label + ':'));
            rowPanel.add(entry.getComponent());
        }
        super.addImpl(rowPanel, null, -1);
        m_entries.add(entry);
    }

    @Override
    protected void addImpl(final Component component, final Object constraints, final int index)
    {
        throw new IllegalStateException("Tried to add a component using add method. Please use the addXXX(String, String) methods");
    }

    /**
     * Sets the fields back to the current configuration.
     */
    public void resetFields()
    {
        for (final ConfigEntry entry : m_entries)
        {
            final String key = m_prefix + '_' + entry.getKey();
            final String defaultValue = entry.getDefaultValue();
            entry.setValue(Config.get(key, defaultValue));
        }
    }

    /**
     * Sets the configuration to the values of the fields.
     */
    public void saveFields()
    {
        for (final ConfigEntry entry : m_entries)
        {
            final String key = m_prefix + '_' + entry.getKey();
            Config.set(key, entry.getValue());
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
    public void addTextField(final String label, final String key, final String defaultValue)
    {
        addComponent(label, new EntryTextField(key, defaultValue));
    }

    /**
     * Adds a password field with the specified label,
     * associated to the given configuration key and default value.
     *
     * @param label        the label to display next to this component
     * @param key          the configuration key associated to this field
     * @param defaultValue the default value if the entry does not exist
     */
    public void addPasswordField(final String label, final String key, final String defaultValue)
    {
        addComponent(label, new EntryPasswordField(key, defaultValue));
    }

    /**
     * Adds a check box with the specified label,
     * associated to the given configuration key and default value.
     *
     * @param label        the label to display next to this component
     * @param key          the configuration key associated to this field
     * @param defaultValue the default value if the entry does not exist
     */
    public void addCheckBox(final String label, final String key, final boolean defaultValue)
    {
        addComponent(label, new EntryCheckBox(key, Boolean.toString(defaultValue)));
    }

}

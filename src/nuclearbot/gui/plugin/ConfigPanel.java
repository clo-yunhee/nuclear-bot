package nuclearbot.gui.plugin;

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
public final class ConfigPanel extends JPanel {

    private static final long serialVersionUID = 7023349359010074929L;

    private final String m_prefix;
    private final Set<JComponent> m_fields;

    /**
     * Constructs a configuration panel with
     * the specified config key prefix.
     *
     * @param prefix the key prefix
     */
    public ConfigPanel(final String prefix)
    {
        m_prefix = prefix;
        m_fields = new HashSet<>();

        setLayout(new VerticalLayout());
    }

    private Object getValueFor(final JComponent component)
    {
        if (component instanceof JTextField)
        {
            return ((JTextField) component).getText();
        }
        else if (component instanceof JCheckBox)
        {
            return ((JCheckBox) component).isSelected();
        }
        else
        {
            throw new IllegalArgumentException("Unsupported config field type " + component.getClass().getName());
        }
    }

    private void setValueFor(final JComponent component, final String value)
    {
        if (component instanceof JTextField)
        {
            ((JTextField) component).setText(value);
        }
        else if (component instanceof JCheckBox)
        {
            ((JCheckBox) component).setSelected(Boolean.valueOf(value));
        }
        else
        {
            throw new IllegalArgumentException("Unsupported config field type " + component.getClass().getName());
        }
    }

    private void addComponent(final JComponent component, final String label, final String key, final String defaultValue)
    {
        final JPanel rowPanel = new JPanel();
        {
            component.putClientProperty("config_key", key);
            rowPanel.add(new JLabel(label + ':'));
            rowPanel.add(component);
        }
        super.addImpl(rowPanel, null, -1);
        m_fields.add(component);
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
        for (final JComponent component : m_fields)
        {
            final String key = m_prefix + '_' + component.getClientProperty("config_key");
            final String defaultValue = (String) component.getClientProperty("config_defaultValue");
            setValueFor(component, Config.get(key, defaultValue));
        }
    }

    /**
     * Sets the configuration to the values of the fields.
     */
    public void saveFields()
    {
        for (final JComponent component : m_fields)
        {
            final String key = m_prefix + '_' + component.getClientProperty("config_key");
            Config.set(key, String.valueOf(getValueFor(component)));
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
        addComponent(new JTextField(12), label, key, defaultValue);
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
        addComponent(new JPasswordField(12), label, key, defaultValue);
    }

    /**
     * Adds a check box with the specified label,
     * associated to the given configuration key and default value.
     *
     * @param label        the label to display next to this component
     * @param key          the configuration key associated to this field
     * @param defaultValue the default value if the entry does not exist
     */
    public void addCheckBox(final String label, final String key, final String defaultValue)
    {
        addComponent(new JCheckBox(), label, key, defaultValue);
    }

}

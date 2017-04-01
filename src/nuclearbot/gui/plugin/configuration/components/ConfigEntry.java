package nuclearbot.gui.plugin.configuration.components;

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

import java.awt.*;

/**
 * Wrapper around a component in a config panel.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public abstract class ConfigEntry {

    private final Component m_component;

    private final String m_key;
    private final String m_default;

    public ConfigEntry(final Component component, final String key, final String defaultValue) {
        m_component = component;
        m_key = key;
        m_default = defaultValue;
    }

    /**
     * Returns the wrapped component.
     *
     * @return the wrapped component with the right type
     */
    @SuppressWarnings("unchecked")
    public <T extends Component> T getComponent() {
        return (T) m_component;
    }

    /**
     * Returns the key of this config entry.
     *
     * @return the entry key
     */
    public String getKey() {
        return m_key;
    }

    /**
     * Returns the default value of this config entry.
     *
     * @return the entry value
     */
    public String getDefaultValue() {
        return m_default;
    }

    /**
     * Returns the value of this config entry.
     *
     * @return the entry value
     */
    public abstract String getValue();

    /**
     * Sets the value of this config entry.
     *
     * @param value the new value
     */
    public abstract void setValue(String value);

}

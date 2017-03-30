package nuclearbot.gui.plugin.configuration.components;

import javax.swing.*;

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
 * Config panel component, password text field.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class EntryPasswordField extends ConfigEntry {

    private final JPasswordField m_field;

    public EntryPasswordField(final String key, final String defaultValue)
    {
        super(new JPasswordField(12), key, defaultValue);

        m_field = getComponent();
    }

    @Override @SuppressWarnings("deprecated") public String getValue()
    {
        return m_field.getText();
    }

    @Override public void setValue(final String value)
    {
        m_field.setText(value);
    }

}

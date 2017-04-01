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
 * Config panel component, check box.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class EntryCheckBox extends ConfigEntry {

    private final JCheckBox m_checkBox;

    public EntryCheckBox(final String key, final String defaultValue) {
        super(new JCheckBox(), key, defaultValue);

        m_checkBox = getComponent();
    }

    @Override
    public String getValue() {
        return Boolean.toString(m_checkBox.isSelected());
    }

    @Override
    public void setValue(final String value) {
        m_checkBox.setSelected(Boolean.getBoolean(value));
    }

}

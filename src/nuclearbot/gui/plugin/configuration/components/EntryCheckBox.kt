package nuclearbot.gui.plugin.configuration.components

import javax.swing.JCheckBox

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
 * Config panel component, check box.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class EntryCheckBox(key: String, defaultValue: String) : ConfigEntry(JCheckBox(), key, defaultValue) {

    private val f = getComponent<JCheckBox>()

    override var value: String
        get() = f.isSelected.toString()
        set(value) {
            f.isSelected = value.toBoolean()
        }

}

package nuclearbot.gui.plugin.configuration.components

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

import java.awt.Component

/**
 * Wrapper around a component in a config panel.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
abstract class ConfigEntry(private val component: Component, val key: String, val defaultValue: String) {

    /**
     * Returns the wrapped component.

     * @return the wrapped component with the right type
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Component> getComponent(): T {
        return component as T
    }

    /**
     * The value of this config entry.
     */
    abstract var value: String

}

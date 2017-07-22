package nuclearbot.plugin

import java.io.File

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
 * Public API interface for the plugin loader.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
interface PluginLoader {

    /**
     * Returns the current loaded plugin.

     * @return the current loaded plugin
     */
    val plugin: JavaPlugin

    /**
     * Loads a plugin from class name. Class must belong to classpath.
     * To load a plugin from a jar file, see `loadPlugin(File file)`.
     *
     * @param className the plugin's full class name
     *
     * @return true if the plugin was loaded succesfully, false otherwise
     */
    fun loadPlugin(className: String): Boolean

    /**
     * Loads a plugin from jar file.
     * The method first seeks the class name inside the plugininfo file inside the jar,
     * then calls `loadPlugin(File file)`.
     *
     * @param file the jar file to load
     *
     * @return true if the plugin was loaded succesfully, false otherwise
     */
    fun loadPlugin(file: File): Boolean

    /**
     * Returns an array filled with the class names of all built-in plugins.
     * The built-in plugins are found in package and sub-packages
     * nuclearbot.builtin, in constructor.
     *
     * @return built-in plugins' class names
     */
    val builtinPlugins: Array<String>

}

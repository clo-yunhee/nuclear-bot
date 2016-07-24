package nuclearbot.plugin;

import java.io.File;

/*
 * Copyright (C) 2016 NuclearCoder
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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Public API interface for the plugin loader.
 */
public interface PluginLoader {

	/**
	 * Returns the current loaded plugin.
	 * @return the current loaded plugin
	 */
	public Plugin getPlugin();
	
	/**
	 * Loads a plugin from class name with the specified classloader. To load a plugin from a jar file,
	 * see <code>loadPlugin(File file, String className)</code> or <code>loadPlugin(File file)</code>.
	 * @param className the plugin's full class name
	 * @return true if the plugin was loaded succesfully, false otherwise
	 */
	public boolean loadPlugin(String className);
	
	/**
	 * Loads a plugin from jar file and given class name.
	 * The method first adds the jar file to the system classloader, then loads from class name.
	 * @param file the jar file to load
	 * @param the plugin's full class name
	 * @return true if the plugin was loaded succesfully, false otherwise
	 */
	public boolean loadPlugin(File file, String className);
	
	/**
	 * Loads a plugin from jar file.
	 * The method first seeks the class name inside the plugininfo file inside the jar,
	 * then calls <code>loadPlugin(File file, String className)</code>.
	 * @param file the jar file to load
	 * @return true if the plugin was loaded succesfully, false otherwise
	 */
	public boolean loadPlugin(File file);

	/**
	 * Returns an array filled with the class names of all built-in plugins.
	 * The built-in plugins are found in package and sub-packages
	 * nuclearbot.builtin, in constructor.
	 * @return built-in plugins' class names
	 */
	public String[] getBuiltinPlugins();
	
}

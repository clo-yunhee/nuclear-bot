package nuclearbot.plugin;

import nuclearbot.client.ChatListener;

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
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)<br>
 * <br>
 * Plugin wrapper for a Plugin with a built-in flag.
 */
public abstract class JavaPlugin implements Plugin {

	private final Plugin m_plugin;
	private final boolean m_builtin;
	
	private final String m_className;
	
	public JavaPlugin(final Plugin plugin, final boolean builtin)
	{
		m_plugin = plugin;
		m_builtin = builtin;
		
		m_className = plugin.getClass().getName();
	}
	
	@Override
	public ChatListener init()
	{
		return m_plugin.init();
	}
	
	/**
	 * Returns the plugin name.
	 * @return the plugin name
	 */
	public abstract String getName();
	
	/**
	 * Returns the plugin version.
	 * @return the plugin version
	 */
	public abstract String getVersion();
	
	/**
	 * Returns the plugin main class name.
	 * @return the plugin main class name
	 */
	public String getClassName()
	{
		return m_className;
	}
	
	/**
	 * Returns true if the plugin was loaded like a built-in plugin.
	 * @return the plugin built-in status
	 */
	public boolean isBuiltin()
	{
		return m_builtin;
	}

}

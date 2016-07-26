package nuclearbot.plugin;

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
 * Plugin wrapper for a Plugin loaded from classpath.
 */
public class ClasspathPlugin extends JavaPlugin {
	
	private final String m_className;
	
	public ClasspathPlugin(final Plugin plugin, final String className)
	{
		super(plugin, true);
		m_className = className;
	}

	@Override
	public String getName()
	{
		return m_className;
	}

	@Override
	public String getVersion()
	{
		return null;
	}

}

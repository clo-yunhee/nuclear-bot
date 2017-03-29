package nuclearbot.plugin;

import java.util.Properties;

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
 * Plugin wrapper for a Plugin loaded from a JAR file.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class JarPlugin extends ImplJavaPlugin {

    private final String m_name;
    private final String m_version;

    public JarPlugin(final Plugin plugin, final Properties properties)
    {
        super(plugin, false);
        m_name = properties.getProperty("name", getClassName());
        m_version = properties.getProperty("version", "external");
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public String getVersion()
    {
        return m_version;
    }

}

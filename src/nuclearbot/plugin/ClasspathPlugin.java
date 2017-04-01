package nuclearbot.plugin;

import java.lang.reflect.Field;

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
 * Plugin wrapper for a Plugin loaded from classpath.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ClasspathPlugin extends ImplJavaPlugin {

    private final String m_name;
    private final String m_version;

    public ClasspathPlugin(final Plugin plugin) {
        super(plugin, true);

        String name;
        try {
            final Field field = plugin.getClass().getField("PLUGIN_NAME");
            name = (String) field.get(plugin);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            name = plugin.getClass().getName();
        }
        m_name = name;

        String version;
        try {
            final Field field = plugin.getClass().getField("PLUGIN_VERSION");
            version = (String) field.get(plugin);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            version = "";
        }
        m_version = version;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String getVersion() {
        return m_version;
    }

}

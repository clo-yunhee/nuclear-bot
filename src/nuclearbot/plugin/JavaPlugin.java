package nuclearbot.plugin;

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
 * Public API interface for a plugin wrapper with name, version, and built-in flag.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface JavaPlugin extends Plugin {

    /**
     * Returns the plugin name.
     *
     * @return the plugin name
     */
    String getName();

    /**
     * Returns the plugin version.
     *
     * @return the plugin version
     */
    String getVersion();

    /**
     * Returns the plugin main class name.
     *
     * @return the plugin main class name
     */
    String getClassName();

    /**
     * Returns true if the plugin was loaded like a built-in plugin.
     *
     * @return the plugin built-in status
     */
    boolean isBuiltin();

    /**
     * Returns the underlying Plugin instance wrapped by this object.
     *
     * @return the unwrapped plugin instance
     */
    Plugin getHandle();

}

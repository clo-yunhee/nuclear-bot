package nuclearbot.util

import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

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
 * Static class for configuration.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
object Config {

    private val configFile = File("config.properties").apply {
        if (isDirectory) {
            Logger.error("Couldn't write to config.properties in the program's directory.")
            System.exit(1)
        }
        if (!exists() && mkdirs() && delete()) { // create an empty config file if it doesn't exist
            try {
                createNewFile()
            } catch (e: IOException) {
                Logger.error("An error occurred while creating config file.")
                Logger.printStackTrace(e)
                System.exit(1)
            }
        }
    }

    private val prop = Properties().apply {
        try {
            FileReader(configFile).use(this::load)
        } catch (e: IOException) {
            Logger.error("An error occurred while loading config.")
            Logger.printStackTrace(e)
        }
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread(ConfigShutdownHook()))
    }

    /**
     * Writes the configuration into the file.

     * @throws IOException if the file exists but is a directory
     *                     rather than a regular file, does not exist but cannot be
     *                     created, or cannot be opened for any other reason
     */
    @Throws(IOException::class)
    fun saveConfig() = FileWriter(configFile).use {
        prop.store(it,
                "please do not attempt to edit anything manually unless asked otherwise")
    }

    /**
     * Reloads the configuration from the file.
     *
     * @throws IOException if the file does not exist, is
     *                     a directory rather than a regular file, or for some other
     *                     reason cannot be opened for reading.
     */
    @Throws(IOException::class)
    fun reloadConfig() = FileReader(configFile).use(prop::load)

    /**
     * Returns the property with the specified key in this
     * configuration. If the key is not found in the list,
     * the method returns the default value and the property is set.
     *
     * @param key          the property key
     * @param defaultValue the default value
     *
     * @return the value in this property list with the specified key
     */
    operator fun get(key: String, defaultValue: String = ""): String =
            if (prop.containsKey(key)) {
                prop.getProperty(key)
            } else {
                prop.setProperty(key, defaultValue)
                defaultValue
            }

    /**
     * Sets the property with the specified key in this
     * configuration with the specified value. This method
     * returns the previous value, or null if there was none.
     *
     * @param key   the property key
     * @param value the new value
     *
     * @return the previous value, or null
     */
    operator fun set(key: String, value: String) = prop.setProperty(key, value) as String

    private class ConfigShutdownHook : Runnable {

        override fun run() {
            Logger.info("(Exit) Saving config...")
            try {
                saveConfig()
            } catch (e: IOException) {
                Logger.error("(Exit) Couldn't save config.")
                Logger.printStackTrace(e)
            }

        }

    }

}

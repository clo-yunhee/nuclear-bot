package nuclearbot.util

import java.nio.file.Paths

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
 * Static class for some OS utility methods.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
object OSUtils {

    /**
     * A statically defined OSType enum for this system.
     */
    val os = System.getProperty("os.name").run {
        when {
            this == "Linux" -> OSType.LINUX
            contains("Windows") -> OSType.WINDOWS
            else -> OSType.UNKNOWN
        }
    }

    /**
     * A statically defined absolute path string of the working directory.
     * More specifically, of the working directory *when the OSUtils class was initialized*.
     */
    val workingDir = Paths.get(".").toAbsolutePath().toString()

    enum class OSType {
        /**
         * Unknown, undefined, or malformed os.name property
         */
        UNKNOWN,
        /**
         * GNU/Linux
         */
        LINUX,
        /**
         * Windows
         */
        WINDOWS
    }

}

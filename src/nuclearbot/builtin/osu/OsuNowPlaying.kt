package nuclearbot.builtin.osu

import nuclearbot.util.Logger
import nuclearbot.util.OSUtils
import java.io.IOException

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
 * Utility to get the currently playing song.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
object OsuNowPlaying {

    private val UNKNOWN_OS = "Couldn't determine the operating system."
    private val NOT_RUNNING = "osu! is not running."
    private val NOTHING = "There is no song playing right now."
    private val ERROR = "An error occurred while fetching the song name."
    private val NOW_PLAYING = "Now playing \"%s\"."

    private fun parseTitle(windowTitle: String): Response {
        val titleArray = windowTitle.split(" - ".toRegex(), 2) // remove the heading 'osu! - ' prefix
        if (titleArray.size == 1) {
            return Response(NOTHING, null)
        } else {
            return Response(NOW_PLAYING, titleArray[1])
        }
    }

    private fun getLinux(): Response {
        // first, list osu! windows
        // for each window, check the title
        // only one should have "osu!" in it (windows should be: X container, .NET container, actual osu! game)
        return try {
            Runtime.getRuntime().exec("xdotool search --classname osu")
                    .inputStream.bufferedReader().readLines().forEach {
                Runtime.getRuntime().exec("xdotool getwindowname ${it.trim().toInt()}")
                        .inputStream.bufferedReader().readLines().forEach {
                    return if ("osu!" in it) {
                        parseTitle(it)
                    } else {
                        Response(NOT_RUNNING, null)
                    }
                }
            }
            Response(ERROR, null)
        } catch (e: NumberFormatException) {
            Response(NOT_RUNNING, null)
        } catch (e: IOException) {
            Logger.error(ERROR)
            Logger.printStackTrace(e)
            Response(ERROR, null)
        } catch (e: IllegalStateException) {
            Logger.error(ERROR)
            Logger.printStackTrace(e)
            Response(ERROR, null)
        }
    }

    private fun getWindows(): Response {
        try {
            // parse csv
            Runtime.getRuntime().exec("tasklist /fo csv /nh /fi \"imagename eq osu!.exe\" /v")
                    .inputStream.bufferedReader().readText().let {
                if (!it.startsWith("\"osu!.exe\"")) {
                    return Response(NOT_RUNNING, null)
                }

                val columns = it.split(",".toRegex()).dropLastWhile(String::isEmpty)
                val windowTitle = columns[9].let { it.substring(1, it.length - 2) }

                return parseTitle(windowTitle)
            }
        } catch (e: IOException) {
            Logger.error(ERROR)
            Logger.printStackTrace(e)
            return Response(ERROR, null)
        }

    }

    val song: Response
        get() = when (OSUtils.os) {
            OSUtils.OSType.LINUX -> getLinux()
            OSUtils.OSType.WINDOWS -> getWindows()
            OSUtils.OSType.UNKNOWN -> Response(UNKNOWN_OS, null)
        }

    class Response(val text: String, val rawTitle: String?)

}

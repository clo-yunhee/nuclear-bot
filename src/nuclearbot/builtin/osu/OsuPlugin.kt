@file:JvmName("OsuPlugin")

package nuclearbot.builtin.osu

import nuclearbot.builtin.osu.commands.CommandNowPlaying
import nuclearbot.builtin.osu.commands.CommandRequest
import nuclearbot.builtin.osu.commands.CommandStats
import nuclearbot.client.ChatClient
import nuclearbot.gui.plugin.configuration.HasConfigPanel
import nuclearbot.gui.plugin.configuration.PluginConfigPanel
import nuclearbot.plugin.Plugin
import nuclearbot.util.Config
import nuclearbot.util.Logger
import nuclearbot.util.Watcher

import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.function.BooleanSupplier

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
 * osu! client with basic commands.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class OsuPlugin : Plugin, HasConfigPanel {

    override val name = "osu!"
    override val version = "1.0"

    private lateinit var apiKey: String
    lateinit var username: String
        private set

    private lateinit var client: OsuClient
    lateinit var fetcher: OsuFetcher
        private set

    private var doWatchSong: Boolean = false
    private lateinit var watcherFile: File

    /**
     * Queues a osu! private message.
     * The message will be sent to the host,
     * basically sending a message to oneself.

     * @param message the message to send
     */
    fun sendPrivateMessage(message: String) {
        client.sendPrivateMessage(message)
    }

    override fun getConfigPanel() = PluginConfigPanel("osu").apply {
        addTextField("osu! user name", "user", "")
        addPasswordField("osu! API key", "api_key", "")
        addPasswordField("osu! IRC key", "irc_key", "")
        addTextField("Song watcher path", "np_path", "")
    }

    override fun onLoad(client: ChatClient) {
        apiKey = Config["osu_api_key", ""]
        username = Config["osu_user", ""]

        this.client = OsuClient(username, Config["osu_irc_key", ""])
        this.fetcher = OsuFetcher(apiKey)

        client.registerCommand("np", CommandNowPlaying()).apply {
            usage = "!np"
            description = "Displays the song playing in osu!.\nOnly works when in game."
        }

        client.registerCommand("req", CommandRequest(this)).apply {
            usage = "!req <url> [comment]"
            description = "Requests a song to be played,\nwith optional comments."
        }

        client.registerCommand("stats", CommandStats(this)).apply {
            usage = "!stats [user]"
            description = "Displays info about an osu! player."
        }

        // check if we can watch and write to the path
        watcherFile = File(Config["osu_np_path", ""])

        try {
            doWatchSong = (watcherFile.exists() || !watcherFile.exists() && watcherFile.mkdirs() && watcherFile.delete() && watcherFile.createNewFile()) && watcherFile.canWrite()
        } catch (ignored: IOException) {
        }
    }

    override fun onStart(client: ChatClient) {
        try {
            this.client.connect()
        } catch (e: IOException) {
            Logger.error("(osu!) Unable to connect the osu! IRC client.")
            Logger.printStackTrace(e)
        }

        // watch the playing song
        if (doWatchSong) {
            Logger.info("(osu!) Watching the now playing song and writing to \"" + watcherFile.absolutePath + "\"")

            Watcher.schedule("np-watcher", BooleanSupplier { doWatchSong }, Runnable {
                try {
                    FileWriter(watcherFile, false).use { it.write(OsuNowPlaying.song.rawTitle ?: "Not playing") }
                } catch (e: IOException) {
                    Logger.warning("(osu!) Could not write the now playing song to the file:")
                    Logger.printStackTrace(e)
                }
            })
        }
    }

    override fun onStop(client: ChatClient) {
        Logger.info("(osu!) Releasing resources...")

        if (doWatchSong) {
            Watcher.cancel("np-watcher")
        }

        try {
            this.client.close()
        } catch (e: IOException) {
            Logger.error("(osu!) Unable to disconnect the osu! IRC client.")
            Logger.printStackTrace(e)
        }

    }

    override fun onMessage(client: ChatClient, username: String, message: String) {}

}

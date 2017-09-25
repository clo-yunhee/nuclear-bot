package nuclearbot.builtin.osu.commands

import nuclearbot.builtin.osu.OsuPlugin
import nuclearbot.builtin.osu.data.DataBeatmap
import nuclearbot.client.ChatClient
import nuclearbot.client.Command
import nuclearbot.plugin.CommandExecutor
import nuclearbot.plugin.joinFrom

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
 * Command "req" to request a beatmap (takes beatmaps, beatmap sets, and external URLs).<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class CommandRequest(private val osu: OsuPlugin) : CommandExecutor {
    private val fetcher = osu.fetcher

    override fun onCommand(client: ChatClient, username: String,
                           command: Command, label: String,
                           args: Array<String>): Boolean {
        if (args.size < 2) {
            return false
        }
        val beatmapUrl = args[1].toLowerCase()
        val matcher = REGEX_BEATMAP_URL.matcher(beatmapUrl)
        if (matcher.matches()) {
            val id = matcher.group(2).toInt()
            if (matcher.group(1).equals("s", ignoreCase = true)) {
                fetcher.getBeatmapset(id)?.takeIf(Array<DataBeatmap>::isNotEmpty)?.let {
                    val beatmap = it.first()

                    client.sendMessage(String.format(MSG_REQUEST_BEATMAPSET,
                            beatmap.artist, beatmap.title, it.size))
                    osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_BEATMAPSET,
                            username, beatmapUrl, beatmap.artist, beatmap.title, it.size))

                } ?: run {
                    client.sendMessage(MSG_REQUEST_NOT_FOUND)
                    return true
                }
            } else {
                fetcher.getBeatmap(id)?.let {
                    client.sendMessage(String.format(MSG_REQUEST_BEATMAP,
                            it.artist, it.title, it.version, it.creator, it.bpm, it.diffAR, it.difficultyRating))
                    osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_BEATMAP,
                            username, beatmapUrl, it.artist, it.title, it.bpm, it.diffAR, it.difficultyRating))
                } ?: run {
                    client.sendMessage(MSG_REQUEST_NOT_FOUND)
                    return true
                }
            }
        } else {
            client.sendMessage(String.format(MSG_REQUEST_OTHER, beatmapUrl))
            osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_OTHER, username, beatmapUrl))
        }
        if (args.size > 2) {
            osu.sendPrivateMessage(String.format(PRIVMSG_REQUEST_MESSAGE, args.joinFrom(2)))
        }
        return true
    }

    companion object {

        private val REGEX_BEATMAP_URL = "^https?://osu\\.ppy\\.sh/([bs])/([0-9]+)(&.*)?$".toPattern()

        private val MSG_REQUEST_NOT_FOUND = "No beatmap found."
        private val MSG_REQUEST_OTHER = "Request: %s"
        private val MSG_REQUEST_BEATMAPSET = "Request: %s - %s (%d diffs)"
        private val MSG_REQUEST_BEATMAP = "Request: %s - %s [%s] (creator %s) \u00A6 BPM %d \u00A6 AR %.1f \u00A6 %.2f stars"

        private val PRIVMSG_REQUEST_OTHER = "Request from %s: %s"
        private val PRIVMSG_REQUEST_BEATMAPSET = "Request from %s: [%s %s - %s] (%d diffs)"
        private val PRIVMSG_REQUEST_BEATMAP = "Request from %s: [%s %s - %s] \u00A6 BPM %d \u00A6 AR %.1f \u00A6 %.2f stars"
        private val PRIVMSG_REQUEST_MESSAGE = "(%s)"
    }

}

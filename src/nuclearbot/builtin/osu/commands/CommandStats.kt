package nuclearbot.builtin.osu.commands

import nuclearbot.builtin.osu.OsuPlugin
import nuclearbot.client.ChatClient
import nuclearbot.client.Command
import nuclearbot.plugin.CommandExecutor
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
 * Command "stats" to display info on a player.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class CommandStats(plugin: OsuPlugin) : CommandExecutor {

    private val fetcher = plugin.fetcher
    private val username = plugin.username

    override fun onCommand(client: ChatClient, username: String, command: Command, label: String, args: Array<String>): Boolean {
        val requestUser = if (args.size < 2) this.username else args.copyOfRange(1, args.size).joinToString(" ")

        val user = fetcher.getUser(requestUser)
        if (user == null) {
            client.sendMessage(UNKNOWN_USER)
        } else {
            client.sendMessage(String.format(Locale.US, STATS, user.name, user.pp, user.rank, user.accuracy))
        }
        return true
    }

    companion object {
        private val UNKNOWN_USER = "There is no such user."
        private val STATS = "Stats for %s: %,.2f pp, rank #%,d, accuracy %.2f%%"
    }

}

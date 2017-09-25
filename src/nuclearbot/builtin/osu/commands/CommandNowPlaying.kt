package nuclearbot.builtin.osu.commands

import nuclearbot.builtin.osu.OsuNowPlaying
import nuclearbot.client.ChatClient
import nuclearbot.client.Command
import nuclearbot.plugin.CommandExecutor

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
 * Command "np" to display the currently playing song.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class CommandNowPlaying : CommandExecutor {

    override fun onCommand(client: ChatClient, username: String,
                           command: Command, label: String,
                           args: Array<String>): Boolean {
        OsuNowPlaying.song.let {
            client.sendMessage(String.format(it.text, it.rawTitle))
        }
        return true
    }

}

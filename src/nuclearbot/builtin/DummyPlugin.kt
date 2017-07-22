@file:JvmName("DummyPlugin")

package nuclearbot.builtin

import nuclearbot.client.ChatClient
import nuclearbot.plugin.Plugin

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
 * A dummy client plugin that doesn't do anything.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class DummyPlugin : Plugin {

    override val name = "Dummy"
    override val version = "FG204"

    override fun onMessage(client: ChatClient, username: String, message: String) {
    }

    override fun onLoad(client: ChatClient) {
    }

    override fun onStart(client: ChatClient) {
    }

    override fun onStop(client: ChatClient) {
    }

}

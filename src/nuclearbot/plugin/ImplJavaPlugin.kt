package nuclearbot.plugin

import nuclearbot.client.ChatClient

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
 * Implementation for JavaPlugin.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ImplJavaPlugin(override val handle: Plugin, override val isBuiltin: Boolean) : JavaPlugin {

    override val name = handle.name
    override val version = handle.version

    override val className: String = handle.javaClass.name

    override fun onLoad(client: ChatClient) {
        handle.onLoad(client)
    }

    override fun onStart(client: ChatClient) {
        handle.onStart(client)
    }

    override fun onStop(client: ChatClient) {
        handle.onStop(client)
    }

    override fun onMessage(client: ChatClient, username: String, message: String) {
        handle.onMessage(client, username, message)
    }

}

package nuclearbot.builtin.osu.commands;

import nuclearbot.builtin.osu.OsuNowPlaying;
import nuclearbot.client.ChatClient;
import nuclearbot.client.Command;
import nuclearbot.plugin.CommandExecutor;

import java.io.IOException;

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
 * Command "np" to display the currently playing song.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandNowPlaying implements CommandExecutor {

    @Override
    public boolean onCommand(final ChatClient client, final String username, final Command command, final String label, final String[] args) throws IOException
    {
        final OsuNowPlaying.Response song = OsuNowPlaying.getSong();
        client.sendMessage(String.format(song.text, song.rawTitle));
        return true;
    }

}

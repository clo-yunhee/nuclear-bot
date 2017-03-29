package nuclearbot.builtin.osu;

import nuclearbot.client.ChatClient;
import nuclearbot.client.Command;
import nuclearbot.plugin.CommandExecutor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

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
 * Command "stats" to display info on a player.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandStats implements CommandExecutor {

    private static final String UNKNOWN_USER = "There is no such user.";
    private static final String STATS = "Stats for %s: %,.2f pp, rank #%,d, accuracy %.2f%%";

    private final OsuFetcher m_fetcher;
    private final String m_username;

    public CommandStats(final OsuPlugin plugin)
    {
        m_fetcher = plugin.getFetcher();
        m_username = plugin.getUsername();
    }

    @Override
    public boolean onCommand(final ChatClient client, final String username, final Command command, final String label, final String[] args) throws IOException
    {
        final String requestUser = (args.length < 2) ?
                m_username : String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        final DataUser user = m_fetcher.getUser(requestUser);
        if (user == null)
        {
            client.sendMessage(UNKNOWN_USER);
        }
        else
        {
            client.sendMessage(String.format(Locale.US, STATS, user.getName(), user.getPP(), user.getRank(), user.getAccuracy()));
        }
        return true;
    }

}

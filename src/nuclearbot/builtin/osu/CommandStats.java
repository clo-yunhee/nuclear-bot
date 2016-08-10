package nuclearbot.builtin.osu;

import java.io.IOException;
import java.util.Locale;

import nuclearbot.client.ChatClient;
import nuclearbot.plugin.CommandExecutor;

/*
 * Copyright (C) 2016 NuclearCoder
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
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandStats implements CommandExecutor {

	private static final String UNKNOWN_USER = "There is no such user.";
	private static final String STATS = "Stats for %s: %,.2f pp, rank #%,d, accuracy %.2f%%";
	
	private final OsuPlugin m_osu;
	
	public CommandStats(final OsuPlugin osu)
	{
		m_osu = osu;
	}
	
	@Override
	public void onCommand(final ChatClient client, final String username, final String command, final String[] params) throws IOException
	{
		if (params.length > 2)
		{
			client.sendMessage("Invalid usage of command: !stats [user]");
		}
		else
		{
			final DataUser user = m_osu.getUser(params.length == 2 ? params[1] : null);
			if (user == null)
			{
				client.sendMessage(UNKNOWN_USER);
			}
			else
			{
				client.sendMessage(String.format(Locale.US, STATS, user.getName(), user.getPP(), user.getRank(), user.getAccuracy()));
			}
		}
	}

}

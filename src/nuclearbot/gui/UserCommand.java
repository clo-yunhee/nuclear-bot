package nuclearbot.gui;

import java.io.IOException;

import nuclearbot.client.ChatClient;
import nuclearbot.client.Command;
import nuclearbot.plugin.CommandExecutor;
import nuclearbot.util.ArgumentFormatter;

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
 * Command executor for user commands created with the GUI.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class UserCommand implements CommandExecutor {

	private final ArgumentFormatter m_formatter;
	
	/**
	 * Constructs a text command executor with the given message format.
	 * Every "$n" or "{$n}", where n is a natural integer.
	 * @param format the format string
	 */
	public UserCommand(final String format)
	{
		m_formatter = new ArgumentFormatter(format);
	}
	
	@Override
	public boolean onCommand(final ChatClient client, final String username, final Command command, final String label, final String[] args) throws IOException
	{
		final String message = m_formatter.format(username, args);
		if (message != null)
		{
			client.sendMessage(message);
			return true;
		}
		return false;
	}

}

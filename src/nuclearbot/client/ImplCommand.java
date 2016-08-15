package nuclearbot.client;

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
 * Implementation of Command.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ImplCommand implements Command {
	
	private final String m_label;
	private final String m_usage;
	private final CommandExecutor m_executor;
	
	public ImplCommand(final String label, final String usage, final CommandExecutor executor)
	{
		m_label = label.intern();
		m_usage = usage.intern();
		m_executor = executor;
	}
	
	@Override
	public String getLabel()
	{
		return m_label;
	}
	
	@Override
	public String getUsage()
	{
		return m_usage;
	}
	
	@Override
	public CommandExecutor getExecutor()
	{
		return m_executor;
	}

}

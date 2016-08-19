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
 * Public API for a command.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface Command {

	/**
	 * Returns the command's label.
	 * This is the text following the exclamation mark.
	 * For instance, if the command <code>!example</code>
	 * is registered, this method will return <code>"example"</code>.
	 * @return the command label
	 */
	public String getLabel();
	
	/**
	 * Returns the command's usage.
	 * This is the text shown when the command fails.
	 * The convention for arguments is &lt;required-argument&gt; [optional-argument].
	 * @return the command usage
	 */
	public String getUsage();
	
	/**
	 * Returns a description of the command.
	 * This is the text used in the help command.
	 * If there is no description it will return a string equal to "".
	 * @return the command description
	 */
	public String getDescription();
	
	/**
	 * Changes the description to the given string.
	 * Sets it to "" if the argument is null or an empty string.
	 * @param description the new description
	 * @return this object
	 * @throws IllegalStateException if the implementation does not support changing the description
	 */
	public Command setDescription(String description);
	
	/**
	 * Returns the command's executor instance.
	 * @return the command executor
	 */
	public CommandExecutor getExecutor();
	
}

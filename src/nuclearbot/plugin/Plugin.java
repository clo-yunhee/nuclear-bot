package nuclearbot.plugin;

import nuclearbot.client.ChatListener;

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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Public API interface for a plugin.
 */
public interface Plugin {
	
	/**
	 * Initializes the plugin.
	 * Please avoid using the constructor, as it may cause problems with not-built-in plugins.
	 * This method returns a chat listener which will be used to create the chat client.
	 * @return the chat listener to be bound to the chat client
	 */
	public ChatListener init();
	
}

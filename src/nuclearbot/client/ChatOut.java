package nuclearbot.client;

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
 * Public API interface for a chat output thread.
 */
public interface ChatOut extends Runnable {

	/**
	 * Queues a message.
	 * @throws IllegalStateException if the queue is full
	 */
	public void write(String str);
	
	/**
	 * Starts the output thread.
	 * The thread name will be (name + " out")
	 * @param name thread name prefix
	 */
	public void start(String name);
	
	/**
	 * Stops the output thread.
	 */
	public void close();
	
}

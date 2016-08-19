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
 * Public API interface for a chat output thread.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public interface ChatOut extends Runnable {

	/**
	 * Queues a message to be written.
	 * @param str the message to add to queue
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

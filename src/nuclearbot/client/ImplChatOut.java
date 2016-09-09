package nuclearbot.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import nuclearbot.util.Logger;

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
 * Implementation of the chat output thread.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ImplChatOut implements ChatOut {
	
	private static final int QUEUE_SIZE = 50;
	
	private final BufferedWriter m_out;
	private final BlockingQueue<String> m_queue;

	private final String m_name;
	
	private Thread m_thread;
	
	private volatile boolean m_running;
	
	public ImplChatOut(final OutputStream stream, final String name)
	{
		m_out = new BufferedWriter(new OutputStreamWriter(stream));
		m_queue = new ArrayBlockingQueue<>(QUEUE_SIZE, true);
		m_name = name;
		m_thread = null;
		m_running = false;
		start(name);
	}
	
	@Override
	public void write(final String str)
	{
		try
		{
			m_queue.add(str);
		}
		catch (IllegalStateException e)
		{
			Logger.error("Output queue for " + m_name + " is full:");
			Logger.printStackTrace(e);
		}
	}
	
	@Override
	public void start(final String name)
	{
		m_running = true;
		m_thread = new Thread(this, name + " out");
		m_thread.start();
	}

	@Override
	public void close()
	{
		m_running = false;
		m_thread.interrupt();
	}
	
	@Override
	public void run()
	{
		try
		{
			while (m_running)
			{
				try
				{
					final String message = m_queue.take();
					m_out.write(message);
					m_out.flush();
				}
				catch (IOException e)
				{
					Logger.error("Exception caught in output thread:");
					Logger.printStackTrace(e);
				}
			}
		}
		catch (InterruptedException e) {}
	}
	

}

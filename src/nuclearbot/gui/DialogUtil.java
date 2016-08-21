package nuclearbot.gui;

import java.awt.EventQueue;
import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
 * Utility class for dialogs. Queues dialogs if the GUI is not visible yet.
 * Delegates dialogs to the Event Dispatch Thread if it is not the current thread.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class DialogUtil {
	
	private final JFrame m_container;
	
	private Queue<DialogCall> m_queue;
	private boolean m_doQueue;
	
	/**
	 * Constructs a dialog utility instance with the specified
	 * container as the parent for all dialogs to be created.
	 * @param container the parent for the created dialogs
	 */
	public DialogUtil(final JFrame container)
	{
		m_container = container;
		m_queue = new ArrayDeque<DialogCall>();
		m_doQueue = true;
	}
	
	/**
	 * Sets the queueing status.
	 * If the argument is set to false, this method empties the queue by processing all queued dialogs.
	 * @param doQueue the new queueing status
	 */
	public void setQueueDialogs(final boolean doQueue)
	{
		m_doQueue = doQueue;
		if (!doQueue)
		{
			synchronized (m_queue) // if we're not queueing anymore, process all previously queued dialogs
			{
				while (!m_queue.isEmpty())
				{
					m_queue.poll().process();
				}
			}
		}
	}
	
	private void dialog(final String message, final String title, final int type)
	{
		new DialogCall(message, title, type).process();
	}
	
	/**
	 * Opens an information message dialog with the specified message and title.
	 * @param message the dialog message
	 * @param title the dialog title
	 */
	public void info(final String message, final String title)
	{
		dialog(message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Opens a warning message dialog with the specified message and title.
	 * @param message the dialog message
	 * @param title the dialog title
	 */
	public void warning(final String message, final String title)
	{
		dialog(message, title, JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * Opens an error message dialog with the specified message and title.
	 * @param message the dialog message
	 * @param title the dialog title
	 */
	public void error(final String message, final String title)
	{
		dialog(message, title, JOptionPane.ERROR_MESSAGE);
	}
	
	private class DialogCall implements Runnable {
		
		private final String m_message;
		private final String m_title;
		private final int m_type;
		
		public DialogCall(final String message, final String title, final int type)
		{
			m_message = message;
			m_title = title;
			m_type = type;
		}
		
		private void process()
		{
			if (m_doQueue) // if we're queueing, append to queue
			{
				synchronized (m_queue)
				{
					m_queue.add(this);
				}
			}
			else if (EventQueue.isDispatchThread()) // we're not queueing and we're in the EDT
			{
				run();
			}
			else // if not, do it
			{
				EventQueue.invokeLater(this);
			}
		}
		
		@Override
		public void run()
		{
			JOptionPane.showMessageDialog(m_container, m_message, m_title, m_type);
		}
		
	}
	
}

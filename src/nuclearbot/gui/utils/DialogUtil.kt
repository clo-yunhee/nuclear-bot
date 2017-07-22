package nuclearbot.gui.utils

import java.awt.EventQueue
import java.util.*
import javax.swing.JFrame
import javax.swing.JOptionPane

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
 * Utility class for dialogs. Queues dialogs if the GUI is not visible yet.
 * Delegates dialogs to the Event Dispatch Thread if it is not the current thread.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class DialogUtil(private val container: JFrame) {

    private val queue = ArrayDeque<DialogCall>()
    private var doQueue = true

    /**
     * Sets the queueing status.
     * If the argument is set to false, this method empties the queue by processing all queued dialogs.

     * @param doQueue the new queueing status
     */
    fun setQueueDialogs(doQueue: Boolean) {
        this.doQueue = doQueue
        if (!doQueue) {
            synchronized(queue) {
                // if we're not queueing anymore, process all previously queued dialogs
                while (!queue.isEmpty()) {
                    queue.poll().process()
                }
            }
        }
    }

    private fun dialog(message: String, title: String, type: Int) {
        DialogCall(message, title, type).process()
    }

    /**
     * Opens an information message dialog with the specified message and title.
     *
     * @param message the dialog message
     * @param title   the dialog title
     */
    fun info(message: String, title: String) {
        dialog(message, title, JOptionPane.INFORMATION_MESSAGE)
    }

    /**
     * Opens a warning message dialog with the specified message and title.
     *
     * @param message the dialog message
     * @param title   the dialog title
     */
    fun warning(message: String, title: String) {
        dialog(message, title, JOptionPane.WARNING_MESSAGE)
    }

    /**
     * Opens an error message dialog with the specified message and title.
     *
     * @param message the dialog message
     * @param title   the dialog title
     */
    fun error(message: String, title: String) {
        dialog(message, title, JOptionPane.ERROR_MESSAGE)
    }

    private inner class DialogCall(private val message: String, private val title: String, private val type: Int) : Runnable {

        fun process() {
            if (doQueue) {
                // if we're queueing, append to queue
                synchronized(queue) {
                    queue.add(this)
                }
            } else if (EventQueue.isDispatchThread()) {
                // we're not queueing and we're in the EDT
                run()
            } else {
                // if not, do it
                EventQueue.invokeLater(this)
            }
        }

        override fun run() {
            JOptionPane.showMessageDialog(container, message, title, type)
        }

    }

}

package nuclearbot.client

import nuclearbot.util.Logger
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.concurrent.ArrayBlockingQueue

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
 * Implementation of the chat output thread.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ImplChatOut(stream: OutputStream, private val name: String) : ChatOut {

    private val out = BufferedWriter(OutputStreamWriter(stream))
    private val queue = ArrayBlockingQueue<String>(QUEUE_SIZE, true)

    private lateinit var thread: Thread

    @Volatile private var running = false

    init {
        start(name)
    }

    override fun write(str: String) {
        try {
            queue.add(str)
        } catch (e: IllegalStateException) {
            Logger.error("Output queue for $name is full:")
            Logger.printStackTrace(e)
        }
    }

    override fun start(name: String) {
        running = true
        thread = Thread(this, name + " out").apply { start() }
    }

    override fun close() {
        running = false
        thread.interrupt()
    }

    override fun run() {
        try {
            while (running) {
                try {
                    val message = queue.take()
                    out.write(message)
                    out.flush()
                } catch (e: IOException) {
                    Logger.error("Exception caught in output thread:")
                    Logger.printStackTrace(e)
                }
            }
        } catch (ignored: InterruptedException) {
        }
    }

    companion object {
        private const val QUEUE_SIZE = 50
    }

}

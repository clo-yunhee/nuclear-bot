package nuclearbot.builtin.osu

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

import nuclearbot.client.ChatOut
import nuclearbot.client.ImplChatOut
import nuclearbot.util.Logger
import java.io.BufferedReader
import java.io.IOException
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * Basic IRC client for the osu! plugin.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class OsuClient(private val username: String, private val ircKey: String) {

    private lateinit var shutdownHook: Thread
    private lateinit var socket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var chatOut: ChatOut

    private lateinit var pingThread: PingRunnable

    fun connect() {
        Logger.info("(osu!) Connecting...")

        Thread(ShutdownHookRunnable()).let {
            shutdownHook = it
            Runtime.getRuntime().addShutdownHook(it)
        }

        socket = Socket(SERVER, PORT)
        reader = socket.getInputStream().bufferedReader()
        chatOut = ImplChatOut(socket.getOutputStream(), "osu")

        // send connection data
        sendMessage("PASS " + ircKey)
        sendMessage("NICK " + username)

        // wait for response
        while (true) {
            if (!reader.ready()) {
                Thread.sleep(100)
                continue
            }

            val line = reader.readLine()
            if (line.startsWith("376", 12)) {
                // skip the prefix which is ':cho.ppy.sh ' (12 characters long)
                // this is the code for the last line of MOTD
                Logger.info("(osu!) Connected!")
                break // we're in
            } else if (line.startsWith("464", 12)) {
                Logger.info("(osu!) Bad authentication token.")
                chatOut.close()
                return
            }
        }

        pingThread = PingRunnable().apply(PingRunnable::start)
    }

    fun close() {
        // close resources and socket
        pingThread.stop()
        chatOut.close()
        reader.close()
        socket.close()

        // we exited properly, unregister shutdown hook
        Runtime.getRuntime().removeShutdownHook(shutdownHook)
    }

    internal fun sendPrivateMessage(msg: String) {
        chatOut.write("PRIVMSG $username :$msg\r\n")
    }

    private fun sendMessage(msg: String) {
        chatOut.write(msg + "\r\n")
    }

    private inner class PingRunnable : Runnable {

        private lateinit var thread: Thread

        @Volatile private var running: Boolean = false

        fun start() {
            running = true
            thread = Thread(this, "osu ping").apply(Thread::start)
        }

        fun stop() {
            running = false
        }

        override fun run() {
            try {
                while (running) {
                    if (reader.ready()) {
                        val line = reader.readLine()
                        if (line.startsWith("PING")) {
                            // ping request
                            sendMessage("PONG " + line.substring(5))
                        }
                    } else {
                        try {
                            // this thread has very low priority, hence will almost never be active.
                            Thread.sleep(PING_SLEEP)
                        } catch (ignored: InterruptedException) {
                            Thread.yield()
                        }

                    }
                }
            } catch (e: IOException) {
                Logger.error("(osu!) Exception caught in ping thread:")
                Logger.printStackTrace(e)
            }

        }

    }

    private inner class ShutdownHookRunnable : Runnable {

        override fun run() {
            Logger.info("(Exit) (osu!) Closing resources...")
            chatOut.close()
            try {
                socket.close()
            } catch (ignored: IOException) {
            }
        }

    }

    companion object {

        private val SERVER = "irc.ppy.sh"
        private val PORT = 6667

        private val PING_SLEEP = TimeUnit.SECONDS.toMillis(2)
    }

}

package nuclearbot.client

import nuclearbot.plugin.CommandExecutor
import nuclearbot.plugin.JavaPlugin
import nuclearbot.util.Config
import nuclearbot.util.Logger
import java.io.BufferedReader
import java.io.IOException
import java.net.Socket
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

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
 * Implementation of the bot client.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
class ImplChatClient(plugin: JavaPlugin) : ChatClient {

    private val username = Config["twitch_user", ""].toLowerCase()
    private val authToken = Config["twitch_oauth_key", ""]

    private val plugin = plugin.handle
    private val clientListeners = Collections.synchronizedList(mutableListOf<ClientListener>())

    private val commands = Collections.synchronizedMap(mutableMapOf<String, Command>())

    private lateinit var shutdownHook: Thread
    private lateinit var socket: Socket
    private lateinit var reader: BufferedReader
    private lateinit var chatOut: ChatOut

    // true to attempt to reconnect when the socket is closed
    private var doReconnect = false

    // if true, the client will exit at next loop.
    @Volatile private var doStop = false

    /* notifiers */

    private inline fun notify(callback: (ClientListener) -> Unit) = clientListeners.forEach(callback)

    private fun notifyConnected() =
            notify { it.onConnected(this) }

    private fun notifyDisconnected() =
            notify { it.onDisconnected(this) }

    private fun notifyMessage(username: String, message: String) =
            notify { it.onMessage(this, username, message) }

    private fun notifyCommandRegistered(label: String, command: Command) =
            notify { it.onCommandRegistered(this, label, command) }

    private fun notifyCommandUnregistered(label: String) =
            notify { it.onCommandUnregistered(this, label) }


    /* registry */

    override fun getCommand(label: String) = commands[label]

    override fun registerCommand(label: String, executor: CommandExecutor): Command {
        if (commands.containsKey(label)) {
            throw IllegalArgumentException("Registered an already registered command \"$label\".")
        }
        val command = ImplCommand(label, executor)
        commands.put(label.intern(), command)
        Logger.info("(Twitch) Registered command \"$label\".")
        notifyCommandRegistered(label, command)
        return command
    }

    override fun unregisterCommand(name: String) {
        if (!commands.containsKey(name)) {
            throw IllegalArgumentException("Unregistered not-registered command \"$name\".")
        }
        commands.remove(name)
        Logger.info("(Twitch) Unregistered command \"$name\".")
        notifyCommandUnregistered(name)
    }

    override fun unregisterAllCommands() {
        commands.clear()
        Logger.info("(Twitch) Unregistered all commands.")
    }

    override fun isCommandRegistered(name: String): Boolean {
        return commands.containsKey(name)
    }

    override fun registerClientListener(listener: ClientListener) {
        if (clientListeners.contains(listener)) {
            throw IllegalArgumentException("Registered an already registered ClientListener.")
        }
        clientListeners.add(listener)
        Logger.info("(Twitch) Registered client listener.")
    }

    override fun unregisterClientListener(listener: ClientListener) {
        if (!clientListeners.contains(listener)) {
            throw IllegalArgumentException("Unregistered a not-registered ClientListener.")
        }
        clientListeners.remove(listener)
        Logger.info("(Twitch) Unregistered client listener.")
    }

    override fun unregisterAllClientListeners() {
        clientListeners.clear()
        Logger.info("(Twitch) Cleared all client listeners.")
    }

    private fun send(msg: String) {
        chatOut.write(msg + "\r\n")
    }

    override fun sendMessage(message: String) {
        chatOut.write("PRIVMSG #$username :$message\r\n")

        notifyMessage(username, message)
    }

    override fun stop() {
        doStop = true
    }

    override fun connect() {
        registerSystemCommands()

        catch("listener onLoad") {
            plugin.onLoad(this)
        }

        do {
            Logger.info("(Twitch) Connecting...")

            Thread(ShutdownHookRunnable()).let {
                shutdownHook = it
                Runtime.getRuntime().addShutdownHook(it)
            }

            socket = Socket(SERVER, PORT)
            reader = socket.getInputStream().bufferedReader()
            chatOut = ImplChatOut(socket.getOutputStream(), "twitch")
            doReconnect = false
            doStop = true

            connectIRC()

            if (!doStop) {
                Logger.info("(Twitch) Requesting reconnect message capability...")
                // ask for commands, allows for RECONNECT message
                send("CAP REQ :twitch.tv/commands")

                Logger.info("(Twitch) Joining channel...")
                send("JOIN #" + username)

                sendMessage("Bot running...")

                catch("listener onStart") {
                    plugin.onStart(this)
                }
                notifyConnected()

                while (!doStop) {
                    if (!reader.ready()) {
                        Thread.sleep(SLEEP_DELAY)
                        continue
                    }

                    val line = reader.readLine()
                    if (line.startsWith("PING")) {
                        send("PONG " + line.substring(5))
                    } else if (line.startsWith("RECONNECT")) {
                        doReconnect = true
                        Logger.info("(Twitch) Received a reconnect notice.")
                    } else if (line.startsWith("CAP * ACK", 15)) {
                        Logger.info("(Twitch) Request for commands capability acknowledged.")
                    } else if (line.startsWith("353", 16 + username.length) ||
                            line.startsWith("366", 16 + username.length) ||
                            line.startsWith("ROOMSTATE", 15) ||
                            line.startsWith("USERSTATE", 15)) {
                        // ignore these
                    } else {
                        processMessage(line)
                    }
                }

                catch("listener onStop") {
                    plugin.onStop(this)
                }
            }

            sendMessage(if (doReconnect) "Restarting bot..." else "Stopping bot...")

            // let it finish some tasks
            Thread.sleep(SLEEP_DELAY)

            Logger.info("(Twitch) Releasing resources...")

            chatOut.close()
            reader.close()
            socket.close()

            notifyDisconnected()

        } while (doReconnect)

        // proper exit, we don't need anymore
        Runtime.getRuntime().removeShutdownHook(shutdownHook)

        Logger.info("(Twitch) Exiting client loop...")
    }

    private fun registerSystemCommands() {
        commands.clear()

        registerCommand("restart") { _, username, _, label, _ ->
            if (Moderators.isModerator(username) && label.equals("restart", ignoreCase = true)) {
                Logger.info("(Twitch) Restart command issued.")
                doReconnect = true
                doStop = true
                unregisterAllClientListeners()
                unregisterAllCommands()
            } else {
                Logger.warning("(Twitch) Unauthorized command.")
            }
            true
        }.apply {
            usage = "!restart"
            description = "Soft-restarts the bot."
        }

        registerCommand("stop") { _, username, _, label, _ ->
            if (Moderators.isModerator(username) && label.equals("restart", ignoreCase = true)) {
                Logger.info("(Twitch) Stop command issued.")
                doReconnect = false
                doStop = true
            } else {
                Logger.warning("(Twitch) Unauthorized command.")
            }
            true
        }.apply {
            usage = "!stop"
            description = "Stops the bot."
        }

        registerCommand("help", CommandHelp()).apply {
            usage = "!help [command]"
            description = "Lists all commands or detailed information."
        }
    }

    private fun connectIRC() {
        send("PASS " + authToken)
        send("NICK " + username)

        while (true) {
            if (!reader.ready()) {
                Thread.sleep(SLEEP_DELAY)
                continue
            }

            val line = reader.readLine()
            if (line.startsWith("376", 15)) {
                // skip the prefix which is ':tmi.twitch.tv ' (15 characters long)
                // 376 is the code of MOTD's last line
                Logger.info("(Twitch) Connected!")
                doStop = false
                break
            } else if (line.startsWith("NOTICE * :", 15)) {
                Logger.info("(Twitch) Couldn't connect: " + line.substring(25))
                break
            }
        }
    }

    private fun processMessage(line: String) {
        val matcher = PATTERN_MESSAGE.matcher(line)
        if (!matcher.matches()) return

        val username = matcher.group(1)
        val message = matcher.group(2)

        // if it's a command
        if (message.first() == '!') {
            val args = message.split("\\s+".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            // strip the ! from the first argument
            val label = args[0].substring(1).toLowerCase()

            Logger.info(String.format("(Twitch) Command from %s: %s", username, Arrays.toString(args)))

            try {
                commands[label]?.let { command ->
                    if (!command.executor.onCommand(this, username, command, label, args)) {
                        sendMessage("Usage: " + command.usage)
                    }
                } ?: Logger.info("(Twitch) Unknown command.")
            } catch (e: Exception) {
                Logger.error("(Twitch) Exception in listener onCommand:")
                Logger.printStackTrace(e)
            }

        } else {
            Logger.info(String.format("(Twitch) Message from %s: %s", username, message))
            try {
                // call the message listener
                plugin.onMessage(this, username, message)
            } catch (e: Exception) // catch exceptions here to not leave the loop
            {
                Logger.error("(Twitch) Exception in listener onMessage:")
                Logger.printStackTrace(e)
            }

            notifyMessage(username, message)
        }
    }

    private inline fun catch(message: String, block: () -> Unit) = try {
        block()
    } catch (e: Exception) {
        Logger.error("(Twitch) Exception in $message:")
        Logger.printStackTrace(e)
    }

    private inner class CommandHelp : CommandExecutor {

        override fun onCommand(client: ChatClient, username: String,
                               command: Command, label: String,
                               args: Array<String>): Boolean {
            // if there is no argument, list the commands
            val message = if (args.size == 1) {
                val sb = StringBuilder()
                sb.append("Commands: ")
                synchronized(commands) {
                    if (commands.isEmpty()) {
                        sb.append("(empty)")
                    } else {
                        val it = commands.keys.iterator()
                        sb.append(it.next()) // there's at least one!
                        while (it.hasNext()) {
                            sb.append(", ")
                            sb.append(it.next())
                        }
                    }
                }
                sb.toString()
            } else {
                commands[args[1]]?.let { "Usage: ${it.usage} - ${it.description}" } ?: "Command does not exist."
            }

            client.sendMessage(message)

            return true
        }

    }

    private inner class ShutdownHookRunnable : Runnable {

        override fun run() {
            Logger.info("(Exit) (Twitch) Closing resources...")

            chatOut.close() // attempt to close output thread cleanly
            try {
                socket.close() // close socket
            } catch (ignored: IOException) {
            }
        }

    }

    companion object {
        private val PATTERN_MESSAGE = Pattern.compile("^:([a-zA-Z0-9_]+)![a-zA-Z0-9_]+@[a-zA-Z0-9_]+\\.tmi\\.twitch\\.tv PRIVMSG #[a-zA-Z0-9_]+ :(.+)\$")

        private const val SERVER = "irc.chat.twitch.tv"
        private const val PORT = 6667

        private val SLEEP_DELAY = TimeUnit.MILLISECONDS.toMillis(100)
    }

}
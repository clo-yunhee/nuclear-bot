package nuclearbot.builtin.osu;

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

import nuclearbot.client.ChatOut;
import nuclearbot.client.ImplChatOut;
import nuclearbot.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Basic IRC client for the osu! plugin.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class OsuClient {

    private static final String SERVER = "irc.ppy.sh";
    private static final int PORT = 6667;

    private static final long PING_SLEEP = TimeUnit.SECONDS.toMillis(2);

    private final String m_username;
    private final String m_ircKey;
    private Thread m_shutdownHook;
    private Socket m_socket;
    private BufferedReader m_reader;
    private ChatOut m_chatOut;

    private PingRunnable m_pingThread;

    public OsuClient(final String username, final String ircKey) {
        m_username = username;
        m_ircKey = ircKey;

        m_socket = null;
        m_reader = null;
        m_chatOut = null;
        m_pingThread = null;
    }

    public void connect() throws IOException {
        String line;

        Logger.info("(osu!) Connecting...");

        // open connection and I/O objects
        Runtime.getRuntime().addShutdownHook(m_shutdownHook = new Thread(new ShutdownHookRunnable()));
        m_socket = new Socket(SERVER, PORT);
        m_reader = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
        m_chatOut = new ImplChatOut(m_socket.getOutputStream(), "osu");

        // send connection data
        sendMessage("PASS " + m_ircKey);
        sendMessage("NICK " + m_username);

        // wait for response
        while ((line = m_reader.readLine()) != null) {
            // skip the prefix which is ':cho.ppy.sh ' (12 characters long)
            if (line.startsWith("376", 12)) // this is the code for the last line of MOTD
            {
                Logger.info("(osu!) Connected!");
                break; // we're in
            } else if (line.startsWith("464", 12)) {
                Logger.info("(osu!) Bad authentication token.");
                m_chatOut.close();
                return;
            }
        }

        m_pingThread = new PingRunnable();
        m_pingThread.start();
    }

    public void close() throws IOException {
        // close resources and socket
        m_pingThread.stop();
        m_chatOut.close();
        m_reader.close();
        m_socket.close();
        m_reader = null;
        m_chatOut = null;
        m_socket = null;

        // we exited properly, unregister shutdown hook
        Runtime.getRuntime().removeShutdownHook(m_shutdownHook);
    }

    void sendPrivateMessage(final String msg) {
        m_chatOut.write("PRIVMSG " + m_username + " :" + msg + "\r\n");
    }

    private void sendMessage(final String msg) {
        m_chatOut.write(msg + "\r\n");
    }

    private class PingRunnable implements Runnable {

        private Thread m_thread;

        private volatile boolean m_running;

        private void start() {
            m_running = true;
            m_thread = new Thread(this, "osu ping");
            m_thread.start();
        }

        private void stop() {
            m_running = false;
        }

        @Override
        public void run() {
            try {
                while (m_running) {
                    if (m_reader.ready()) {
                        final String line = m_reader.readLine();
                        if (line.startsWith("PING")) // ping request
                        {
                            sendMessage("PONG " + line.substring(5));
                        }
                    } else {
                        try {
                            // this thread has very low priority, hence will almost never be active.
                            Thread.sleep(PING_SLEEP);
                        } catch (InterruptedException ignored) {
                            Thread.yield();
                        }
                    }
                }
            } catch (IOException e) {
                Logger.error("(osu!) Exception caught in ping thread:");
                Logger.printStackTrace(e);
            }
        }

    }

    private class ShutdownHookRunnable implements Runnable {

        @Override
        public void run() {
            Logger.info("(Exit) (osu!) Closing resources...");
            if (m_chatOut != null) {
                m_chatOut.close();
            }
            try {
                if (m_socket != null) {
                    m_socket.close();
                }
            } catch (IOException ignored) {
            } // we don't really care
        }

    }

}

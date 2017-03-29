package nuclearbot.builtin.osu;

import nuclearbot.client.ChatClient;
import nuclearbot.client.ChatOut;
import nuclearbot.client.ImplChatOut;
import nuclearbot.gui.plugin.ConfigPanel;
import nuclearbot.gui.plugin.HasConfigPanel;
import nuclearbot.plugin.Plugin;
import nuclearbot.util.Config;
import nuclearbot.util.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
 * osu! client with basic commands.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class OsuPlugin implements Plugin, HasConfigPanel {

    public static final String PLUGIN_NAME = "osu!";
    public static final String PLUGIN_VERSION = "1.0";

    private static final String SERVER = "cho.ppy.sh";
    private static final int PORT = 6667;

    private Thread m_shutdownHook;

    private String m_apiKey;
    private String m_ircKey;
    private String m_username;

    private OsuFetcher m_fetcher;

    private Socket m_socket;
    private BufferedReader m_reader;
    private ChatOut m_chatOut;

    private PingRunnable m_pingThread;

    /**
     * Queues a osu! private message.
     * The message will be sent to the host,
     * basically sending a message to oneself.
     *
     * @param msg the message to send
     */
    public void sendPrivateMessage(final String msg)
    {
        m_chatOut.write("PRIVMSG " + m_username + " :" + msg + "\r\n");
    }

    /**
     * Queues a message to send to the osu! IRC server.
     *
     * @param msg the message to send
     */
    public void sendMessage(final String msg)
    {
        m_chatOut.write(msg + "\r\n");
    }

    /**
     * Returns the osu! username of the account the bot is running on.
     *
     * @return the bot's osu! username
     */
    public String getUsername()
    {
        return m_username;
    }

    /**
     * Returns the osu! API fetcher for this session.
     *
     * @return the bot's osu! API fetcher
     */
    public OsuFetcher getFetcher()
    {
        return m_fetcher;
    }

    @Override
    public ConfigPanel getConfigPanel()
    {
        final ConfigPanel configPanel = new ConfigPanel("osu");
        configPanel.addTextField("osu! user name", "user", "");
        configPanel.addPasswordField("osu! API key", "api_key", "");
        configPanel.addPasswordField("osu! IRC key", "irc_key", "");
        return configPanel;
    }

    @Override
    public void onLoad(final ChatClient client) throws IOException
    {
        m_apiKey = Config.get("osu_api_key");
        m_ircKey = Config.get("osu_irc_key");
        m_username = Config.get("osu_user");
        m_fetcher = new OsuFetcher(m_apiKey);
        m_socket = null;
        m_reader = null;
        m_chatOut = null;

        client.registerCommand("np", "!np", new CommandNowPlaying())
                .setDescription("Displays the song playing in osu!.\nOnly works when in game.");

        client.registerCommand("req", "!req <url> [comments]", new CommandRequest(this))
                .setDescription("Requests a song to be played,\nwith optional comments.");

        client.registerCommand("stats", "!stats [user]", new CommandStats(this))
                .setDescription("Displays info about an osu! player.");
    }

    @Override
    public void onStart(final ChatClient client) throws IOException
    {
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
        while ((line = m_reader.readLine()) != null)
        {
            // skip the prefix which is ':cho.ppy.sh ' (12 characters long)
            if (line.startsWith("376", 12)) // this is the code for the last line of MOTD
            {
                Logger.info("(osu!) Connected!");
                break; // we're in
            }
            else if (line.startsWith("464", 12))
            {
                Logger.info("(osu!) Bad authentication token.");
                m_chatOut.close();
                return;
            }
        }

        m_pingThread = new PingRunnable();
        m_pingThread.start();
    }

    @Override
    public void onStop(final ChatClient client) throws IOException
    {
        Logger.info("(osu!) Releasing resources...");

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

    @Override
    public void onMessage(final ChatClient client, final String username, final String message) throws IOException
    {
    }

    private class PingRunnable implements Runnable {

        private Thread m_thread;

        private volatile boolean m_running;

        public void start()
        {
            m_running = true;
            m_thread = new Thread(this, "osu ping");
            m_thread.start();
        }

        public void stop()
        {
            m_running = false;
        }

        @Override
        public void run()
        {
            try
            {
                while (m_running)
                {
                    if (m_reader.ready())
                    {
                        final String line = m_reader.readLine();
                        if (line.startsWith("PING")) // ping request
                        {
                            sendMessage("PONG " + line.substring(5));
                        }
                    }
                    else
                    {
                        try
                        {
                            // this thread has very low priority, hence will almost never be active.
                            Thread.sleep(800L);
                        }
                        catch (InterruptedException ignored)
                        {
                        }
                    }
                }
            }
            catch (IOException e)
            {
                Logger.error("(osu!) Exception caught in ping thread:");
                Logger.printStackTrace(e);
            }
        }

    }

    private class ShutdownHookRunnable implements Runnable {

        @Override
        public void run()
        {
            Logger.info("(Exit) (osu!) Closing resources...");
            if (m_chatOut != null)
            {
                m_chatOut.close();
            }
            try
            {
                if (m_socket != null)
                {
                    m_socket.close();
                }
            }
            catch (IOException ignored)
            {
            } // we don't really care
        }

    }

}

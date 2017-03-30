package nuclearbot.builtin.osu;

import nuclearbot.builtin.osu.commands.CommandNowPlaying;
import nuclearbot.builtin.osu.commands.CommandRequest;
import nuclearbot.builtin.osu.commands.CommandStats;
import nuclearbot.client.ChatClient;
import nuclearbot.gui.plugin.configuration.HasConfigPanel;
import nuclearbot.gui.plugin.configuration.PluginConfigPanel;
import nuclearbot.plugin.Plugin;
import nuclearbot.util.Config;
import nuclearbot.util.Logger;
import nuclearbot.util.Watcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

    private String m_apiKey;
    private String m_username;

    private boolean m_doWatchSong;
    private File m_watcherFile;

    private OsuClient m_client;
    private OsuFetcher m_fetcher;

    /**
     * Queues a osu! private message.
     * The message will be sent to the host,
     * basically sending a message to oneself.
     *
     * @param message the message to send
     */
    public void sendPrivateMessage(final String message)
    {
        m_client.sendPrivateMessage(message);
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
    public PluginConfigPanel getConfigPanel()
    {
        final PluginConfigPanel configPanel = new PluginConfigPanel("osu");
        configPanel.addTextField("osu! user name", "user", "");
        configPanel.addPasswordField("osu! API key", "api_key", "");
        configPanel.addPasswordField("osu! IRC key", "irc_key", "");
        configPanel.addTextField("Song watcher path", "np_path", "");
        return configPanel;
    }

    @Override public void onLoad(final ChatClient client) throws IOException
    {
        m_apiKey = Config.get("osu_api_key");
        m_username = Config.get("osu_user");

        m_client = new OsuClient(m_username, Config.get("osu_irc_key"));
        m_fetcher = new OsuFetcher(m_apiKey);

        client.registerCommand("np", "!np", new CommandNowPlaying())
                .setDescription("Displays the song playing in osu!.\nOnly works when in game.");

        client.registerCommand("req", "!req <url> [comments]", new CommandRequest(this))
                .setDescription("Requests a song to be played,\nwith optional comments.");

        client.registerCommand("stats", "!stats [user]", new CommandStats(this))
                .setDescription("Displays info about an osu! player.");

        // check if we can watch and write to the path
        final String watcherPath = Config.get("osu_np_path");
        m_watcherFile = new File(watcherPath);

        m_doWatchSong =
                (m_watcherFile.exists() || (!m_watcherFile.exists() && m_watcherFile.mkdirs()
                        && m_watcherFile.delete() && m_watcherFile.createNewFile()))
                        && m_watcherFile.canWrite();
    }

    @Override public void onStart(final ChatClient client) throws IOException
    {
        try
        {
            m_client.connect();
        }
        catch (IOException e)
        {
            Logger.error("(osu!) Unable to connect the osu! IRC client.");
            Logger.printStackTrace(e);
        }

        // watch the playing song
        if (m_doWatchSong)
        {
            Logger.info("(osu!) Watching the now playing song and writing to \"" + m_watcherFile
                    .getAbsolutePath() + "\"");

            Watcher.schedule("np-watcher", () -> m_doWatchSong, () ->
            {
                try
                {
                    final OsuNowPlaying.Response song = OsuNowPlaying.getSong();
                    final String text = song.rawTitle != null ? song.rawTitle : "Not playing";
                    try (final FileWriter writer = new FileWriter(m_watcherFile, false))
                    {
                        writer.write(text);
                    }
                }
                catch (IOException e)
                {
                    Logger.warning("(osu!) Could not write the now playing song to the file:");
                    Logger.printStackTrace(e);
                }
            });
        }
    }

    @Override public void onStop(final ChatClient client) throws IOException
    {
        Logger.info("(osu!) Releasing resources...");

        if (m_doWatchSong)
        {
            Watcher.cancel("np-watcher");
        }

        try
        {
            m_client.close();
        }
        catch (IOException e)
        {
            Logger.error("(osu!) Unable to disconnect the osu! IRC client.");
            Logger.printStackTrace(e);
        }
    }

    @Override
    public void onMessage(final ChatClient client, final String username,
                          final String message) throws IOException
    {
    }


}

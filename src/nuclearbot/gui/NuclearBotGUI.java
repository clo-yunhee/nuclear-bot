package nuclearbot.gui;

import nuclearbot.client.ChatClient;
import nuclearbot.client.ClientListener;
import nuclearbot.client.Command;
import nuclearbot.client.ImplChatClient;
import nuclearbot.gui.components.ConfigPanel;
import nuclearbot.gui.components.FooterPanel;
import nuclearbot.gui.components.ModeratorPanel;
import nuclearbot.gui.components.StatusPanel;
import nuclearbot.gui.components.chat.ChatPanel;
import nuclearbot.gui.components.commands.CommandPanel;
import nuclearbot.gui.components.console.ConsolePanel;
import nuclearbot.gui.components.console.DocumentOutputStream;
import nuclearbot.gui.components.plugins.PluginPanel;
import nuclearbot.gui.utils.DialogUtil;
import nuclearbot.plugin.ImplPluginLoader;
import nuclearbot.plugin.JavaPlugin;
import nuclearbot.plugin.PluginLoader;
import nuclearbot.util.HTML;
import nuclearbot.util.Logger;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
 * Main window for the GUI.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
@SuppressWarnings("unused")
public class NuclearBotGUI extends JPanel implements ClientListener {

    public static final int TAB_STATUS = 0;
    public static final int TAB_PLUGIN = 1;
    public static final int TAB_COMMANDS = 2;
    public static final int TAB_CHAT = 3;
    public static final int TAB_CONFIG = 4;
    public static final int TAB_CONSOLE = 5;
    private static final long serialVersionUID = 606418561134403181L;

    // GUI components
    private final DialogUtil m_dialogs;
    private final JPopupMenu m_textComponentPopupMenu;

    private final JFrame m_container;
    private final JTabbedPane m_body;

    private final StatusPanel m_status;
    private final PluginPanel m_plugins;
    private final CommandPanel m_commands;
    private final ModeratorPanel m_moderators;
    private final ChatPanel m_chat;
    private final ConfigPanel m_config;
    private final ConsolePanel m_console;

    // client stuff
    private final PluginLoader m_pluginLoader;
    private boolean m_isFrameClosing; // window is closing?
    private boolean m_doRestartClient; // restart after the client is stopped?
    private boolean m_isClientRunning; // client is running?
    private ClientThread m_clientThread;
    private ChatClient m_client;

    // constructor
    public NuclearBotGUI()
    {
        final Document consoleDocument = new PlainDocument();
        { // do that first in order to log the most we can
            Logger.info("(GUI) Linking GUI console to system console...");
            DocumentOutputStream.redirectSystemOut(consoleDocument);
        }

        // client variables init

        m_pluginLoader = new ImplPluginLoader();

        m_isFrameClosing = false;
        m_doRestartClient = false;
        m_isClientRunning = false;
        m_clientThread = null;
        m_client = null;

        // gui variables init

        Logger.info("(GUI) Constructing window...");

        m_container = new JFrame("NuclearBot - Control Panel");
        m_container.setLocationByPlatform(true);
        m_container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        m_container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent event)
            {
                m_isFrameClosing = true;
                m_plugins.dispose();
                m_container.dispose();
                if (m_client != null)
                {
                    stopClient();
                }
            }
        });

        m_dialogs = new DialogUtil(m_container);

        m_textComponentPopupMenu = new JPopupMenu();
        m_textComponentPopupMenu.add(TransferHandler.getCopyAction());

        m_body = new JTabbedPane();

        m_status = new StatusPanel(this);
        m_plugins = new PluginPanel(this);
        m_commands = new CommandPanel(this);
        m_moderators = new ModeratorPanel(this);
        m_chat = new ChatPanel(this);
        m_config = new ConfigPanel(this);
        m_console = new ConsolePanel(this, consoleDocument);

        m_body.addTab("Status", m_status);
        m_body.addTab("Plugins", m_plugins);
        m_body.addTab("Commands", m_commands);
        m_body.addTab("Moderators", m_moderators);
        m_body.addTab("Chat", m_chat);
        m_body.addTab("Config", m_config);
        m_body.addTab("Console", m_console);

        setLayout(new BorderLayout());
        add(m_body, BorderLayout.CENTER);
        add(new FooterPanel(), BorderLayout.SOUTH);

        m_container.setContentPane(this);
        m_container.pack();

        pluginChanged(m_pluginLoader.getPlugin());
    }

    public void open()
    {
        m_container.setVisible(true);
        m_dialogs.setQueueDialogs(false);
    }

    public boolean isClientRunning()
    {
        return m_isClientRunning;
    }

    public ChatClient getClient()
    {
        return m_client;
    }

    public DialogUtil getDialogs()
    {
        return m_dialogs;
    }

    public JPopupMenu getTextPopupMenu()
    {
        return m_textComponentPopupMenu;
    }

    public PluginLoader getPluginLoader()
    {
        return m_pluginLoader;
    }

    public void setRestartClient(final boolean restart)
    {
        m_doRestartClient = restart;
    }

    public JFrame getFrame()
    {
        return m_container;
    }

    public void selectTab(final int index)
    {
        m_body.setSelectedIndex(index);
    }

    public void startClient()
    {
        m_status.toggleStartButton(false);

        Logger.info("(GUI) Starting client...");

        final JavaPlugin plugin = m_pluginLoader.getPlugin();

        m_client = new ImplChatClient(plugin);
        m_client.registerClientListener(this);
        m_clientThread = new ClientThread(m_client);
        m_clientThread.start();
    }

    public void stopClient()
    {
        Logger.info("(GUI) Stopping client...");

        m_status.toggleStopButton(false);
        m_status.toggleRestartButton(false);

        m_commands.unregisterCommands();

        m_client.stop();
    }

    private void clientStarted()
    {
        m_isClientRunning = true;

        m_status.setStatusText("Running");
        m_status.toggleStartButton(false);
        m_status.toggleStopButton(true);
        m_status.toggleRestartButton(true);

        m_chat.toggleSendButton(true);

        m_doRestartClient = false;

        if (m_isFrameClosing)
        {
            stopClient();
        }
        else
        {
            m_commands.registerCommands();
        }
    }

    private void clientStopped()
    {
        m_isClientRunning = false;

        m_status.setStatusText("Not running");
        m_status.toggleStartButton(true);
        m_status.toggleStopButton(false);
        m_status.toggleRestartButton(false);

        m_chat.toggleSendButton(false);

        m_commands.unregisterCommands();

        if (m_doRestartClient)
        {
            startClient();
        }
    }

    public void pluginChanged(final JavaPlugin plugin)
    {
        if (plugin != null)
        {
            // italic name if built-in plugin
            final String pluginName = HTML.escapeText(plugin.getName());
            final String pluginClassName = plugin.getClassName();
            final String pluginLabelText = "<html>" + (plugin.isBuiltin() ? "<em>" + pluginName + "</em>" : pluginName) + "</html>";

            m_plugins.setPluginText(pluginLabelText, pluginClassName);
            m_status.setPluginText(pluginLabelText, pluginClassName);

            m_config.setPluginPanel(plugin.getHandle());

            if (m_isClientRunning)
            { // ask to restart if the client is already running
                final int restart = JOptionPane.showConfirmDialog(m_container, "The changes will be effective after a restart.\nRestart now?", "Restart?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (restart == JOptionPane.YES_OPTION)
                {
                    m_doRestartClient = true;
                    stopClient();
                }
            }
        }
        else
        {
            selectTab(TAB_CONSOLE);
            m_dialogs.error("Couldn't load the plugin. Check console for details.", "Error while loading plugin");
        }
    }

	/* **** client listener methods **** */

    @Override
    public void onConnected(final ChatClient client)
    {
        SwingUtilities.invokeLater(this::clientStarted);
    }

    @Override
    public void onDisconnected(final ChatClient client)
    {
        SwingUtilities.invokeLater(this::clientStopped);
    }

    @Override
    public void onMessage(final ChatClient client, final String username, final String message)
    {
        SwingUtilities.invokeLater(() -> m_chat.addMessage(username, message));
    }

    @Override
    public void onCommandRegistered(final ChatClient client, final String label, final Command command)
    {
        SwingUtilities.invokeLater(() -> m_commands.addCommandList(label));
    }

    @Override
    public void onCommandUnregistered(final ChatClient client, final String label)
    {
        SwingUtilities.invokeLater(() -> m_commands.removeCommandList(label));
    }

	/* **** client thread class **** */

    private class ClientThread implements Runnable {

        private final ChatClient m_client;
        private Thread m_thread;

        public ClientThread(final ChatClient client)
        {
            m_client = client;
            m_thread = null;
        }

        public void start()
        {
            m_thread = new Thread(this, "client");
            m_thread.start();
        }

        @Override
        public void run()
        {
            try
            {
                m_client.connect();
            }
            catch (IOException e)
            {
                Logger.error("(GUI) Exception caught in client thread:");
                Logger.printStackTrace(e);
                m_doRestartClient = false;
                onDisconnected(m_client);
                selectTab(TAB_CONSOLE);
                m_dialogs.error("Exception in client thread. Check console for details.", "Client error");
            }
        }

    }

	/* **** misc. classes **** */

}

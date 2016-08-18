package nuclearbot.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.client.ChatClient;
import nuclearbot.client.ClientListener;
import nuclearbot.client.Command;
import nuclearbot.client.ImplChatClient;
import nuclearbot.plugin.ImplPluginLoader;
import nuclearbot.plugin.JavaPlugin;
import nuclearbot.plugin.Plugin;
import nuclearbot.plugin.PluginLoader;
import nuclearbot.utils.Config;
import nuclearbot.utils.HTML;
import nuclearbot.utils.Logger;
import nuclearbot.utils.OSUtils;

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
 * Main window for the GUI.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ControlPanel extends JPanel implements ActionListener, ItemListener, ClientListener {
	
	private static final long serialVersionUID = 606418561134403181L;

	private static final String GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/";
	
	// GUI components
	
	private final JPopupMenu m_textComponentPopupMenu;
	private final FileDialog m_pluginFileDialog;
	
	private final JFrame m_container;
	private final JTabbedPane m_body;
	
	private final JLabel m_statusRunningLabel;
	private final JButton m_startButton;
	private final JButton m_stopButton;
	private final JButton m_restartButton;
	private final JLabel m_currentPluginLabel;
	
	private final JLabel m_loadedPluginLabel;
	private final JTextField m_externalPluginPathField;
	private final JButton m_externalPluginBrowseButton;
	private final JButton m_externalPluginLoadButton;
	private final JComboBox<String> m_builtinPluginsComboBox;
	private final JButton m_builtinPluginLoadButton;
	
	private final JComboBox<String> m_commandsRegisteredComboBox;
	private final JLabel m_commandInfoNameLabel;
	private final JLabel m_commandInfoUsageLabel;
	
	private final LimitedStringList m_chatHistoryList;
	
	// client stuff
	
	private final PluginLoader m_pluginLoader;

	private boolean m_isFrameClosing; // window is closing?
	private boolean m_doRestartClient; // restart after the client is stopped?

	private boolean m_isClientRunning; // client is running?
	private ClientThread m_clientThread;
	private ChatClient m_client;

	// constructor
	public ControlPanel()
	{
		final Document consoleDocument = new PlainDocument();
		{
			Logger.info("(GUI) Linking GUI console to system console...");
			DocumentOutputStream.redirectSystemOut(consoleDocument);
		}
		
		m_pluginLoader = new ImplPluginLoader();
		
		m_isFrameClosing = false;
		m_doRestartClient = false;
		m_isClientRunning = false;
		m_clientThread = null;
		m_client = null;
		
		Logger.info("(GUI) Constructing window...");		

		m_container = new JFrame("NuclearBot - Control Panel");
		m_container.setLocationRelativeTo(null);
		m_container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		m_container.addWindowListener(new NotifiedClosingListener());

		m_pluginFileDialog = new FileDialog(m_container, "Choose a file", FileDialog.LOAD);
		m_pluginFileDialog.setLocationRelativeTo(m_container);
		m_pluginFileDialog.setDirectory(OSUtils.workingDir());
		m_pluginFileDialog.setFile("*.jar");
		m_pluginFileDialog.setFilenameFilter(new PluginFilenameFilter());
		
		m_textComponentPopupMenu = new JPopupMenu();
		m_textComponentPopupMenu.add(TransferHandler.getCopyAction());
		
		m_body = new JTabbedPane();
		
		final JPanel tabStatus = new JPanel();
		{
			final JPanel currentPluginPanel = new JPanel();
			{
				final JLabel currentPluginPrefixLabel = new JLabel("<html><u>Current plugin:</u></html>");
				m_currentPluginLabel = new JLabel();
				
				m_currentPluginLabel.setFont(m_currentPluginLabel.getFont().deriveFont(Font.PLAIN));
				m_currentPluginLabel.setHorizontalAlignment(JLabel.CENTER);
				m_currentPluginLabel.setComponentPopupMenu(m_textComponentPopupMenu);
				
				currentPluginPanel.setLayout(new FlowLayout());
				currentPluginPanel.add(currentPluginPrefixLabel);
				currentPluginPanel.add(m_currentPluginLabel);
			} // currentPluginPanel
			
			final JPanel controlButtonsPanel = new JPanel();
			{
				m_statusRunningLabel = new JLabel("Not running");
				m_startButton = new JButton("Start");
				m_stopButton = new JButton("Stop");
				m_restartButton = new JButton("Restart");
				
				m_statusRunningLabel.setFont(m_statusRunningLabel.getFont().deriveFont(Font.ITALIC));
				m_startButton.addActionListener(this);
				m_stopButton.addActionListener(this);
				m_stopButton.setEnabled(false);
				m_restartButton.addActionListener(this);
				m_restartButton.setEnabled(false);
				
				controlButtonsPanel.setLayout(new FlowLayout());
				controlButtonsPanel.add(m_statusRunningLabel);
				controlButtonsPanel.add(m_startButton);
				controlButtonsPanel.add(m_stopButton);
				controlButtonsPanel.add(m_restartButton);
			} // controlButtonsPanel
			
			tabStatus.setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
			tabStatus.add(controlButtonsPanel);
			tabStatus.add(currentPluginPanel);
		} // tabStatus
		
		final JPanel tabPlugins = new JPanel();
		{
			final JPanel loadedPluginPanel = new JPanel();
			{
				m_loadedPluginLabel = new JLabel();
				
				m_loadedPluginLabel.setHorizontalAlignment(JLabel.CENTER);
				m_loadedPluginLabel.setFont(m_loadedPluginLabel.getFont().deriveFont(Font.PLAIN));
				m_loadedPluginLabel.setComponentPopupMenu(m_textComponentPopupMenu);
				
				loadedPluginPanel.setBorder(BorderFactory.createTitledBorder("Current plugin"));
				loadedPluginPanel.setLayout(new FlowLayout());
				loadedPluginPanel.add(m_loadedPluginLabel);
			} // loadedPluginPanel
			
			final JPanel loadExternalPluginPanel = new JPanel();
			{
				m_externalPluginPathField = new JTextField(16);
				m_externalPluginBrowseButton = new JButton("Browse...");
				m_externalPluginLoadButton = new JButton("Load");
				
				m_externalPluginPathField.setComponentPopupMenu(m_textComponentPopupMenu);
				m_externalPluginPathField.addActionListener(this);
				m_externalPluginBrowseButton.addActionListener(this);
				m_externalPluginLoadButton.addActionListener(this);
				
				loadExternalPluginPanel.setBorder(BorderFactory.createTitledBorder("Change (external)"));
				loadExternalPluginPanel.setLayout(new FlowLayout());
				loadExternalPluginPanel.add(m_externalPluginPathField);
				loadExternalPluginPanel.add(m_externalPluginBrowseButton);
				loadExternalPluginPanel.add(m_externalPluginLoadButton);
			} // loadExternalPluginPanel
			
			final JPanel loadBuiltinPluginPanel = new JPanel();
			{
				m_builtinPluginsComboBox = new JComboBox<String>(m_pluginLoader.getBuiltinPlugins());
				m_builtinPluginLoadButton = new JButton("Load");
				
				m_builtinPluginsComboBox.setEditable(false);
				m_builtinPluginsComboBox.setSelectedItem(DummyPlugin.class.getName());
				m_builtinPluginsComboBox.setFont(m_builtinPluginsComboBox.getFont().deriveFont(Font.PLAIN));
				m_builtinPluginLoadButton.addActionListener(this);
				
				loadBuiltinPluginPanel.setBorder(BorderFactory.createTitledBorder("Change (built-in)"));
				loadBuiltinPluginPanel.setLayout(new FlowLayout());
				loadBuiltinPluginPanel.add(m_builtinPluginsComboBox);
				loadBuiltinPluginPanel.add(m_builtinPluginLoadButton);
			} // loadBuiltinPluginPanel
			
			tabPlugins.setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
			tabPlugins.add(loadedPluginPanel);
			tabPlugins.add(loadExternalPluginPanel);
			tabPlugins.add(loadBuiltinPluginPanel);
		} // tabPlugins
		
		final JPanel tabCommands = new JPanel();
		{
			m_commandsRegisteredComboBox = new JComboBox<String>();
			m_commandsRegisteredComboBox.setEditable(false);
			m_commandsRegisteredComboBox.addItemListener(this);
			
			final JPanel commandInfoPanel = new JPanel();
			{
				final JPanel commandNamePanel = new JPanel();
				{
					final JLabel commandNamePrefixLabel = new JLabel("<html><u>Name:</u></html>");
					m_commandInfoNameLabel = new JLabel("(No command)");
					
					commandNamePanel.setLayout(new FlowLayout());
					commandNamePanel.add(commandNamePrefixLabel);
					commandNamePanel.add(m_commandInfoNameLabel);
				}
				final JPanel commandUsagePanel = new JPanel();
				{
					final JLabel commandUsagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
					m_commandInfoUsageLabel = new JLabel("(No command)");
					
					commandUsagePanel.setLayout(new FlowLayout());
					commandUsagePanel.add(commandUsagePrefixLabel);
					commandUsagePanel.add(m_commandInfoUsageLabel);
				}
				
				commandInfoPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
				commandInfoPanel.add(commandNamePanel);
				commandInfoPanel.add(commandUsagePanel);
			}
			
			tabCommands.setLayout(new BorderLayout());
			tabCommands.add(m_commandsRegisteredComboBox, BorderLayout.NORTH);
			tabCommands.add(commandInfoPanel, BorderLayout.CENTER);
		} // tabCommands
		
		final JPanel tabChat = new JPanel();
		{
			final JScrollPane chatHistoryScrollPane = new JScrollPane();
			m_chatHistoryList = new LimitedStringList();
			
			chatHistoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			chatHistoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			chatHistoryScrollPane.setViewportView(m_chatHistoryList);
			
			m_chatHistoryList.setComponentPopupMenu(m_textComponentPopupMenu);
			m_chatHistoryList.setFont(m_chatHistoryList.getFont().deriveFont(Font.PLAIN));
			
			tabChat.setBorder(BorderFactory.createLoweredBevelBorder());
			tabChat.setLayout(new BorderLayout());
			tabChat.add(m_chatHistoryList, BorderLayout.CENTER);
		} // tabChat
		
		final JScrollPane tabConsole = new JScrollPane();
		{
			final JTextArea consoleTextArea = new JTextArea(consoleDocument);
			final DefaultCaret caret = new DefaultCaret();
	
			caret.setVisible(true);
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	
			consoleTextArea.setEditable(false);
			consoleTextArea.setCaret(caret);
			consoleTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			consoleTextArea.setComponentPopupMenu(m_textComponentPopupMenu);
			
			tabConsole.setBorder(BorderFactory.createLoweredBevelBorder());
			tabConsole.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			tabConsole.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			tabConsole.setViewportView(consoleTextArea);
		} // tabConsole
		
		m_body.addTab("Status", tabStatus);
		m_body.addTab("Plugin", tabPlugins);
		m_body.addTab("Commands", tabCommands);
		m_body.addTab("Chat", tabChat);
		m_body.addTab("Console", tabConsole);
			
		final JPanel footer = new JPanel();
		{
			final JLabel copyrightAndLicenseLabel = new JLabel("Copyright \u00a9 2016 NuclearCoder. Licensed under A-GPLv3.");
			final JLabel sourceLinkLabel = new JLabel("<html><a href=\"\">Source code here</a></html>");
			
			copyrightAndLicenseLabel.setFont(copyrightAndLicenseLabel.getFont().deriveFont(10F));
			
			sourceLinkLabel.addMouseListener(new HyperlinkListener(GITHUB_URL));
			sourceLinkLabel.setToolTipText(GITHUB_URL);
			sourceLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
			sourceLinkLabel.setFont(sourceLinkLabel.getFont().deriveFont(10F));
			
			footer.setLayout(new BorderLayout());
			footer.add(copyrightAndLicenseLabel, BorderLayout.WEST);
			footer.add(sourceLinkLabel, BorderLayout.EAST);
		} // footer
	
		setLayout(new BorderLayout(5, 5));
		add(m_body, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
		
		m_container.setContentPane(this);
		m_container.pack();
		
		pluginChanged(m_pluginLoader.getPlugin());
	}
	
	public PluginLoader getPluginLoader()
	{
		return m_pluginLoader;
	}
	
	public JFrame getFrame()
	{
		return m_container;
	}
	
	public void open()
	{
		m_container.setVisible(true);
	}
	
	/* **** awt/swing listener methods **** */
	
	@Override
	public void actionPerformed(final ActionEvent event)
	{
		final Object source = event.getSource();
		if (source == m_startButton)
		{
			startClient();
		}
		else if (source == m_stopButton)
		{
			m_doRestartClient = false;
			stopClient();
		}
		else if (source == m_restartButton)
		{
			m_doRestartClient = true;
			stopClient();
		}
		else if (source == m_externalPluginPathField || source == m_externalPluginBrowseButton)
		{
			choosePluginFile();
		}
		else if (source == m_externalPluginLoadButton)
		{
			changePluginExternal();
		}
		else if (source == m_builtinPluginLoadButton)
		{
			changePluginBuiltin();
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent event)
	{
		Object source = event.getSource();
		if (source == m_commandsRegisteredComboBox)
		{
			updateCommandInfo();
		}
	}
	
	/* **** events **** */
	
	private void startClient()
	{
		m_startButton.setEnabled(false);
		
		Logger.info("(GUI) Starting client...");
		
		final String twitchUser = Config.get("twitch_user");
		final String twitchOauthKey = Config.get("twitch_oauth_key");
		final Plugin plugin = m_pluginLoader.getPlugin();
		
		m_client = new ImplChatClient(twitchUser, twitchOauthKey, plugin);
		m_client.registerClientListener(this);
		m_clientThread = new ClientThread(m_client);
		m_clientThread.start();
	}
	
	private void stopClient()
	{
		Logger.info("(GUI) Stopping client...");
		
		m_stopButton.setEnabled(false);
		m_restartButton.setEnabled(false);
		
		m_client.stop();
	}

	private void choosePluginFile()
	{
		Logger.info("(GUI) Opening plugin file dialog...");
		
		m_pluginFileDialog.setVisible(true);
		final String filename = m_pluginFileDialog.getFile();
		if (filename != null)
		{
			m_externalPluginPathField.setText(new File(m_pluginFileDialog.getDirectory(), filename).getAbsolutePath());
			
		}
	}
	
	private void changePluginExternal()
	{
		final File file = new File(m_externalPluginPathField.getText());
		if (file.isFile())
		{
			pluginChanged(m_pluginLoader.loadPlugin(file) ? m_pluginLoader.getPlugin() : null);
		}
		else
		{
			Logger.error("(GUI) Provided path \"" + file.getAbsolutePath() + "\" that wasn't a file.");
			JOptionPane.showMessageDialog(this, "This is not a file!", "Not a file", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void changePluginBuiltin()
	{
		final String pluginClassName = (String) m_builtinPluginsComboBox.getSelectedItem();
		pluginChanged(m_pluginLoader.loadPlugin(pluginClassName) ? m_pluginLoader.getPlugin() : null);
	}
	
	private void updateCommandInfo()
	{
		final String label = (String) m_commandsRegisteredComboBox.getSelectedItem();
		final Command command = m_client.getCommand(label);
		m_commandInfoNameLabel.setText(command != null ? command.getLabel() : "(no such command)");
		m_commandInfoUsageLabel.setText(command != null ? command.getUsage() : "(no such command)");
	}
	
	/* **** notifies **** */
	
	private void clientStarted()
	{
		m_isClientRunning = true;
		
		m_statusRunningLabel.setText("Running");
		m_startButton.setEnabled(false);
		m_stopButton.setEnabled(true);
		m_restartButton.setEnabled(true);
		
		m_doRestartClient = false;
		
		if (m_isFrameClosing)
		{
			stopClient();
		}
	}
	
	private void clientStopped()
	{
		m_statusRunningLabel.setText("Not running");
		m_startButton.setEnabled(true);
		m_stopButton.setEnabled(false);
		m_restartButton.setEnabled(false);
		m_commandsRegisteredComboBox.removeAllItems();
		
		if (m_doRestartClient)
		{
			startClient();
		}
	}
	
	private void clientMessage(final String username, final String message)
	{
		m_chatHistoryList.add("<html><strong>" + username + " :</strong> " + HTML.escapeText(message) + "</html>");
	}
	
	private void commandRegistered(final String label, final Command command)
	{
		m_commandsRegisteredComboBox.addItem(label);
	}
	
	private void commandUnregistered(final String label)
	{
		m_commandsRegisteredComboBox.removeItem(label);
	}
	
	private void frameClosing()
	{
		m_isFrameClosing = true;
		m_pluginFileDialog.dispose();
		m_container.dispose();
		if (m_client != null)
		{
			stopClient();
		}
	}
	
	private void pluginChanged(final JavaPlugin plugin)
	{
		if (plugin != null)
		{
			// italic name if built-in plugin
			final String pluginName = HTML.escapeText(plugin.getName());
			final String pluginClassName = plugin.getClassName();
			final String pluginLabelText = "<html>" + (plugin.isBuiltin() ? "<em>" + pluginName + "</em>" : pluginName) + "</html>";
			
			m_loadedPluginLabel.setText(pluginLabelText);
			m_loadedPluginLabel.setToolTipText(pluginClassName);
			m_currentPluginLabel.setText(pluginLabelText);
			m_currentPluginLabel.setToolTipText(pluginClassName);
			
			if (m_isClientRunning)
			{ // ask to restart if the client is already running
				final int restart = JOptionPane.showConfirmDialog(m_container, "The changes will be not effective until a restart.\nRestart now?", "Restart?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (restart == JOptionPane.YES_OPTION)
				{
//					m_body.setSelectedIndex(0); // switch back to the Status tab
					m_doRestartClient = true;
					stopClient();
				}
			}
		}
		else
		{
			JOptionPane.showMessageDialog(m_container, "Couldn't load the plugin. See console for details.", "Error while loading plugin", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/* **** client listener methods **** */

	@Override
	public void onConnected(final ChatClient client)
	{
		SwingUtilities.invokeLater(new NotifiedStartedRunnable());
	}

	@Override
	public void onDisconnected(final ChatClient client)
	{
		SwingUtilities.invokeLater(new NotifiedStoppedRunnable());
	}
	
	@Override
	public void onMessage(final ChatClient client, final String username, final String message)
	{
		SwingUtilities.invokeLater(new NotifiedMessageRunnable(username, message));
	}
	
	@Override
	public void onCommandRegistered(final ChatClient client, final String label, final Command command)
	{
		SwingUtilities.invokeLater(new NotifiedCommandRegisteredRunnable(label, command));
	}
	
	@Override
	public void onCommandUnregistered(final ChatClient client, final String label)
	{
		SwingUtilities.invokeLater(new NotifiedCommandUnregisteredRunnable(label));
	}
	
	/* **** client thread class **** */

	private class ClientThread implements Runnable {
		
		private Thread m_thread;
		
		private final ChatClient m_client;
		
		public ClientThread(final ChatClient client)
		{
			m_client = client;
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
			}
		}
		
	}
	
	/* **** notifies classes **** */

	private class NotifiedStartedRunnable implements Runnable {
	
		@Override
		public void run()
		{
			clientStarted();
		}
		
	};
	
	private class NotifiedStoppedRunnable implements Runnable {
		
		@Override
		public void run()
		{
			clientStopped();
		}
		
	};
	
	private class NotifiedMessageRunnable implements Runnable {
		
		private final String m_username;
		private final String m_message;
		
		public NotifiedMessageRunnable(final String username, final String message)
		{
			m_username = username;
			m_message = message;
		}
		
		@Override
		public void run()
		{
			clientMessage(m_username, m_message);
		}
		
	};
	
	private class NotifiedCommandRegisteredRunnable implements Runnable {
		
		private final String m_label;
		private final Command m_command;
		
		public NotifiedCommandRegisteredRunnable(final String label, final Command command)
		{
			m_label = label;
			m_command = command;
		}
		
		@Override
		public void run()
		{
			commandRegistered(m_label, m_command);
		}
		
	}
	
	private class NotifiedCommandUnregisteredRunnable implements Runnable {
		
		private final String m_label;
		
		public NotifiedCommandUnregisteredRunnable(final String label)
		{
			m_label = label;
		}
		
		@Override
		public void run()
		{
			commandUnregistered(m_label);
		}
		
	}
	
	private class NotifiedClosingListener extends WindowAdapter {
		
		@Override
		public void windowClosing(final WindowEvent event)
		{
			frameClosing();
		}
		
	}
	
	/* **** gui classes **** */
	
	private class HyperlinkListener extends MouseAdapter {
		
		private URI m_uri;
		
		public HyperlinkListener(final String url)
		{
			try
			{
				m_uri = new URL(url).toURI();
			}
			catch (MalformedURLException | URISyntaxException e)
			{
				Logger.error("(GUI) Bad URL \"" + url + "\":");
				Logger.printStackTrace(e);
			}
		}
		
		@Override
		public void mouseClicked(final MouseEvent event)
		{
			if (m_uri != null && Desktop.isDesktopSupported())
			{
				try
				{
					Desktop.getDesktop().browse(m_uri);
				}
				catch (IOException e)
				{
					Logger.error("(GUI) Browser issued an I/O exception (\"" + m_uri.toString() + "\"):");
					Logger.printStackTrace(e);
				}
			}
			else
			{
				Logger.error("(GUI) Desktop not supported.");
			}
		}
		
	}
	
	/* **** misc. classes **** */

	private class PluginFilenameFilter implements FilenameFilter {
	
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".jar");
		}
		
	}
	
}

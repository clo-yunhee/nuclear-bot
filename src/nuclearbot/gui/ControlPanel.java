package nuclearbot.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.client.ChatClient;
import nuclearbot.client.ClientListener;
import nuclearbot.client.Command;
import nuclearbot.client.ImplChatClient;
import nuclearbot.plugin.ImplPluginLoader;
import nuclearbot.plugin.JavaPlugin;
import nuclearbot.plugin.Plugin;
import nuclearbot.plugin.PluginLoader;
import nuclearbot.util.Config;
import nuclearbot.util.HTML;
import nuclearbot.util.Logger;
import nuclearbot.util.OSUtils;

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
@SuppressWarnings("unused")
public class ControlPanel extends JPanel implements ActionListener, ItemListener, FocusListener, ClientListener {
	
	private static final long serialVersionUID = 606418561134403181L;

	private static final String GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/";
	
	private static final int TAB_STATUS = 0;
	private static final int TAB_PLUGIN = 1;
	private static final int TAB_COMMANDS = 2;
	private static final int TAB_CHAT = 3;
	private static final int TAB_CONFIG = 4;
	private static final int TAB_CONSOLE = 5;
	
	// GUI components
	
	private final DialogUtil m_dialogs;
	
	private final JPopupMenu m_textComponentPopupMenu;
	private final FileDialog m_pluginFileDialog;
	
	private final JFrame m_container;
	private final JTabbedPane m_body;
	
	private final JLabel m_runningStatusLabel;
	private final JButton m_startButton;
	private final JButton m_stopButton;
	private final JButton m_restartButton;
	private final JLabel m_currentPluginLabel;
	
	private final JLabel m_loadedPluginLabel;
	private final JTextField m_externalPluginPathField;
	private final JButton m_externalPluginBrowseButton;
	private final JButton m_loadExternalPluginButton;
	private final JComboBox<String> m_builtinPluginsCombo;
	private final JButton m_loadBuiltinPluginButton;
	
	private final JComboBox<String> m_commandsRegisteredCombo;
	private final JLabel m_commandUsageLabel;
	private final JLabel m_commandDescriptionLabel;
	private final JComboBox<String> m_userCommandsCombo;
	private final JTextField m_commandUsageField;
	private final JTextField m_commandDescriptionField;
	private final JTextField m_commandResponseField;
	private final JCheckBox m_commandPersistentCheck;
	private final JButton m_commandCreateButton;
	private final JButton m_commandRemoveButton;
	private final JButton m_commandCreationHelpButton;
	
	private final LimitedStringList m_chatHistoryList;
	private final JTextField m_chatMessageField;
	private final JButton m_sendMessageButton;
	
	private final JButton m_reloadConfigButton;
	
	// client stuff
	
	private final PluginLoader m_pluginLoader;

	private boolean m_isFrameClosing; // window is closing?
	private boolean m_doRestartClient; // restart after the client is stopped?

	private boolean m_isClientRunning; // client is running?
	private ClientThread m_clientThread;
	private ChatClient m_client;
	
	private final Map<String, List<String>> m_userCommands;

	// constructor
	public ControlPanel()
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
		
		m_userCommands = new HashMap<String, List<String>>();
		
		// gui variables init
		
		Logger.info("(GUI) Constructing window...");

		m_container = new JFrame("NuclearBot - Control Panel");
		m_container.setLocationByPlatform(true);
		m_container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		m_container.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event)
			{
				frameClosing();
			}
		});

		m_dialogs = new DialogUtil(m_container);

		m_pluginFileDialog = new FileDialog(m_container, "Choose a file", FileDialog.LOAD);
		m_pluginFileDialog.setLocationRelativeTo(m_container);
		m_pluginFileDialog.setDirectory(OSUtils.workingDir());
		m_pluginFileDialog.setFile("*.jar");
		m_pluginFileDialog.setFilenameFilter(new FilenameFilter() {
			@Override
			public boolean accept(final File dir, final String name)
			{
				return name.endsWith(".jar");
			}
		});
		
		m_textComponentPopupMenu = new JPopupMenu();
		{
			m_textComponentPopupMenu.add(TransferHandler.getCopyAction());
		}
		
		m_body = new JTabbedPane();
		
		final JPanel tabStatus = new JPanel();
		{
			final JPanel currentPluginPanel = new JPanel();
			{
				final JLabel currentPluginPrefixLabel = new JLabel("<html><u>Current plugin:</u></html>");
				m_currentPluginLabel = new JLabel();
				
				m_currentPluginLabel.setFont(m_currentPluginLabel.getFont().deriveFont(Font.PLAIN));
				m_currentPluginLabel.setHorizontalAlignment(SwingConstants.CENTER);
				m_currentPluginLabel.setComponentPopupMenu(m_textComponentPopupMenu);
				
				currentPluginPanel.setLayout(new FlowLayout());
				currentPluginPanel.add(currentPluginPrefixLabel);
				currentPluginPanel.add(m_currentPluginLabel);
			} // currentPluginPanel
			
			final JPanel controlButtonsPanel = new JPanel();
			{
				m_runningStatusLabel = new JLabel("Not running");
				m_startButton = new JButton("Start");
				m_stopButton = new JButton("Stop");
				m_restartButton = new JButton("Restart");
				
				m_runningStatusLabel.setFont(m_runningStatusLabel.getFont().deriveFont(Font.ITALIC));
				m_startButton.addActionListener(this);
				m_stopButton.addActionListener(this);
				m_stopButton.setEnabled(false);
				m_restartButton.addActionListener(this);
				m_restartButton.setEnabled(false);
				
				controlButtonsPanel.setLayout(new FlowLayout());
				controlButtonsPanel.add(m_runningStatusLabel);
				controlButtonsPanel.add(m_startButton);
				controlButtonsPanel.add(m_stopButton);
				controlButtonsPanel.add(m_restartButton);
			} // controlButtonsPanel
			
			tabStatus.setLayout(new VerticalLayout());
			tabStatus.add(controlButtonsPanel);
			tabStatus.add(currentPluginPanel);
		} // tabStatus
		
		final JScrollPane tabPlugins = new JScrollPane();
		{
			final JPanel pluginPanel = new JPanel();
			{
				final JPanel loadedPluginPanel = new JPanel();
				{
					m_loadedPluginLabel = new JLabel();
					
					m_loadedPluginLabel.setHorizontalAlignment(SwingConstants.CENTER);
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
					m_loadExternalPluginButton = new JButton("Load");
					
					m_externalPluginPathField.setComponentPopupMenu(m_textComponentPopupMenu);
					m_externalPluginPathField.addActionListener(this);
					m_externalPluginBrowseButton.addActionListener(this);
					m_loadExternalPluginButton.addActionListener(this);
					
					loadExternalPluginPanel.setBorder(BorderFactory.createTitledBorder("Load an external plugin"));
					loadExternalPluginPanel.setLayout(new FlowLayout());
					loadExternalPluginPanel.add(m_externalPluginPathField);
					loadExternalPluginPanel.add(m_externalPluginBrowseButton);
					loadExternalPluginPanel.add(m_loadExternalPluginButton);
				} // loadExternalPluginPanel
				
				final JPanel loadBuiltinPluginPanel = new JPanel();
				{
					m_builtinPluginsCombo = new JComboBox<String>(m_pluginLoader.getBuiltinPlugins());
					m_loadBuiltinPluginButton = new JButton("Load");
					
					m_builtinPluginsCombo.setEditable(false);
					m_builtinPluginsCombo.setSelectedItem(DummyPlugin.class.getName());
					m_builtinPluginsCombo.setFont(m_builtinPluginsCombo.getFont().deriveFont(Font.PLAIN));
					m_loadBuiltinPluginButton.addActionListener(this);
					
					loadBuiltinPluginPanel.setBorder(BorderFactory.createTitledBorder("Load a built-in plugin"));
					loadBuiltinPluginPanel.setLayout(new FlowLayout());
					loadBuiltinPluginPanel.add(m_builtinPluginsCombo);
					loadBuiltinPluginPanel.add(m_loadBuiltinPluginButton);
				} // loadBuiltinPluginPanel
				
				pluginPanel.setLayout(new VerticalLayout());
				pluginPanel.add(loadedPluginPanel);
				pluginPanel.add(loadExternalPluginPanel);
				pluginPanel.add(loadBuiltinPluginPanel);
			} // pluginPanel
			
			tabPlugins.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			tabPlugins.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			tabPlugins.setViewportView(pluginPanel);
		} // tabPlugins
		
		final JPanel tabCommands = new JPanel();
		{
			final JPanel commandOverviewPanel = new JPanel();
			{
				final JPanel commandNamePanel = new JPanel();
				{
					final JLabel commandNamePrefixLabel = new JLabel("<html><u>Name:</u></html>");
					m_commandsRegisteredCombo = new JComboBox<String>();
					
					m_commandsRegisteredCombo.setEditable(false);
					m_commandsRegisteredCombo.addItemListener(this);
					
					commandNamePanel.setLayout(new FlowLayout());
					commandNamePanel.add(commandNamePrefixLabel);
					commandNamePanel.add(m_commandsRegisteredCombo);
				} // commandNamePanel
				
				final JPanel commandUsagePanel = new JPanel();
				{
					final JLabel commandUsagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
					m_commandUsageLabel = new JLabel("(no such command)");
					
					commandUsagePrefixLabel.setToolTipText("Angle brackets for required arguments\nSquare brackets for optional arguments");
					m_commandUsageLabel.setToolTipText("Angle brackets for required arguments\nSquare brackets for optional arguments");
					
					commandUsagePanel.setLayout(new FlowLayout());
					commandUsagePanel.add(commandUsagePrefixLabel);
					commandUsagePanel.add(m_commandUsageLabel);
				} // commandUsagePanel
				
				final JPanel commandDescriptionPanel = new JPanel();
				{
					final JLabel commandDescriptionPrefixLabel = new JLabel("<html><u>Description:</u></html>");
					m_commandDescriptionLabel = new JLabel("(no such command)");
					
					commandDescriptionPanel.setLayout(new FlowLayout());
					commandDescriptionPanel.add(commandDescriptionPrefixLabel);
					commandDescriptionPanel.add(m_commandDescriptionLabel);
				} // commandDescriptionPanel
				
				commandOverviewPanel.setBorder(BorderFactory.createTitledBorder("Overview"));
				commandOverviewPanel.setLayout(new VerticalLayout());
				commandOverviewPanel.add(commandNamePanel);
				commandOverviewPanel.add(commandUsagePanel);
				commandOverviewPanel.add(commandDescriptionPanel);
			}
			
			final JPanel commandCreationPanel = new JPanel();
			{
				final JPanel commandNamePanel = new JPanel();
				{
					final JLabel commandNamePrefixLabel = new JLabel("<html><u>Name:</u></html>");
					m_userCommandsCombo = new JComboBox<String>();

					m_userCommandsCombo.setEditable(true);
					m_userCommandsCombo.addFocusListener(this);
					m_userCommandsCombo.addActionListener(this);
					
					commandNamePanel.setLayout(new FlowLayout());
					commandNamePanel.add(commandNamePrefixLabel);
					commandNamePanel.add(m_userCommandsCombo);
				} // commandNamePanel
				
				final JPanel commandUsagePanel = new JPanel();
				{
					final JLabel commandUsagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
					m_commandUsageField = new JTextField(12);
					
					commandUsagePanel.setLayout(new FlowLayout());
					commandUsagePanel.add(commandUsagePrefixLabel);
					commandUsagePanel.add(m_commandUsageField);
				} // commandUsagePanel
				
				final JPanel commandDescriptionPanel = new JPanel();
				{
					final JLabel commandDescriptionPrefixLabel = new JLabel("<html><u>Description:</u></html>");
					m_commandDescriptionField = new JTextField(16);
					
					commandDescriptionPanel.setLayout(new FlowLayout());
					commandDescriptionPanel.add(commandDescriptionPrefixLabel);
					commandDescriptionPanel.add(m_commandDescriptionField);
				} // commandDescriptionPanel
				
				final JPanel commandResponsePanel = new JPanel();
				{
					final JLabel commandResponsePrefixLabel = new JLabel("<html><u>Response:</u></html>");
					m_commandResponseField = new JTextField(16);
					
					commandResponsePanel.setLayout(new FlowLayout());
					commandResponsePanel.add(commandResponsePrefixLabel);
					commandResponsePanel.add(m_commandResponseField);
				} // commandResponsePanel
				
				final JPanel commandSubmitPanel = new JPanel();
				{
					m_commandPersistentCheck = new JCheckBox("Persistent?", true);
					m_commandCreateButton = new JButton("Create/Update");
					m_commandRemoveButton = new JButton("Remove");
					m_commandCreationHelpButton = new JButton("Help...");
					
					m_commandCreateButton.setEnabled(false);
					m_commandCreateButton.addActionListener(this);
					m_commandRemoveButton.setEnabled(false);
					m_commandRemoveButton.addActionListener(this);
					m_commandCreationHelpButton.addActionListener(this);
					
					commandSubmitPanel.setLayout(new FlowLayout());
					commandSubmitPanel.add(m_commandPersistentCheck);
					commandSubmitPanel.add(m_commandCreateButton);
					commandSubmitPanel.add(m_commandRemoveButton);
					commandSubmitPanel.add(m_commandCreationHelpButton);
				} // commandSubmitPanel

				commandCreationPanel.setBorder(BorderFactory.createTitledBorder("Create/Update"));
				commandCreationPanel.setLayout(new VerticalLayout());
				commandCreationPanel.add(commandNamePanel);
				commandCreationPanel.add(commandUsagePanel);
				commandCreationPanel.add(commandDescriptionPanel);
				commandCreationPanel.add(commandResponsePanel);
				commandCreationPanel.add(commandSubmitPanel);
			} // commandCreationPanel
			
			tabCommands.setLayout(new GridLayout(0, 2));
			tabCommands.add(commandOverviewPanel);
			tabCommands.add(commandCreationPanel);
		} // tabCommands
		
		final JPanel tabChat = new JPanel();
		{
			final JScrollPane chatHistoryScrollPane = new JScrollPane();
			m_chatHistoryList = new LimitedStringList();
			final JPanel sendMessagePanel = new JPanel();
			{
				m_chatMessageField = new JTextField();
				m_sendMessageButton = new JButton("Send");
				
				m_chatMessageField.setFont(m_chatMessageField.getFont().deriveFont(Font.PLAIN));
				m_chatMessageField.addActionListener(this);
				m_sendMessageButton.setEnabled(false);
				m_sendMessageButton.addActionListener(this);
				
				sendMessagePanel.setLayout(new BorderLayout());
				sendMessagePanel.add(m_chatMessageField, BorderLayout.CENTER);
				sendMessagePanel.add(m_sendMessageButton, BorderLayout.EAST);
			} // sendMessagePanel
			
			chatHistoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			chatHistoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			chatHistoryScrollPane.setViewportView(m_chatHistoryList);
			
			m_chatHistoryList.setComponentPopupMenu(m_textComponentPopupMenu);
			m_chatHistoryList.setFont(m_chatHistoryList.getFont().deriveFont(Font.PLAIN));
			
			tabChat.setLayout(new BorderLayout());
			tabChat.add(chatHistoryScrollPane, BorderLayout.CENTER);
			tabChat.add(sendMessagePanel, BorderLayout.SOUTH);
		} // tabChat
		
		final JScrollPane tabConfig = new JScrollPane();
		{
			final JPanel configPanel = new JPanel();
			{
				final JPanel configListPanel = new JPanel();
				{
					// TODO: config here
					
				} // configListPanel
				
				final JPanel configButtonsPanel = new JPanel();
				{
					m_reloadConfigButton = new JButton("Reload config");
					
					m_reloadConfigButton.addActionListener(this);
					
					configButtonsPanel.setLayout(new FlowLayout());
					configButtonsPanel.add(m_reloadConfigButton);
				} // configButtonsPanel
				
				configPanel.setLayout(new VerticalLayout());
				configPanel.add(configListPanel);
				configPanel.add(configButtonsPanel);
			} // configPanel
			
			tabConfig.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			tabConfig.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			tabConfig.setViewportView(configPanel);
		} // tabConfig
		
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
			
			tabConsole.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			tabConsole.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			tabConsole.setViewportView(consoleTextArea);
		} // tabConsole
		
		m_body.addTab("Status", tabStatus);
		m_body.addTab("Plugin", tabPlugins);
		m_body.addTab("Commands", tabCommands);
		m_body.addTab("Chat", tabChat);
		m_body.addTab("Config", tabConfig);
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
	
		setLayout(new BorderLayout());
		add(m_body, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
		
		m_container.setContentPane(this);
		m_container.pack();
		
		pluginChanged(m_pluginLoader.getPlugin());
		reloadUserCommands(true);
	}
	
	public void open()
	{
		m_container.setVisible(true);
		m_dialogs.setQueueDialogs(false);
	}
	
	private void createUserCommand(final String name, final String usage, final String description, final String response, final boolean persistent, final boolean silent)
	{
		if (m_userCommands.containsKey(name))
		{
			Logger.info("(GUI) Updating command \"" + name + "\"...");
			if (m_isClientRunning)
			{
				m_client.unregisterCommand(name);
			}
			m_userCommands.remove(name);
			m_userCommandsCombo.removeItem(name);
		}
		else
		{
			Logger.info("(GUI) Creating command \"" + name + "\"...");
		}
		if (m_isClientRunning)
		{
			if (m_client.getCommand(name) != null)
			{
				final String realUsage = '!' + name + ' ' + usage;
				final UserCommand executor = new UserCommand(response);
				m_client.registerCommand(name, realUsage, executor).setDescription(description);
			}
			else
			{
				Logger.warning("(GUI) Command \"" + name + "\" already exists.");
				if (!silent)
				{
					m_dialogs.warning("Command \"" + name + "\" already exists.", "Command already registered");
				}
			}
		}
		
		m_userCommandsCombo.addItem(name);
		
		Logger.info("(GUI) Command \"" + name + "\" successfully created.");
		if (!silent)
		{
			m_dialogs.info("Command \"" + name + "\" succesfully created.", "Command created");
		}
		
		if (persistent)
		{
			final List<String> commandData = new ArrayList<String>(3);
			commandData.add(usage);
			commandData.add(description);
			commandData.add(response);
			m_userCommands.put(name, commandData);
			
			Config.set("user_commands", new Gson().toJson(m_userCommands, Map.class));
			
			try
			{
				Config.saveConfig();
			}
			catch (IOException e)
			{
				Logger.error("(GUI) Couldn't save persistent user command:");
				Logger.printStackTrace(e);
				if (!silent)
				{
					m_dialogs.error("Couldn't save persistent user command. Check console for details.", "Couldn't save config");
				}
			}
		}
	}

	private void reloadUserCommands(final boolean silent)
	{
		m_userCommands.clear();
		try
		{
			final Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
			final Map<String, List<String>> map = new Gson().fromJson(Config.get("user_commands"), type);
			for (final Map.Entry<String, List<String>> entry : map.entrySet())
			{
				final String name = entry.getKey();
				final List<String> commandData = entry.getValue();
				final String usage = commandData.get(0).trim();
				final String description = commandData.get(1).trim();
				final String response = commandData.get(2).trim();
				
				createUserCommand(name, usage, description, response, false, silent);
			}
			if (!silent)
			{
				m_dialogs.info("User commands successfully reloaded!", "User commands reloaded");
			}
		}
		catch (JsonSyntaxException e)
		{
			Logger.error("(GUI) JSON syntax error while loading user commands:");
			Logger.printStackTrace(e);
			m_dialogs.error("JSON syntax error in the user commands configuration. Check console for details.", "JSON syntax error");
		}
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
		else if (source == m_loadExternalPluginButton)
		{
			loadPluginExternal();
		}
		else if (source == m_loadBuiltinPluginButton)
		{
			loadPluginBuiltin();
		}
		else if (source == m_userCommandsCombo)
		{
			updateUserCommandInfo();
		}
		else if (source == m_commandCreateButton)
		{
			createUserCommand();
		}
		else if (source == m_commandRemoveButton)
		{
			removeUserCommand();
		}
		else if (source == m_commandCreationHelpButton)
		{
			openCommandCreationHelp();
		}
		else if (source == m_chatMessageField || source == m_sendMessageButton)
		{
			sendChatMessage();
		}
		else if (source == m_reloadConfigButton)
		{
			reloadConfig();
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent event)
	{
		Object source = event.getSource();
		if (source == m_commandsRegisteredCombo)
		{
			updateCommandInfo();
		}
	}
	
	@Override
	public void focusGained(FocusEvent event)
	{
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		Object source = event.getSource();
		if (source == m_userCommandsCombo)
		{
			updateUserCommandInfo();
		}
	}
	
	/* **** events **** */
	
	private void startClient()
	{
		m_startButton.setEnabled(false);
		
		Logger.info("(GUI) Starting client...");
		
		final Plugin plugin = m_pluginLoader.getPlugin();
		
		m_client = new ImplChatClient(plugin);
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
	
	private void loadPluginExternal()
	{
		final File file = new File(m_externalPluginPathField.getText());
		if (file.isFile())
		{
			pluginChanged(m_pluginLoader.loadPlugin(file) ? m_pluginLoader.getPlugin() : null);
		}
		else
		{
			Logger.error("(GUI) Provided path \"" + file.getAbsolutePath() + "\" that wasn't a file.");
			m_dialogs.error("This is not a file!", "Not a file");
		}
	}
	
	private void loadPluginBuiltin()
	{
		final String pluginClassName = (String) m_builtinPluginsCombo.getSelectedItem();
		pluginChanged(m_pluginLoader.loadPlugin(pluginClassName) ? m_pluginLoader.getPlugin() : null);
	}
	
	private void updateCommandInfo()
	{
		final String label = (String) m_commandsRegisteredCombo.getSelectedItem();
		final Command command = m_client.getCommand(label);
		m_commandUsageLabel.setText(command != null ? command.getUsage() : "(no such command)");
		m_commandDescriptionLabel.setText(command != null ? command.getDescription() : "(no such command)");
	}
	
	private void updateUserCommandInfo()
	{
		final String label = (String) m_userCommandsCombo.getSelectedItem();
		final List<String> commandData = m_userCommands.get(label);
		m_commandUsageField.setText(commandData != null ? commandData.get(0) : "");
		m_commandDescriptionField.setText(commandData != null ? commandData.get(1) : "");
		m_commandResponseField.setText(commandData != null ? commandData.get(2) : "");
	}
	
	private void removeUserCommand()
	{
		final String name = ((String) m_userCommandsCombo.getSelectedItem()).trim().toLowerCase();
		if (m_userCommands.containsKey(name))
		{
			m_client.unregisterCommand(name);
			m_userCommands.remove(name);
			m_userCommandsCombo.removeItem(name);
		}
		else
		{
			m_dialogs.warning("Command \"" + name + "\" is not an user command.", "Not an user command");
		}
	}
	
	private void createUserCommand()
	{
		final String name = ((String) m_userCommandsCombo.getSelectedItem()).trim().toLowerCase();
		final String usage = m_commandUsageField.getText().trim();
		final String description = m_commandDescriptionField.getText().trim();
		final String response = m_commandResponseField.getText().trim();
		final boolean persistent = m_commandPersistentCheck.isSelected();
		
		createUserCommand(name, usage, description, response, persistent, false);
	}
	
	private void openCommandCreationHelp()
	{
		m_dialogs.info("The convention for usage format is the following:\n"
				+ "- angle brackets for required arguments;\n"
				+ "- square brackets for optional arguments;\n"
				+ "but the GUI creator doesn't support optional arguments yet.\n"
				+ "Please only write arguments without the command in the usage field.\n\n"
				+ "To include arguments in the response, use \"$n\" or \"{$n}\", where n is the argument index.\n"
				+ "The zero-th argument is the sender's username.\n"
				+ "For instance, to create a hug command, put \"<target>\" in usage and \"$0 hugs $1.\" in response.\n\n"
				+ "By default the commands are created persistent, which means they will be created again on start.\n"
				+ "Non-persistent commands are lost once the bot stops or restarts.\n"
				+ "The GUI will however remember non-persistent commands' data until it is closed.", "Command creation help");
	}
	
	private void sendChatMessage()
	{
		if (m_isClientRunning)
		{
			final String message = m_chatMessageField.getText().trim();
			if (!message.isEmpty())
			{
				m_client.sendMessage(message);
				m_chatMessageField.setText("");
			}
		}
	}
	
	private void reloadConfig()
	{
		try
		{
			Config.reloadConfig();
			reloadUserCommands(false);
			m_dialogs.info("Config was reloaded successfully.", "Config reloaded");
		}
		catch (IOException e)
		{
			Logger.error("(GUI) Exception while reloading config:");
			Logger.printStackTrace(e);
			m_body.setSelectedIndex(TAB_CONSOLE);
			m_dialogs.error("Exception while reloading config. Check console for details.", "Couldn't reload config");
		}
	}
	
	/* **** notifies **** */
	
	private void clientStarted()
	{
		m_isClientRunning = true;
		
		m_runningStatusLabel.setText("Running");
		m_startButton.setEnabled(false);
		m_stopButton.setEnabled(true);
		m_restartButton.setEnabled(true);
		m_commandCreateButton.setEnabled(true);
		m_commandRemoveButton.setEnabled(true);
		m_sendMessageButton.setEnabled(true);
		
		m_doRestartClient = false;
		
		if (m_isFrameClosing)
		{
			stopClient();
		}
		else 
		{
			// if we are not closing, we can try to register user commands
			List<String> commandData;
			String usage;
			String description;
			String response;
			UserCommand executor;
			for (final String name : m_userCommands.keySet())
			{
				commandData = m_userCommands.get(name);
				usage = '!' + name + ' ' + commandData.get(0);
				description = commandData.get(1);
				response = commandData.get(2);
				executor = new UserCommand(response);
				
				try
				{
					m_client.registerCommand(name, usage, executor).setDescription(description);
				}
				catch (IllegalArgumentException e)
				{
					Logger.warning("(GUI) User command \"" + name + "\" was already registered by something else.");
					m_dialogs.warning("User command \"" + name + "\" was already registered by something else.", "Command already registered");
				}
			}
		}
	}
	
	private void clientStopped()
	{
		m_runningStatusLabel.setText("Not running");
		m_startButton.setEnabled(true);
		m_stopButton.setEnabled(false);
		m_restartButton.setEnabled(false);
		m_commandCreateButton.setEnabled(false);
		m_commandRemoveButton.setEnabled(false);
		m_sendMessageButton.setEnabled(false);
		m_commandsRegisteredCombo.removeAllItems();
		
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
		m_commandsRegisteredCombo.addItem(label);
	}
	
	private void commandUnregistered(final String label)
	{
		m_commandsRegisteredCombo.removeItem(label);
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
			m_body.setSelectedIndex(TAB_CONSOLE);
			m_dialogs.error("Couldn't load the plugin. Check console for details.", "Error while loading plugin");
		}
	}
	
	/* **** client listener methods **** */

	@Override
	public void onConnected(final ChatClient client)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				clientStarted();
			}
		});
	}

	@Override
	public void onDisconnected(final ChatClient client)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				clientStopped();
			}
		});
	}
	
	@Override
	public void onMessage(final ChatClient client, final String username, final String message)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				clientMessage(username, message);
			}
		});
	}
	
	@Override
	public void onCommandRegistered(final ChatClient client, final String label, final Command command)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				commandRegistered(label, command);
			}
		});
	}
	
	@Override
	public void onCommandUnregistered(final ChatClient client, final String label)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				commandUnregistered(label);
			}
		});
	}
	
	/* **** client thread class **** */

	private class ClientThread implements Runnable {
		
		private Thread m_thread;
		
		private final ChatClient m_client;
		
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
				onDisconnected(m_client);
				m_body.setSelectedIndex(TAB_CONSOLE);
				m_dialogs.error("Exception in client thread. Check console for details.", "Client error");
			}
		}
		
	}
	
	/* **** misc. classes **** */
	
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
	
}

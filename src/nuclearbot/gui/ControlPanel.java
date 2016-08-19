package nuclearbot.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.glass.events.KeyEvent;

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
	private final JComboBox<String> m_builtinPluginsCombo;
	private final JButton m_builtinPluginLoadButton;
	
	private final JComboBox<String> m_commandsRegisteredCombo;
	private final JLabel m_commandNameLabel;
	private final JLabel m_commandUsageLabel;
	private final JLabel m_commandDescriptionLabel;
	private final JTextField m_commandNameField;
	private final JTextField m_commandUsageField;
	private final JTextField m_commandDescriptionField;
	private final JTextField m_commandResponseField;
	private final JCheckBox m_commandPersistentCheck;
	private final JButton m_commandCreateButton;
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
	
	private final Set<List<String>> m_userCommands;

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
		
		m_userCommands = new HashSet<List<String>>();
		
		Logger.info("(GUI) Constructing window...");		

		m_container = new JFrame("NuclearBot - Control Panel");
		m_container.setLocationByPlatform(true);
		m_container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		m_container.addWindowListener(new NotifiedClosingListener());

		m_pluginFileDialog = new FileDialog(m_container, "Choose a file", FileDialog.LOAD);
		m_pluginFileDialog.setLocationRelativeTo(m_container);
		m_pluginFileDialog.setDirectory(OSUtils.workingDir());
		m_pluginFileDialog.setFile("*.jar");
		m_pluginFileDialog.setFilenameFilter(new PluginFilenameFilter());
		
		m_textComponentPopupMenu = new JPopupMenu();
		{
			final JMenuItem copyMenuItem = new JMenuItem("Copy");
			
			copyMenuItem.setAction(TransferHandler.getCopyAction());
			copyMenuItem.setMnemonic(KeyEvent.VK_C);
			
			m_textComponentPopupMenu.add(copyMenuItem);
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
			
			tabStatus.setLayout(new VerticalLayout());
			tabStatus.add(controlButtonsPanel);
			tabStatus.add(currentPluginPanel);
		} // tabStatus
		
		final JScrollPane tabPlugins = new JScrollPane();
		{
			final JPanel pluginsPanel = new JPanel();
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
					m_externalPluginLoadButton = new JButton("Load");
					
					m_externalPluginPathField.setComponentPopupMenu(m_textComponentPopupMenu);
					m_externalPluginPathField.addActionListener(this);
					m_externalPluginBrowseButton.addActionListener(this);
					m_externalPluginLoadButton.addActionListener(this);
					
					loadExternalPluginPanel.setBorder(BorderFactory.createTitledBorder("Load an external plugin"));
					loadExternalPluginPanel.setLayout(new FlowLayout());
					loadExternalPluginPanel.add(m_externalPluginPathField);
					loadExternalPluginPanel.add(m_externalPluginBrowseButton);
					loadExternalPluginPanel.add(m_externalPluginLoadButton);
				} // loadExternalPluginPanel
				
				final JPanel loadBuiltinPluginPanel = new JPanel();
				{
					m_builtinPluginsCombo = new JComboBox<String>(m_pluginLoader.getBuiltinPlugins());
					m_builtinPluginLoadButton = new JButton("Load");
					
					m_builtinPluginsCombo.setEditable(false);
					m_builtinPluginsCombo.setSelectedItem(DummyPlugin.class.getName());
					m_builtinPluginsCombo.setFont(m_builtinPluginsCombo.getFont().deriveFont(Font.PLAIN));
					m_builtinPluginLoadButton.addActionListener(this);
					
					loadBuiltinPluginPanel.setBorder(BorderFactory.createTitledBorder("Load a built-in plugin"));
					loadBuiltinPluginPanel.setLayout(new FlowLayout());
					loadBuiltinPluginPanel.add(m_builtinPluginsCombo);
					loadBuiltinPluginPanel.add(m_builtinPluginLoadButton);
				} // loadBuiltinPluginPanel
				
				pluginsPanel.setLayout(new VerticalLayout());
				pluginsPanel.add(loadedPluginPanel);
				pluginsPanel.add(loadExternalPluginPanel);
				pluginsPanel.add(loadBuiltinPluginPanel);
			} // pluginsPanel
			
			tabPlugins.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			tabPlugins.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			tabPlugins.setViewportView(pluginsPanel);
		} // tabPlugins
		
		final JPanel tabCommands = new JPanel();
		{
			final JPanel commandOverviewPanel = new JPanel();
			{
				final JPanel commandsListPanel = new JPanel();
				{
					final JLabel commandsListPrefixLabel = new JLabel("Overview: "); 
					m_commandsRegisteredCombo = new JComboBox<String>();
					
					((JLabel) m_commandsRegisteredCombo.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
					m_commandsRegisteredCombo.setEditable(false);
					m_commandsRegisteredCombo.addItemListener(this);
					
					commandsListPanel.setLayout(new FlowLayout());
					commandsListPanel.add(commandsListPrefixLabel);
					commandsListPanel.add(m_commandsRegisteredCombo);
				} // commandsListPanel
				
				final JPanel commandInfoPanel = new JPanel();
				{
					final JPanel commandNamePanel = new JPanel();
					{
						final JLabel commandNamePrefixLabel = new JLabel("<html><u>Name:</u></html>");
						m_commandNameLabel = new JLabel("(no such command)");
						
						commandNamePanel.setLayout(new FlowLayout());
						commandNamePanel.add(commandNamePrefixLabel);
						commandNamePanel.add(m_commandNameLabel);
					} // commandNamePanel
					
					final JPanel commandUsagePanel = new JPanel();
					{
						final JLabel commandUsagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
						m_commandUsageLabel = new JLabel("(no such command)");
						
						commandUsagePrefixLabel.setToolTipText("Angle brackets for required arguments\nSquare brackets for optional arguments");
						
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
					
					commandInfoPanel.setLayout(new VerticalLayout());
					commandInfoPanel.add(commandNamePanel);
					commandInfoPanel.add(commandUsagePanel);
					commandInfoPanel.add(commandDescriptionPanel);
				} // commandInfoPanel
				
				commandOverviewPanel.setBorder(BorderFactory.createTitledBorder("Command overview"));
				commandOverviewPanel.setLayout(new BorderLayout());
				commandOverviewPanel.add(commandsListPanel, BorderLayout.NORTH);
				commandOverviewPanel.add(commandInfoPanel, BorderLayout.CENTER);
			}
			
			final JPanel commandCreationPanel = new JPanel();
			{
				final JPanel commandNamePanel = new JPanel();
				{
					final JLabel commandNamePrefixLabel = new JLabel("<html><u>Name:</u></html>");
					m_commandNameField = new JTextField(8);
					
					((PlainDocument) m_commandNameField.getDocument()).setDocumentFilter(new AlnumDocumentFilter());
					
					commandNamePanel.setLayout(new FlowLayout());
					commandNamePanel.add(commandNamePrefixLabel);
					commandNamePanel.add(m_commandNameField);
				} // commandNamePanel
				
				final JPanel commandUsagePanel = new JPanel();
				{
					final JLabel commandUsagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
					m_commandUsageField = new JTextField(16);
					
					commandUsagePrefixLabel.setToolTipText("Angle brackets for required arguments");
					
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
					m_commandCreateButton = new JButton("Create");
					m_commandCreationHelpButton = new JButton("Help...");
					
					m_commandCreateButton.setEnabled(false);
					m_commandCreateButton.addActionListener(this);
					m_commandCreationHelpButton.addActionListener(this);
					
					commandSubmitPanel.setLayout(new FlowLayout());
					commandSubmitPanel.add(m_commandPersistentCheck);
					commandSubmitPanel.add(m_commandCreateButton);
					commandSubmitPanel.add(m_commandCreationHelpButton);
				} // commandSubmitPanel

				commandCreationPanel.setBorder(BorderFactory.createTitledBorder("Command creation"));
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
				m_sendMessageButton = new JButton("Say");
				
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
		loadUserCommands();
	}
	
	public void open()
	{
		m_container.setVisible(true);
	}
	
	private void showInfoDialog(final String message, final String title)
	{
		JOptionPane.showMessageDialog(m_container, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void showWarningDialog(final String message, final String title)
	{
		JOptionPane.showMessageDialog(m_container, message, title, JOptionPane.WARNING_MESSAGE);
	}
	
	private void showErrorDialog(final String message, final String title)
	{
		JOptionPane.showMessageDialog(m_container, message, title, JOptionPane.ERROR_MESSAGE);
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
			loadPluginExternal();
		}
		else if (source == m_builtinPluginLoadButton)
		{
			loadPluginBuiltin();
		}
		else if (source == m_commandCreateButton)
		{
			createTextCommand();
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
	
	/* **** events **** */
	
	@SuppressWarnings("unchecked")
	private void loadUserCommands()
	{
		m_userCommands.clear();
		try
		{
			m_userCommands.addAll(new Gson().fromJson(Config.get("user_commands"), Set.class));
		}
		catch (JsonSyntaxException e)
		{
			Logger.error("(GUI) JSON syntax error while loading user commands:");
			Logger.printStackTrace(e);
			showErrorDialog("JSON syntax error in the user commands configuration. Check console for details.", "User commands JSON syntax error");
		}
	}
	
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
			showErrorDialog("This is not a file!", "Not a file");
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
		m_commandNameLabel.setText(command != null ? label : "(no such command)");
		m_commandUsageLabel.setText(command != null ? command.getUsage() : "(no such command)");
		m_commandDescriptionLabel.setText(command != null ? command.getDescription() : "(no such command)");
	}
	
	private void createTextCommand()
	{
		final String name = m_commandNameField.getText();
		if (m_client.getCommand(name) == null)
		{
			final String usage = '!' + name + ' ' + m_commandUsageField.getText().trim();
			final String description = m_commandDescriptionField.getText().trim();
			final String response = m_commandResponseField.getText();
			final UserCommand executor = new UserCommand(response);
			
			m_client.registerCommand(name, usage, executor).setDescription(description);
			
			showInfoDialog("Command \"" + name + "\" succesfully created.", "Command created");
			
			if (m_commandPersistentCheck.isSelected())
			{
				final List<String> commandData = new ArrayList<String>(4);
				commandData.add(name);
				commandData.add(usage);
				commandData.add(description);
				commandData.add(response);
				m_userCommands.add(commandData);
				
				Config.set("user_commands", new Gson().toJson(m_userCommands, Set.class));
				
				try
				{
					Config.saveConfig();
				}
				catch (IOException e)
				{
					Logger.error("(GUI) Couldn't save persistent user command:");
					Logger.printStackTrace(e);
					showErrorDialog("Couldn't save persistent user command.", "Couldn't save config");
				}
			}
			
			m_commandNameField.setText("");
			m_commandUsageField.setText("");
			m_commandDescriptionField.setText("");
			m_commandResponseField.setText("");
			m_commandPersistentCheck.setSelected(true);
		}
		else
		{
			showErrorDialog("The command \"" + name + "\" already exists.", "Command already registered");
		}
	}
	
	private void openCommandCreationHelp()
	{
		showInfoDialog("The convention for usage format is the following:\n"
				+ "- angle brackets for required arguments;\n"
				+ "- square brackets for optional arguments;\n"
				+ "but the GUI creator doesn't support optional arguments yet.\n"
				+ "Please only write arguments without the command in the usage field.\n\n"
				+ "To include arguments in the response, use \"$n\" or \"{$n}\", where n is the argument index.\n"
				+ "The zero-th argument is the sender's username.\n"
				+ "For instance, to create a hug command, put \"<target>\" in usage and \"$0 hugs $1.\" in response.\n\n"
				+ "By default the commands are created persistent, which means they will be created again on start.\n"
				+ "Non-persistent commands are lost once the bot stops or restarts.", "Command creation help");
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
		}
		catch (IOException e)
		{
			Logger.error("(GUI) Error while reloading config:");
			Logger.printStackTrace(e);
			showErrorDialog("Error while reloading config. Check console for details.", "Couldn't reload config");
		}
		loadUserCommands();
	}
	
	/* **** notifies **** */
	
	private void clientStarted()
	{
		m_isClientRunning = true;
		
		m_statusRunningLabel.setText("Running");
		m_startButton.setEnabled(false);
		m_stopButton.setEnabled(true);
		m_restartButton.setEnabled(true);
		m_commandCreateButton.setEnabled(true);
		m_sendMessageButton.setEnabled(true);
		
		m_doRestartClient = false;
		
		if (m_isFrameClosing)
		{
			stopClient();
		}
		else // if we are not closing, we can try to register user commands
		{
			String name;
			String usage;
			String description;
			String response;
			UserCommand executor;
			for (final List<String> command : m_userCommands)
			{
				name = command.get(0);
				usage = command.get(1);
				description = command.get(2);
				response = command.get(3);
				executor = new UserCommand(response);
				
				try
				{
					m_client.registerCommand(name, usage, executor).setDescription(description);
				}
				catch (IllegalArgumentException e)
				{
					Logger.warning("(GUI) User command \"" + name + "\" was already registered by something else.");
					showErrorDialog("User command \"" + name + "\" was already registered by something else.", "Command already registered");
				}
			}
		}
	}
	
	private void clientStopped()
	{
		m_statusRunningLabel.setText("Not running");
		m_startButton.setEnabled(true);
		m_stopButton.setEnabled(false);
		m_restartButton.setEnabled(false);
		m_commandCreateButton.setEnabled(false);
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
			showErrorDialog("Couldn't load the plugin. See console for details.", "Error while loading plugin");
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
				showErrorDialog("Error in client thread. See console for details.", "Client error");
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

	private class AlnumDocumentFilter extends DocumentFilter {

		private boolean isValid(final String string)
		{
			final char[] chars = string.toCharArray();
			final int stringLength = chars.length;
			char ch;
			for (int i = 0; i < stringLength; i++)
			{
				ch = chars[i];
				if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9') && ch != '_')
				{
					return false;
				}
			}
			return true;
		}
		
		@Override
		public void insertString(final FilterBypass fb, final int offset, final String string, final AttributeSet attr) throws BadLocationException
		{
			if (isValid(string))
			{
				super.insertString(fb, offset, string, attr);
			}
		}

		@Override
		public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException
		{
			if (isValid(text))
			{
				super.replace(fb, offset, length, text, attrs);
			}
		}
		
	}
	
	private class PluginFilenameFilter implements FilenameFilter {
	
		@Override
		public boolean accept(File dir, String name)
		{
			return name.endsWith(".jar");
		}
		
	}
	
}

package nuclearbot.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import nuclearbot.client.ChatListener;
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
public class ControlPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 606418561134403181L;

	private static final String GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/";
	
	// GUI components
	private final JTabbedPane m_body;
	private final JLabel m_statusControlLabel;
	private final JLabel m_statusLabelPluginName;
	private final JLabel m_pluginLabelName;
	private final FileDialog m_pluginExternalFileDialog;
	private final JComboBox<String> m_pluginBuiltinBuiltinList;
	private final LimitedStringList m_chatList;
	// activable components
	private final JPopupMenu m_textPopupMenu;
	private final JButton m_statusControlStartButton;
	private final JButton m_statusControlStopButton;
	private final JButton m_statusControlRestartButton;
	private final JTextField m_pluginExternalFilePath;
	private final JButton m_pluginExternalBrowseButton;
	private final JButton m_pluginExternalLoadButton;
	private final JButton m_pluginBuiltinLoadButton;

	private final JFrame m_container;
	
	private final PluginLoader m_pluginLoader;

	private boolean m_closing; // window is closing?
	private boolean m_doRestart; // restart after the client is stopped?

	private boolean m_clientRunning; // client is running?
	private ClientThread m_clientThread;
	private ChatClient m_client;

	public ControlPanel()
	{
		Document consoleDocument = new PlainDocument();
		Logger.info("(GUI) Linking GUI console to system console...");
		DocumentOutputStream.redirectSystemOut(consoleDocument);
		
		m_pluginLoader = new ImplPluginLoader();
		
		m_closing = false;
		m_doRestart = false;
		m_clientRunning = false;
		m_clientThread = null;
		m_client = null;
		
		Logger.info("(GUI) Constructing window...");		

		m_container = new JFrame("NuclearBot - Control Panel");
		m_container.setLocationRelativeTo(null);
		m_container.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		m_container.addWindowListener(new ControlPanelWindowListener());

		m_pluginExternalFileDialog = new FileDialog(m_container, "Choose a file", FileDialog.LOAD);
		m_pluginExternalFileDialog.setLocationRelativeTo(m_container);
		m_pluginExternalFileDialog.setDirectory(OSUtils.workingDir());
		m_pluginExternalFileDialog.setFile("*.jar");
		m_pluginExternalFileDialog.setFilenameFilter(new PluginFilenameFilter());
		
		m_textPopupMenu = new JPopupMenu();
		m_textPopupMenu.add(TransferHandler.getCopyAction());
		
		m_body = new JTabbedPane();
		{		
			JPanel tabStatus = new JPanel();
			{
				JPanel statusPluginName = new JPanel();
				{
					JLabel statusLabelPluginPrefix = new JLabel("Current plugin:");
					m_statusLabelPluginName = new JLabel();
			
					m_statusLabelPluginName.setFont(m_statusLabelPluginName.getFont().deriveFont(Font.PLAIN));
					m_statusLabelPluginName.setHorizontalAlignment(JLabel.CENTER);
					m_statusLabelPluginName.setComponentPopupMenu(m_textPopupMenu);
					
					statusPluginName.setLayout(new FlowLayout());
					statusPluginName.add(statusLabelPluginPrefix);
					statusPluginName.add(m_statusLabelPluginName);
				} // END controlPluginName
				
				JPanel statusControl = new JPanel();
				{
					m_statusControlLabel = new JLabel("Not running");
					m_statusControlStartButton = new JButton("Start");
					m_statusControlStopButton = new JButton("Stop");
					m_statusControlRestartButton = new JButton("Restart");
					
					m_statusControlStartButton.addActionListener(this);
					m_statusControlStopButton.addActionListener(this);
					m_statusControlStopButton.setEnabled(false);
					m_statusControlRestartButton.addActionListener(this);
					m_statusControlRestartButton.setEnabled(false);
					
					statusControl.setLayout(new FlowLayout());
					statusControl.add(m_statusControlLabel);
					statusControl.add(m_statusControlStartButton);
					statusControl.add(m_statusControlStopButton);
					statusControl.add(m_statusControlRestartButton);
				} // END controlStatus
				
				tabStatus.setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
				tabStatus.add(statusControl);
				tabStatus.add(statusPluginName);
			} // END tabControl
			
			JPanel tabPlugins = new JPanel();
			{
				JPanel pluginCurrent = new JPanel();
				{
					m_pluginLabelName = new JLabel();
					
					m_pluginLabelName.setHorizontalAlignment(JLabel.CENTER);
					m_pluginLabelName.setFont(m_pluginLabelName.getFont().deriveFont(Font.PLAIN));
					m_pluginLabelName.setComponentPopupMenu(m_textPopupMenu);
					
					pluginCurrent.setBorder(BorderFactory.createTitledBorder("Current plugin"));
					pluginCurrent.setLayout(new FlowLayout());
					pluginCurrent.add(m_pluginLabelName);
				} // END pluginCurrent
				JPanel pluginExternal = new JPanel();
				{
					m_pluginExternalFilePath = new JTextField(16);
					m_pluginExternalBrowseButton = new JButton("Browse...");
					m_pluginExternalLoadButton = new JButton("Load");
					
					m_pluginExternalFilePath.addActionListener(this);
					m_pluginExternalBrowseButton.addActionListener(this);
					m_pluginExternalLoadButton.addActionListener(this);
					
					pluginExternal.setBorder(BorderFactory.createTitledBorder("Change (external)"));
					pluginExternal.setLayout(new FlowLayout());
					pluginExternal.add(m_pluginExternalFilePath);
					pluginExternal.add(m_pluginExternalBrowseButton);
					pluginExternal.add(m_pluginExternalLoadButton);
				} // END pluginExternal
				JPanel pluginBuiltin = new JPanel();
				{
					m_pluginBuiltinBuiltinList = new JComboBox<String>(m_pluginLoader.getBuiltinPlugins());
					m_pluginBuiltinLoadButton = new JButton("Load");
					
					m_pluginBuiltinBuiltinList.setEditable(false);
					m_pluginBuiltinBuiltinList.setSelectedItem(DummyPlugin.class.getName());
					m_pluginBuiltinBuiltinList.setFont(m_pluginBuiltinBuiltinList.getFont().deriveFont(Font.PLAIN));
					m_pluginBuiltinLoadButton.addActionListener(this);
					
					pluginBuiltin.setBorder(BorderFactory.createTitledBorder("Change (built-in)"));
					pluginBuiltin.setLayout(new FlowLayout());
					pluginBuiltin.add(m_pluginBuiltinBuiltinList);
					pluginBuiltin.add(m_pluginBuiltinLoadButton);
				} // END pluginBuiltin
				
				tabPlugins.setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
				tabPlugins.add(pluginCurrent);
				tabPlugins.add(pluginExternal);
				tabPlugins.add(pluginBuiltin);
			} // END tabPlugins
			
			JPanel tabCommands = new JPanel();
			{
				
			} // END tabCommands
			
			JPanel tabChat = new JPanel();
			{
				JScrollPane chatListScrollPane = new JScrollPane();
				m_chatList = new LimitedStringList();
				
				chatListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				chatListScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				chatListScrollPane.setViewportView(m_chatList);
				
				m_chatList.setComponentPopupMenu(m_textPopupMenu);
				m_chatList.setFont(m_chatList.getFont().deriveFont(Font.PLAIN));
				
				tabChat.setBorder(BorderFactory.createLoweredBevelBorder());
				tabChat.setLayout(new BorderLayout());
				tabChat.add(m_chatList, BorderLayout.CENTER);
			} // END tabChat
			
			JScrollPane tabConsole = new JScrollPane();
			{
				JTextArea consoleTextArea = new JTextArea(consoleDocument);
				DefaultCaret caret = new DefaultCaret();
		
				caret.setVisible(true);
				caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
				consoleTextArea.setEditable(false);
				consoleTextArea.setCaret(caret);
				consoleTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
				consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
				consoleTextArea.setComponentPopupMenu(m_textPopupMenu);
				
				tabConsole.setBorder(BorderFactory.createLoweredBevelBorder());
				tabConsole.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				tabConsole.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				tabConsole.setViewportView(consoleTextArea);
			} // END tabConsole
			
			m_body.addTab("Status", tabStatus);
			m_body.addTab("Plugin", tabPlugins);
			m_body.addTab("Commands", tabCommands);
			m_body.addTab("Chat", tabChat);
			m_body.addTab("Console", tabConsole);
		} // END m_body
		
		JPanel footer = new JPanel();
		{
			JLabel footerLabelCopyrightAndLicense = new JLabel("Copyright \u00a9 2016 NuclearCoder. Licensed under AGPLv3.");
			JLabel footerLabelSourceLink = new JLabel("<html><a href=\"\">Source code here</a></html>");
			
			footerLabelCopyrightAndLicense.setFont(footerLabelCopyrightAndLicense.getFont().deriveFont(10F));
			
			footerLabelSourceLink.addMouseListener(new HyperlinkListener(GITHUB_URL));
			footerLabelSourceLink.setToolTipText(GITHUB_URL);
			footerLabelSourceLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
			footerLabelSourceLink.setFont(footerLabelSourceLink.getFont().deriveFont(10F));
			
			footer.setLayout(new BorderLayout());
			footer.add(footerLabelCopyrightAndLicense, BorderLayout.WEST);
			footer.add(footerLabelSourceLink, BorderLayout.EAST);
		}// END footer
		
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
	
	@Override
	public void actionPerformed(final ActionEvent event)
	{
		final Object source = event.getSource();
		if (source == m_statusControlStartButton)
		{
			startClient();
		}
		else if (source == m_statusControlStopButton)
		{
			m_doRestart = false;
			stopClient();
		}
		else if (source == m_statusControlRestartButton)
		{
			m_doRestart = true;
			stopClient();
		}
		else if (source == m_pluginExternalFilePath || source == m_pluginExternalBrowseButton)
		{
			choosePluginFile();
		}
		else if (source == m_pluginExternalLoadButton)
		{
			changePluginExternal();
		}
		else if (source == m_pluginBuiltinLoadButton)
		{
			changePluginBuiltin();
		}
	}
	
	/* **** events **** */
	
	private void startClient()
	{
		m_statusControlStartButton.setEnabled(false);
		
		Logger.info("(GUI) Starting client...");
		
		final String twitchUser = Config.get("twitch_user");
		final String twitchOauthKey = Config.get("twitch_oauth_key");
		final Plugin plugin = m_pluginLoader.getPlugin();
		
		m_client = new ImplChatClient(twitchUser, twitchOauthKey, plugin);
		m_client.registerChatListener(new ClientChatListener());
		m_clientThread = new ClientThread(m_client);
		m_clientThread.start();
	}
	
	private void stopClient()
	{
		Logger.info("(GUI) Stopping client...");
		
		m_statusControlStopButton.setEnabled(false);
		m_statusControlRestartButton.setEnabled(false);
		
		m_client.stop();
	}

	private void choosePluginFile()
	{
		Logger.info("(GUI) Opening plugin file dialog...");
		
		m_pluginExternalFileDialog.setVisible(true);
		final String filename = m_pluginExternalFileDialog.getFile();
		if (filename != null)
		{
			m_pluginExternalFilePath.setText(new File(m_pluginExternalFileDialog.getDirectory(), filename).getAbsolutePath());
		}
	}
	
	private void changePluginExternal()
	{
		final File file = new File(m_pluginExternalFilePath.getText());
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
		final String pluginClassName = m_pluginBuiltinBuiltinList.getItemAt(m_pluginBuiltinBuiltinList.getSelectedIndex());
		pluginChanged(m_pluginLoader.loadPlugin(pluginClassName) ? m_pluginLoader.getPlugin() : null);
	}
	
	/* **** notifies **** */
	
	private void clientStarted()
	{
		m_clientRunning = true;
		
		m_statusControlLabel.setText("Running");
		m_statusControlStartButton.setEnabled(false);
		m_statusControlStopButton.setEnabled(true);
		m_statusControlRestartButton.setEnabled(true);
		
		m_doRestart = false;
		
		if (m_closing)
		{
			stopClient();
		}
	}
	
	private void clientStopped()
	{
		m_statusControlLabel.setText("Not running");
		m_statusControlStartButton.setEnabled(true);
		m_statusControlStopButton.setEnabled(false);
		m_statusControlRestartButton.setEnabled(false);
		
		if (m_doRestart)
		{
			startClient();
		}
	}
	
	private void clientMessage(final String username, final String message)
	{
		m_chatList.add("<html><strong>" + username + " :</strong> " + HTML.escapeText(message) + "</html>");
	}
	
	private void frameClosing()
	{
		m_closing = true;
		m_pluginExternalFileDialog.dispose();
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
			
			m_pluginLabelName.setText(pluginLabelText);
			m_pluginLabelName.setToolTipText(pluginClassName);
			m_statusLabelPluginName.setText(pluginLabelText);
			m_statusLabelPluginName.setToolTipText(pluginClassName);
			
			if (m_clientRunning)
			{ // ask to restart if the client is already running
				final int restart = JOptionPane.showConfirmDialog(m_container, "The changes will be not effective until a restart.\nRestart now?", "Restart?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (restart == JOptionPane.YES_OPTION)
				{
					m_body.setSelectedIndex(0); // switch back to the Status tab
					m_doRestart = true;
					stopClient();
				}
			}
		}
		else
		{
			JOptionPane.showMessageDialog(m_container, "Couldn't load the plugin. See logs for details.", "Error while loading plugin", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/* **** classes **** */

	private class ClientStartedRunnable implements Runnable {
	
		@Override
		public void run()
		{
			clientStarted();
		}
		
	};
	
	private class ClientStoppedRunnable implements Runnable {
		
		@Override
		public void run()
		{
			clientStopped();
		}
		
	};
	
	private class ClientMessageRunnable implements Runnable {
		
		private final String m_username;
		private final String m_message;
		
		public ClientMessageRunnable(final String username, final String message)
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
	
	private class ClientChatListener implements ChatListener {
		
		@Override
		public void onConnected(final ChatClient client)
		{
			SwingUtilities.invokeLater(new ClientStartedRunnable());
		}

		@Override
		public void onDisconnected(final ChatClient client)
		{
			SwingUtilities.invokeLater(new ClientStoppedRunnable());
		}
		
		@Override
		public void onChat(final ChatClient client, final String username, final String message)
		{
			SwingUtilities.invokeLater(new ClientMessageRunnable(username, message));
		}
		
	}
	
	private class ControlPanelWindowListener extends WindowAdapter {
		
		@Override
		public void windowClosing(final WindowEvent event)
		{
			frameClosing();
		}
		
	}
	
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
				Logger.error("(GUI) Desktop class not supported.");
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

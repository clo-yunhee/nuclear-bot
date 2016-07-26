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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.client.ChatClient;
import nuclearbot.client.ChatListener;
import nuclearbot.client.ImplChatClient;
import nuclearbot.client.StateListener;
import nuclearbot.plugin.ImplPluginLoader;
import nuclearbot.plugin.JavaPlugin;
import nuclearbot.plugin.PluginLoader;
import nuclearbot.utils.Config;
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
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)<br>
 * <br>
 * Main window for the GUI.
 */
public class ControlPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private static final String GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/";
	
	// GUI components
	private final JLabel m_pluginLabelName;
	private final FileDialog m_pluginExternalFileDialog;
	private final JComboBox<String> m_pluginBuiltinBuiltinList;
	// activable components
	private final JButton m_controlStartButton;
	private final JButton m_controlStopButton;
	private final JButton m_controlRestartButton;
	private final JTextField m_pluginExternalFilePath;
	private final JButton m_pluginExternalBrowseButton;
	private final JButton m_pluginExternalLoadButton;
	private final JButton m_pluginBuiltinLoadButton;
	
	private final JFrame m_container;
	
	private final PluginLoader m_pluginLoader;

	private boolean m_doRestart;

	private boolean m_clientRunning;
	private ClientThread m_clientThread;
	private ChatClient m_client;
	
	public ControlPanel()
	{
		m_pluginLoader = new ImplPluginLoader();
		
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
		
		final JTabbedPane body = new JTabbedPane();
		{
			final JPanel tabControl = new JPanel();
			{
				m_controlStartButton = new JButton("Start");
				m_controlStopButton = new JButton("Stop");
				m_controlRestartButton = new JButton("Restart");
				
				m_controlStartButton.addActionListener(this);
				
				m_controlStopButton.addActionListener(this);
				m_controlStopButton.setEnabled(false);
				
				m_controlRestartButton.addActionListener(this);
				m_controlRestartButton.setEnabled(false);
				
				tabControl.setLayout(new FlowLayout());
				tabControl.add(m_controlStartButton);
				tabControl.add(m_controlStopButton);
				tabControl.add(m_controlRestartButton);
			}
			
			final JScrollPane tabConsole = new JScrollPane();
			{
				final JTextArea consoleTextArea = new JTextArea();
				final DefaultCaret caret = new DefaultCaret();
	
				caret.setVisible(true);
				caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	
				tabConsole.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				tabConsole.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
				
				consoleTextArea.setEditable(false);
				consoleTextArea.setCaret(caret);
				consoleTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
				consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
				
				Logger.info("(GUI) Linking GUI console to system console...");
				Logger.redirectSystemOut(consoleTextArea.getDocument());
				
				tabConsole.setViewportView(consoleTextArea);
			}
			
			final JPanel tabPlugins = new JPanel();
			{
				m_pluginLabelName = new JLabel();
				final JPanel pluginExternal = new JPanel();
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
				}
				final JPanel pluginBuiltin = new JPanel();
				{
					m_pluginBuiltinBuiltinList = new JComboBox<String>(m_pluginLoader.getBuiltinPlugins());
					m_pluginBuiltinLoadButton = new JButton("Load");
					
					m_pluginBuiltinBuiltinList.setEditable(false);
					m_pluginBuiltinBuiltinList.setSelectedItem(DummyPlugin.class.getName());
					m_pluginBuiltinLoadButton.addActionListener(this);
					
					pluginBuiltin.setBorder(BorderFactory.createTitledBorder("Change (built-in)"));
					pluginBuiltin.setLayout(new FlowLayout());
					pluginBuiltin.add(m_pluginBuiltinBuiltinList);
					pluginBuiltin.add(m_pluginBuiltinLoadButton);
				}
				
				m_pluginLabelName.setHorizontalAlignment(JLabel.CENTER);
				
				tabPlugins.setLayout(new GridLayout(0, 1));
				tabPlugins.add(m_pluginLabelName);
				tabPlugins.add(pluginExternal);
				tabPlugins.add(pluginBuiltin);
			}
			
			body.addTab("Controls", tabControl);
			body.addTab("Plugins", tabPlugins);
			body.addTab("Console", tabConsole);
		}
		
		final JPanel footer = new JPanel();
		{
			final JLabel footerLabelCopyrightAndLicense = new JLabel("Copyright \u00a9 2016 NuclearCoder. Licensed under AGPLv3.");
			final JLabel footerLabelSourceLink = new JLabel("<html><a href=\"\">Source code here</a></html>");
			
			footerLabelCopyrightAndLicense.setFont(footerLabelCopyrightAndLicense.getFont().deriveFont(10F));
			
			footerLabelSourceLink.addMouseListener(new ControlPanelHyperlinkListener(GITHUB_URL));
			footerLabelSourceLink.setToolTipText(GITHUB_URL);
			footerLabelSourceLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
			footerLabelSourceLink.setFont(footerLabelSourceLink.getFont().deriveFont(10F));
			
			footer.setLayout(new BorderLayout());
			footer.add(footerLabelCopyrightAndLicense, BorderLayout.WEST);
			footer.add(footerLabelSourceLink, BorderLayout.EAST);
		}
		
		setLayout(new BorderLayout(5, 5));
		add(body, BorderLayout.CENTER);
		add(footer, BorderLayout.SOUTH);
		
		m_container.setContentPane(this);
		m_container.pack();
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
		startClient();
	}
	
	/* **** events **** */
	
	@Override
	public void actionPerformed(final ActionEvent event)
	{
		final Object source = event.getSource();
		
		if (source == m_controlStartButton)
		{
			startClient();
		}
		else if (source == m_controlStopButton)
		{
			m_doRestart = false;
			stopClient();
		}
		else if (source == m_controlRestartButton)
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
	
	private void startClient()
	{
		m_controlStartButton.setEnabled(false);
		
		Logger.info("(GUI) Starting client...");
		
		final String twitchUser = Config.get("twitch_user");
		final String twitchOauthKey = Config.get("twitch_oauth_key");
		
		final ChatListener listener = m_pluginLoader.getPlugin().init();
		
		m_client = new ImplChatClient(twitchUser, twitchOauthKey, listener);
		m_client.addStateListener(new ClientStateListener());
		m_clientThread = new ClientThread(m_client);
		m_clientThread.start();
	}
	
	private void stopClient()
	{
		Logger.info("(GUI) Stopping client...");
		
		m_controlStopButton.setEnabled(false);
		m_controlRestartButton.setEnabled(false);
		
		m_client.stop();
	}

	private void choosePluginFile()
	{
		Logger.info("(GUI) Opening plugin file dialog...");
		
		m_pluginExternalFileDialog.setVisible(true);
		final String filename = m_pluginExternalFileDialog.getFile();
		if (filename != null)
		{
			m_pluginExternalFilePath.setText(new File(m_pluginExternalFileDialog.getFile()).getAbsolutePath());
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
	
	void clientStarted()
	{
		m_clientRunning = true;
		
		m_controlStartButton.setEnabled(false);
		m_controlStopButton.setEnabled(true);
		m_controlRestartButton.setEnabled(true);
		
		m_doRestart = false;
	}
	
	void clientStopped()
	{
		m_clientRunning = false;
		
		m_controlStartButton.setEnabled(true);
		m_controlStopButton.setEnabled(false);
		m_controlRestartButton.setEnabled(false);
		
		if (m_doRestart)
		{
			startClient();
		}
	}
	
	void frameClosing()
	{
		stopClient();
		m_pluginExternalFileDialog.dispose();
		m_container.dispose();
	}
	
	void pluginChanged(final JavaPlugin plugin)
	{
		if (plugin != null)
		{			
			// italic name if built-in plugin
			m_pluginLabelName.setText(plugin.getName());
			m_pluginLabelName.setToolTipText(plugin.getClassName());
			final Font fontLabelPlugin = m_pluginLabelName.getFont().deriveFont(plugin.isBuiltin() ? Font.ITALIC : Font.PLAIN);
			m_pluginLabelName.setFont(fontLabelPlugin);

			if (m_clientRunning)
			{
				final int restart = JOptionPane.showConfirmDialog(m_container, "The changes will be not effective until a restart.\nRestart now?", "Restart?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (restart == JOptionPane.YES_OPTION)
				{
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
	
	private class ClientStateListener implements StateListener {

		private class ClientConnectedRunnable implements Runnable {
		
			@Override
			public void run()
			{
				clientStarted();
			}
			
		};
		
		private class ClientDisconnectedRunnable implements Runnable {
			
			@Override
			public void run()
			{
				clientStopped();
			}
			
		};
		
		@Override
		public void onConnected(final ChatClient client)
		{
			SwingUtilities.invokeLater(new ClientConnectedRunnable());
		}

		@Override
		public void onDisconnected(final ChatClient client)
		{
			SwingUtilities.invokeLater(new ClientDisconnectedRunnable());
		}
		
	}
	
	private class ControlPanelWindowListener extends WindowAdapter {
		
		@Override
		public void windowClosing(final WindowEvent event)
		{
			frameClosing();
		}
		
	}
	
	private class ControlPanelHyperlinkListener extends MouseAdapter {
		
		private URI m_uri;
		
		public ControlPanelHyperlinkListener(final String url)
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

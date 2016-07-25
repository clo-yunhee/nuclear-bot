package nuclearbot.gui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import nuclearbot.client.ChatClient;
import nuclearbot.client.ChatListener;
import nuclearbot.client.ImplChatClient;
import nuclearbot.client.StateListener;
import nuclearbot.plugin.ImplPluginLoader;
import nuclearbot.plugin.Plugin;
import nuclearbot.plugin.PluginLoader;
import nuclearbot.utils.Config;
import nuclearbot.utils.Logger;

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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Main window for the GUI.
 */
public class ControlPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private static final String GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/";
	
	private final DialogChangePluginExternal m_dialogChangePluginExternal;
	private final DialogChangePluginBuiltin m_dialogChangePluginBuiltin;
	
	private final JFrame m_frame;
	
	private final JPanel m_paneTop;
	private final JPanel m_paneCenter;
	private final JPanel m_paneSide;
	private final JPanel m_paneSideChangePlugin;
	private final JPanel m_paneBot;
	
	private final JButton m_buttonStart;
	private final JButton m_buttonStop;
	private final JButton m_buttonRestart;

	private final JTextArea m_textAreaConsole;
	
	private final JLabel m_labelPluginFilename;
	private final JButton m_buttonChangePluginExternal;
	private final JButton m_buttonChangePluginBuiltin;
	
	private final JLabel m_labelCopyrightAndLicense;
	private final JLabel m_labelLinkSource;
	
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

		final ActionListener action = new ActionButtons();

		m_frame = new JFrame("NuclearBot - Control Panel");
		m_frame.setLocationRelativeTo(null);
		m_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		m_frame.addWindowListener(new ControlPanelWindowListener());
		
		m_dialogChangePluginExternal = new DialogChangePluginExternal(this, m_pluginLoader);
		m_dialogChangePluginBuiltin = new DialogChangePluginBuiltin(this, m_pluginLoader);
		
		m_paneTop = new JPanel();
		{
			m_buttonStart = new JButton("Start");
			m_buttonStop = new JButton("Stop");
			m_buttonRestart = new JButton("Restart");
			
			m_buttonStart.addActionListener(action);
			
			m_buttonStop.addActionListener(action);
			m_buttonStop.setEnabled(false);
			
			m_buttonRestart.addActionListener(action);
			m_buttonRestart.setEnabled(false);
			
			m_paneTop.setLayout(new FlowLayout());
			m_paneTop.add(m_buttonStart);
			m_paneTop.add(m_buttonStop);
			m_paneTop.add(m_buttonRestart);
		}
		
		m_paneCenter = new JPanel();
		{
			m_textAreaConsole = new JTextArea();
			final DefaultCaret caret = (DefaultCaret) m_textAreaConsole.getCaret();
			final JScrollPane textAreaConsoleScrollPane = new JScrollPane(m_textAreaConsole);
			
			m_textAreaConsole.setEditable(false);
			m_textAreaConsole.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			m_textAreaConsole.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			Logger.redirectSystemOut(m_textAreaConsole.getDocument());
		
			caret.setVisible(true);
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			
			textAreaConsoleScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			textAreaConsoleScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			
			m_paneCenter.setLayout(new BorderLayout(5, 5));
			m_paneCenter.setBorder(BorderFactory.createLoweredBevelBorder());
			m_paneCenter.add(textAreaConsoleScrollPane);
		}
		
		m_paneSide = new JPanel();
		{
			m_labelPluginFilename = new JLabel(m_pluginLoader.getPlugin().getClass().getName());
			m_paneSideChangePlugin = new JPanel();
			{
				m_buttonChangePluginExternal = new JButton("External");
				m_buttonChangePluginBuiltin = new JButton("Built-in");
				
				m_buttonChangePluginExternal.addActionListener(action);
				m_buttonChangePluginBuiltin.addActionListener(action);
				
				m_paneSideChangePlugin.setLayout(new FlowLayout());
				m_paneSideChangePlugin.add(m_buttonChangePluginExternal);
				m_paneSideChangePlugin.add(m_buttonChangePluginBuiltin);
			}
			
			m_paneSide.setLayout(new GridLayout(0, 1));
			m_paneSide.add(m_labelPluginFilename);
			m_paneSide.add(m_paneSideChangePlugin);
		}
		
		m_paneBot = new JPanel();
		{
			m_labelCopyrightAndLicense = new JLabel("Copyright \u00a9 2016 NuclearCoder. Licensed under AGPLv3.");
			m_labelLinkSource = new JLabel("<html><a href=\"\">Source code here</a></html>");
			
			m_labelCopyrightAndLicense.setFont(m_labelCopyrightAndLicense.getFont().deriveFont(10F));
			
			m_labelLinkSource.addMouseListener(new ControlPanelHyperlinkListener(GITHUB_URL));
			m_labelLinkSource.setToolTipText(GITHUB_URL);
			m_labelLinkSource.setCursor(new Cursor(Cursor.HAND_CURSOR));
			m_labelLinkSource.setFont(m_labelLinkSource.getFont().deriveFont(10F));
			
			m_paneBot.setLayout(new BorderLayout());
			m_paneBot.add(m_labelCopyrightAndLicense, BorderLayout.WEST);
			m_paneBot.add(m_labelLinkSource, BorderLayout.EAST);
		}
		
		setLayout(new BorderLayout(5, 5));
		add(m_paneTop, BorderLayout.NORTH);
		add(m_paneCenter, BorderLayout.CENTER);
		add(m_paneSide, BorderLayout.EAST);
		add(m_paneBot, BorderLayout.SOUTH);
		
		m_frame.setContentPane(this);
		m_frame.pack();
	}
	
	public PluginLoader getPluginLoader()
	{
		return m_pluginLoader;
	}
	
	public JFrame getFrame()
	{
		return m_frame;
	}
	
	public void open()
	{
		m_frame.setVisible(true);
		startClient();
	}
	
	/* **** events **** */
	
	private void startClient()
	{
		m_buttonStart.setEnabled(false);
		
		Logger.info("(GUI) Starting client...");
		
		final String twitchUser = Config.get("twitch_user");
		final String twitchOauthKey = Config.get("twitch_oauth_key");
		
		final ChatListener listener = m_pluginLoader.getPlugin().init();
		
		m_client = new ImplChatClient(twitchUser, twitchOauthKey, listener);
		m_client.addStateListener(new ControlPanelStateListener());
		m_clientThread = new ClientThread(m_client);
		m_clientThread.start();
	}
	
	private void stopClient()
	{
		m_buttonStop.setEnabled(false);
		m_buttonRestart.setEnabled(false);

		Logger.info("(GUI) Stopping client...");
		
		m_client.stop();
	}
	
	private void changePluginExternal()
	{
		Logger.info("(GUI) Change plugin external dialog opened.");
		
		m_dialogChangePluginExternal.open();
	}
	
	private void changePluginBuiltin()
	{
		Logger.info("(GUI) Change plugin built-in dialog opened.");
		
		m_dialogChangePluginBuiltin.open();
	}

	/* **** notifies **** */
	
	void clientStarted()
	{
		m_clientRunning = true;
		
		m_buttonStart.setEnabled(false);
		m_buttonStop.setEnabled(true);
		m_buttonRestart.setEnabled(true);
		
		m_doRestart = false;
	}
	
	void clientStopped()
	{
		m_clientRunning = false;
		
		m_buttonStart.setEnabled(true);
		m_buttonStop.setEnabled(false);
		m_buttonRestart.setEnabled(false);
		
		if (m_doRestart)
		{
			startClient();
		}
	}
	
	void frameClosing()
	{
		stopClient();
		m_dialogChangePluginExternal.dispose();
		m_dialogChangePluginBuiltin.dispose();
		m_frame.dispose();
	}
	
	void pluginChanged(final Plugin plugin)
	{
		if (plugin != null)
		{
			m_labelPluginFilename.setText(plugin.getClass().getName());
			
			if (m_clientRunning)
			{
				final int restart = JOptionPane.showConfirmDialog(m_frame, "The changes will be not effective until a restart.\nRestart now?", "Restart?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (restart == JOptionPane.YES_OPTION)
				{
					m_doRestart = true;
					stopClient();
				}
			}
		}
		else
		{
			JOptionPane.showMessageDialog(m_frame, "Couldn't load the plugin. See logs for details.", "Error while loading plugin", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/* **** classes **** */
	
	private class ActionButtons implements ActionListener {
		
		@Override
		public void actionPerformed(final ActionEvent event)
		{
			final Object source = event.getSource();
			
			if (source == m_buttonStart)
			{
				startClient();
			}
			else if (source == m_buttonStop)
			{
				m_doRestart = false;
				stopClient();
			}
			else if (source == m_buttonRestart)
			{
				m_doRestart = true;
				stopClient();
			}
			else if (source == m_buttonChangePluginExternal)
			{
				changePluginExternal();
			}
			else if (source == m_buttonChangePluginBuiltin)
			{
				changePluginBuiltin();
			}
		}
	}
	
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
	
	private class ControlPanelStateListener implements StateListener {

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
	
}

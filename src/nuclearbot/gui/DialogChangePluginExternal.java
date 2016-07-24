package nuclearbot.gui;

import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import nuclearbot.plugin.PluginLoader;
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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Dialog to load a plugin file.
 */
public class DialogChangePluginExternal extends JDialog implements ActionListener, PropertyChangeListener, DocumentListener {

	private static final long serialVersionUID = 1L;
	
	private final ControlPanel m_controlPanel;
	
	private final PluginLoader m_pluginLoader;
	
	private final JOptionPane m_optionPane;
	private final JPanel m_pane;
	
	private final JTextField m_textFieldFile;
	private final JButton m_buttonBrowse;
	
	private File m_lastFile;
	
	public DialogChangePluginExternal(final ControlPanel controlPanel, final PluginLoader pluginLoader)
	{
		super(controlPanel.getFrame(), "Change plugin (external)", true);
		
		m_controlPanel = controlPanel;
		m_pluginLoader = pluginLoader;
		
		m_pane = new JPanel();
		{
			m_textFieldFile = new JTextField(16);
			m_buttonBrowse = new JButton("Browse");

			m_textFieldFile.addActionListener(this);
			m_textFieldFile.getDocument().addDocumentListener(this);
			m_buttonBrowse.addActionListener(this);
			
			m_pane.setLayout(new FlowLayout());
			m_pane.add(m_textFieldFile);
			m_pane.add(m_buttonBrowse);
		}
		
		m_optionPane = new JOptionPane(m_pane, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		m_optionPane.addPropertyChangeListener(this);
		
		m_lastFile = null;
		
		setLocationRelativeTo(controlPanel.getFrame());
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowClosingListener());
		addComponentListener(new ComponentShownListener());
		setContentPane(m_optionPane);
		setResizable(false);
		pack();
	}
	
	public void open()
	{
		setVisible(true);
	}
	
	public void close()
	{
		setVisible(false);
	}
	
	private void chooseFile()
	{
		final FileDialog fd = new FileDialog(this, "Choose a file", FileDialog.LOAD);
		fd.setLocationRelativeTo(this);
		fd.setDirectory(m_lastFile != null ? m_lastFile.getParent() : OSUtils.workingDir());
		fd.setFile("*.jar");
		fd.setFilenameFilter((dir, name) -> name.endsWith(".jar"));
		fd.setVisible(true);
		final String filename = fd.getFile();
		if (filename != null)
		{
			m_lastFile = new File(fd.getDirectory(), filename);
			m_textFieldFile.setText(m_lastFile.getPath());
		}
		fd.dispose();
	}

	@Override
	public void propertyChange(PropertyChangeEvent event)
	{
		final Object source = event.getSource();
		final String property = event.getPropertyName();
		
		if (isVisible() && source == m_optionPane && (property == JOptionPane.INPUT_VALUE_PROPERTY || property == JOptionPane.VALUE_PROPERTY))
		{
			final Object value = m_optionPane.getValue();
			
			if (value != JOptionPane.UNINITIALIZED_VALUE)
			{
				// reset option
				m_optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
				
				if (value.equals(JOptionPane.OK_OPTION))
				{
					// load plugin
					final File file = new File(m_textFieldFile.getText());
					
					if (file.isFile())
					{
						m_controlPanel.pluginChanged(m_pluginLoader.loadPlugin(file) ? m_pluginLoader.getPlugin() : null);
						close();
					}
					else
					{
						Logger.error("(GUI) Provided path \"" + file.getAbsolutePath() + "\" that wasn't a file.");
						JOptionPane.showMessageDialog(this, "This is not a file!", "Not a file", JOptionPane.ERROR_MESSAGE);
					}
				}
				else
				{
					close();
				}
			}
		}
	}
	
	@Override
	public void insertUpdate(DocumentEvent event)
	{
		m_lastFile = new File(m_textFieldFile.getText());
	}
	
	@Override
	public void removeUpdate(DocumentEvent event)
	{
		m_lastFile = new File(m_textFieldFile.getText());
	}
	
	@Override
	public void changedUpdate(DocumentEvent event)
	{
		m_lastFile = new File(m_textFieldFile.getText());
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		final Object source = event.getSource();
	
		if (source == m_textFieldFile || source == m_buttonBrowse)
		{
			chooseFile();
		}
	}
	
	public class WindowClosingListener extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent event)
		{
			m_optionPane.setValue(JOptionPane.CLOSED_OPTION);
		}
		
	}
	
	public class ComponentShownListener extends ComponentAdapter {

		@Override
		public void componentShown(ComponentEvent event)
		{
			m_buttonBrowse.requestFocusInWindow();
		}
		
	}
	
}

package nuclearbot.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.plugin.PluginLoader;

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
 * Dialog to load a built-in plugin.
 */
public class DialogChangePluginBuiltin extends JDialog implements ActionListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private final ControlPanel m_controlPanel;
	
	private final PluginLoader m_pluginLoader;
	
	private final JOptionPane m_optionPane;
	private final JComboBox<String> m_comboBoxBuiltins;
	
	public DialogChangePluginBuiltin(final ControlPanel controlPanel, final PluginLoader pluginLoader)
	{
		super(controlPanel.getFrame(), "Change plugin (built-in)", true);
		
		m_controlPanel = controlPanel;
		m_pluginLoader = pluginLoader;
		
		m_comboBoxBuiltins = new JComboBox<String>(pluginLoader.getBuiltinPlugins());
		m_comboBoxBuiltins.setEditable(false);
		m_comboBoxBuiltins.setSelectedItem(DummyPlugin.class.getName());
		
		m_optionPane = new JOptionPane(m_comboBoxBuiltins, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		m_optionPane.addPropertyChangeListener(this);
		
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
					final String pluginClassName = m_comboBoxBuiltins.getItemAt(m_comboBoxBuiltins.getSelectedIndex());
					
					m_controlPanel.pluginChanged(m_pluginLoader.loadPlugin(pluginClassName) ? m_pluginLoader.getPlugin() : null);
				}
				close();
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent event)
	{
		final Object source = event.getSource();
	
		if (source == m_comboBoxBuiltins)
		{
			m_optionPane.setValue(JOptionPane.OK_OPTION);
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
			m_comboBoxBuiltins.requestFocusInWindow();
		}
		
	}
	
}

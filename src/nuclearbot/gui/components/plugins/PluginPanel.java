package nuclearbot.gui.components.plugins;

import nuclearbot.builtin.DummyPlugin;
import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.gui.utils.VerticalLayout;
import nuclearbot.plugin.PluginLoader;
import nuclearbot.util.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
 * The GUI panel for loading plugins.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class PluginPanel extends JScrollPane {

    private final NuclearBotGUI m_gui;

    private final PluginLoader m_pluginLoader;

    private final PluginFileDialog m_fileDialog;

    private final JLabel m_pluginLabel;
    private final JTextField m_pathTextField;
    private final JComboBox<String> m_builtinCombo;

    public PluginPanel(final NuclearBotGUI gui)
    {
        m_gui = gui;
        m_pluginLoader = gui.getPluginLoader();

        m_fileDialog = new PluginFileDialog(gui.getFrame());

        final JPanel container = new JPanel(new VerticalLayout());

        final JPanel loadedPluginPanel = new JPanel(new FlowLayout());
        {
            m_pluginLabel = new JLabel();

            m_pluginLabel.setHorizontalAlignment(SwingConstants.CENTER);
            m_pluginLabel.setFont(m_pluginLabel.getFont().deriveFont(Font.PLAIN));
            m_pluginLabel.setComponentPopupMenu(gui.getTextPopupMenu());

            loadedPluginPanel.setBorder(BorderFactory.createTitledBorder("Current plugin"));
            loadedPluginPanel.add(m_pluginLabel);
        }

        final JPanel externalPanel = new JPanel(new FlowLayout());
        {
            m_pathTextField = new JTextField(16);
            final JButton browseButton = new JButton("Browse...");
            final JButton externalButton = new JButton("Load");

            m_pathTextField.setComponentPopupMenu(gui.getTextPopupMenu());

            m_pathTextField.addActionListener(e -> chooseFile());
            browseButton.addActionListener(e -> chooseFile());
            externalButton.addActionListener(e -> loadExternal());

            externalPanel.setBorder(BorderFactory.createTitledBorder("Load an external plugin"));
            externalPanel.add(m_pathTextField);
            externalPanel.add(browseButton);
            externalPanel.add(externalButton);
        }

        final JPanel builtinPanel = new JPanel(new FlowLayout());
        {
            m_builtinCombo = new JComboBox<>(m_pluginLoader.getBuiltinPlugins());
            final JButton builtinButton = new JButton("Load");

            m_builtinCombo.setEditable(false);
            m_builtinCombo.setSelectedItem(DummyPlugin.class.getName());
            m_builtinCombo.setFont(m_builtinCombo.getFont().deriveFont(Font.PLAIN));

            builtinButton.addActionListener(e -> loadBuiltin());

            builtinPanel.setBorder(BorderFactory.createTitledBorder("Load a built-in plugin"));
            builtinPanel.add(m_builtinCombo);
            builtinPanel.add(builtinButton);
        }

        container.add(loadedPluginPanel);
        container.add(externalPanel);
        container.add(builtinPanel);

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setViewportView(container);
    }

    public void setPluginText(final String text, final String tooltipText)
    {
        m_pluginLabel.setText(text);
        m_pluginLabel.setToolTipText(tooltipText);
    }

    public void dispose()
    {
        m_fileDialog.dispose();
    }

    private void chooseFile()
    {
        Logger.info("(GUI) Opening plugin file dialog...");

        m_fileDialog.setVisible(true);
        final String filename = m_fileDialog.getFile();
        if (filename != null)
        {
            m_pathTextField.setText(new File(m_fileDialog.getDirectory(), filename).getAbsolutePath());
        }
    }

    private void loadExternal()
    {
        final File file = new File(m_pathTextField.getText());
        if (file.isFile())
        {
            m_gui.pluginChanged(m_pluginLoader.loadPlugin(file) ? m_pluginLoader.getPlugin() : null);
        }
        else
        {
            Logger.error("(GUI) Provided path \"" + file.getAbsolutePath() + "\" that wasn't a file.");
            m_gui.getDialogs().error("This is not a file!", "Not a file");
        }
    }

    private void loadBuiltin()
    {
        final String pluginClassName = (String) m_builtinCombo.getSelectedItem();
        m_gui.pluginChanged(m_pluginLoader.loadPlugin(pluginClassName) ? m_pluginLoader.getPlugin() : null);
    }

}

package nuclearbot.gui.components;

import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.gui.plugin.configuration.HasConfigPanel;
import nuclearbot.gui.plugin.configuration.PluginConfigPanel;
import nuclearbot.gui.utils.VerticalLayout;
import nuclearbot.plugin.Plugin;
import nuclearbot.util.Config;
import nuclearbot.util.Logger;

import javax.swing.*;
import java.awt.*;
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
 * GUI panel for the configuration tab.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ConfigPanel extends JScrollPane {

    private final NuclearBotGUI m_gui;

    private final JPanel m_configPanel;

    private PluginConfigPanel m_pluginPanel;

    public ConfigPanel(final NuclearBotGUI gui)
    {
        m_gui = gui;

        m_configPanel = new JPanel(new VerticalLayout());
        {
            final PluginConfigPanel clientPanel = new PluginConfigPanel("twitch");

            clientPanel.addTextField("Twitch user name", "user", "");
            clientPanel.addPasswordField("Twitch OAuth token", "oauth_key", "");

            m_pluginPanel = null;

            final JPanel buttonsPanel = new JPanel(new FlowLayout());
            {
                final JButton resetButton = new JButton("Reset fields");
                final JButton saveButton = new JButton("Save config");
                final JButton reloadButton = new JButton("Reload config");

                resetButton.addActionListener(e -> resetFields());
                saveButton.addActionListener(e -> saveConfig());
                reloadButton.addActionListener(e -> reloadConfig());

                buttonsPanel.add(resetButton);
                buttonsPanel.add(saveButton);
                buttonsPanel.add(reloadButton);
            }

            m_configPanel.add(buttonsPanel);
            m_configPanel.add(clientPanel);
        } // configPanel

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        setViewportView(m_configPanel);

        resetFields();
    }

    public void setPluginPanel(final Plugin handle)
    {
        if (m_pluginPanel != null)
        {
            m_configPanel.remove(m_pluginPanel);
        }
        if (handle instanceof HasConfigPanel)
        {
            try
            {
                m_pluginPanel = ((HasConfigPanel) handle).getConfigPanel();
                m_pluginPanel.resetFields();
                m_configPanel.add(m_pluginPanel);
            }
            catch (Exception e)
            {
                Logger.error("(GUI) Exception caught while changing plugin configuration panel:");
                Logger.printStackTrace(e);
                m_gui.getDialogs().error("Exception caught while changing plugin configuration panel. Check console for details.", "Exception while updating config panel");
            }
        }
        else
        {
            m_pluginPanel = null;
        }

    }

    private void setFields()
    {
        final Component[] components;
        synchronized (m_configPanel.getTreeLock())
        {
            components = m_configPanel.getComponents();
        }

        for (final Component component : components)
        {
            if (component instanceof PluginConfigPanel)
            {
                ((PluginConfigPanel) component).saveFields();
            }
        }
    }

    private void resetFields()
    {
        final Component[] components;
        synchronized (m_configPanel.getTreeLock())
        {
            components = m_configPanel.getComponents();
        }

        for (final Component component : components)
        {
            if (component instanceof PluginConfigPanel)
            {
                ((PluginConfigPanel) component).resetFields();
            }
        }
    }

    private void saveConfig()
    {
        try
        {
            setFields();
            Config.saveConfig();
            m_gui.getDialogs().info("Config saved successfully.", "Config saved");
        }
        catch (IOException e)
        {
            Logger.error("(GUI) Exception while saving config:");
            Logger.printStackTrace(e);
            m_gui.selectTab(NuclearBotGUI.TAB_CONSOLE);
            m_gui.getDialogs().error("Exception while saving config. Check console for details.", "Couldn't save config");
        }
    }

    private void reloadConfig()
    {
        try
        {
            Config.reloadConfig();
            m_gui.getDialogs().info("Config reloaded successfully.", "Config reloaded");
        }
        catch (IOException e)
        {
            Logger.error("(GUI) Exception while reloading config:");
            Logger.printStackTrace(e);
            m_gui.selectTab(NuclearBotGUI.TAB_CONSOLE);
            m_gui.getDialogs().error("Exception while reloading config. Check console for details.", "Couldn't reload config");
        }
    }

}

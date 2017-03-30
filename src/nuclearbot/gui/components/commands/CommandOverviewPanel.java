package nuclearbot.gui.components.commands;

import nuclearbot.client.Command;
import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.gui.utils.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

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
 * The GUI panel for the command overview panel.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandOverviewPanel extends JPanel {

    private final NuclearBotGUI m_gui;

    private final JComboBox<String> m_commandList;
    private final JLabel m_usageLabel;
    private final JLabel m_descriptionLabel;

    public CommandOverviewPanel(final NuclearBotGUI gui)
    {
        super(new VerticalLayout());
        setBorder(BorderFactory.createTitledBorder("Overview"));

        m_gui = gui;

        final JPanel namePanel = new JPanel(new FlowLayout());
        {
            final JLabel namePrefixLabel = new JLabel("<html><u>Name:</u></html>");
            m_commandList = new JComboBox<>();

            m_commandList.setEditable(false);
            m_commandList.addItemListener(e ->
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    updateCommandInfo();
            });

            namePanel.add(namePrefixLabel);
            namePanel.add(m_commandList);
        }

        final JPanel usagePanel = new JPanel(new FlowLayout());
        {
            final JLabel usagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
            m_usageLabel = new JLabel("(no such command)");

            usagePrefixLabel.setToolTipText("Angle brackets for required arguments\nSquare brackets for optional arguments");
            m_usageLabel.setToolTipText("Angle brackets for required arguments\nSquare brackets for optional arguments");

            usagePanel.add(usagePrefixLabel);
            usagePanel.add(m_usageLabel);
        }

        final JPanel descriptionPanel = new JPanel(new FlowLayout());
        {
            final JLabel descriptionPrefixLabel = new JLabel("<html><u>Description:</u></html>");
            m_descriptionLabel = new JLabel("(no such command)");

            descriptionPanel.add(descriptionPrefixLabel);
            descriptionPanel.add(m_descriptionLabel);
        }

        add(namePanel);
        add(usagePanel);
        add(descriptionPanel);
    }

    public void addCommand(final String name)
    {
        m_commandList.addItem(name);
        updateCommandInfo();
    }

    public void removeCommand(final String name)
    {
        m_commandList.removeItem(name);
        updateCommandInfo();
    }

    public void clearCommandList()
    {
        m_commandList.removeAllItems();
    }

    private void updateCommandInfo()
    {
        final String label = (String) m_commandList.getSelectedItem();
        final Command command = m_gui.getClient().getCommand(label);

        final String usage = command != null ? command.getUsage() : "(no such command)";
        final String description = command != null ? command.getDescription() : "(no such command)";

        m_usageLabel.setText(usage);
        m_descriptionLabel.setText(description);
    }

}

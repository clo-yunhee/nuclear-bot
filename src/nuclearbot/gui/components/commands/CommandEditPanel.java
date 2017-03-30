package nuclearbot.gui.components.commands;

import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.gui.commands.UserCommandManager;
import nuclearbot.gui.utils.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
 * The GUI panel for the command edition panel.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandEditPanel extends JPanel {

    private static final String HELP_TEXT = "The convention for usage format is the following:\n"
            + "- angle brackets for required arguments;\n"
            + "- square brackets for optional arguments;\n"
            + "but the GUI creator doesn't support optional arguments yet.\n"
            + "Please only write arguments without the command in the usage field.\n\n"
            + "To include arguments in the response, use \"$n\" or \"{$n}\", where n is the argument index.\n"
            + "The zero-th argument is the sender's username.\n"
            + "For instance, to create a hug command, put \"!hug <target>\" in usage and \"$0 hugs $1.\" in response.\n\n"
            + "By default the commands are created persistent, which means they will be created again on start.\n"
            + "Non-persistent commands are lost once the bot stops or restarts.";

    private final NuclearBotGUI m_gui;

    private final JComboBox<String> m_commandCombo;
    private final JTextField m_usageField;
    private final JTextField m_descriptionField;
    private final JTextField m_responseField;

    private final UserCommandManager m_commands;

    public CommandEditPanel(final NuclearBotGUI gui)
    {
        super(new VerticalLayout());
        setBorder(BorderFactory.createTitledBorder("Create/Update"));

        m_gui = gui;

        final JPanel namePanel = new JPanel(new FlowLayout());
        {
            final JLabel namePrefixLabel = new JLabel("<html><u>Name:</u></html>");
            m_commandCombo = new JComboBox<>();

            m_commandCombo.setEditable(true);

            m_commandCombo.addActionListener(e -> updateCommandInfo());
            m_commandCombo.addItemListener(e ->
            {
                if (e.getStateChange() == ItemEvent.SELECTED)
                    updateCommandInfo();
            });
            m_commandCombo.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e)
                {
                    updateCommandInfo();
                }
            });

            namePanel.add(namePrefixLabel);
            namePanel.add(m_commandCombo);
        }

        final JPanel usagePanel = new JPanel(new FlowLayout());
        {
            final JLabel usagePrefixLabel = new JLabel("<html><u>Usage:</u></html>");
            m_usageField = new JTextField(12);

            usagePanel.add(usagePrefixLabel);
            usagePanel.add(m_usageField);
        }

        final JPanel descriptionPanel = new JPanel(new FlowLayout());
        {
            final JLabel descriptionPrefixLabel = new JLabel("<html><u>Description:</u></html>");
            m_descriptionField = new JTextField(16);

            descriptionPanel.add(descriptionPrefixLabel);
            descriptionPanel.add(m_descriptionField);
        }

        final JPanel responsePanel = new JPanel(new FlowLayout());
        {
            final JLabel responsePrefixLabel = new JLabel("<html><u>Response:</u></html>");
            m_responseField = new JTextField(16);

            responsePanel.add(responsePrefixLabel);
            responsePanel.add(m_responseField);
        }

        final JPanel submitPanel = new JPanel(new FlowLayout());
        {
            final JButton createButton = new JButton("Create/Update");
            final JButton removeButton = new JButton("Remove");
            final JButton helpButton = new JButton("Help...");

            createButton.addActionListener(e -> createCommand());

            removeButton.addActionListener(e -> removeCommand());

            helpButton.addActionListener(e ->
                    m_gui.getDialogs().info(HELP_TEXT, "Command creation help"));

            submitPanel.add(createButton);
            submitPanel.add(removeButton);
            submitPanel.add(helpButton);
        }

        m_commands = new UserCommandManager(gui, m_commandCombo);

        add(namePanel);
        add(usagePanel);
        add(descriptionPanel);
        add(responsePanel);
        add(submitPanel);

        m_commands.loadCommands();
    }

    public void registerCommands()
    {
        m_commands.registerCommands();
    }

    private void updateCommandInfo()
    {
        final String label = String.valueOf(m_commandCombo.getSelectedItem()).trim().toLowerCase();
        final UserCommandManager.CommandInfo command = m_commands.getCommand(label);
        m_usageField.setText(command != null ? command.usage : "");
        m_descriptionField.setText(command != null ? command.description : "");
        m_responseField.setText(command != null ? command.response : "");
    }

    private void createCommand()
    {
        final String name = ((String) m_commandCombo.getSelectedItem()).trim().toLowerCase();
        final String usage = m_usageField.getText().trim();
        final String description = m_descriptionField.getText().trim();
        final String response = m_responseField.getText().trim();

        m_commands.createUserCommand(name, usage, description, response, false);
    }

    private void removeCommand()
    {
        m_commands.removeCommand(((String) m_commandCombo.getSelectedItem()).trim().toLowerCase());
    }

}

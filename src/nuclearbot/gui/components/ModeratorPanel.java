package nuclearbot.gui.components;

import nuclearbot.client.Moderators;
import nuclearbot.gui.NuclearBotGUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

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
 * GUI panel for the moderator list.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ModeratorPanel extends JPanel {

    private final NuclearBotGUI m_gui;

    private final DefaultListModel<String> m_moderators;

    public ModeratorPanel(final NuclearBotGUI gui)
    {
        super(new BorderLayout());

        m_gui = gui;

        m_moderators = new DefaultListModel<>();

        final JList<String> list = new JList<>(m_moderators);
        final JScrollPane listScroll = new JScrollPane(list);

        listScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        listScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        final JPanel side = new JPanel(new FlowLayout());
        {
            final JTextField nameField = new JTextField(10);
            final JButton addButton = new JButton("Add");
            final JButton removeButton = new JButton("Remove");

            final ActionListener addAction = e ->
            {
                final String name = nameField.getText().toLowerCase().trim();
                if (!name.isEmpty())
                {
                    nameField.setText("");
                    addModerator(name);
                }
            };

            nameField.addActionListener(addAction);
            addButton.addActionListener(addAction);

            removeButton.addActionListener(e ->
                    list.getSelectedValuesList().forEach(this::removeModerator));

            side.add(nameField);
            side.add(addButton);
            side.add(removeButton);
        }

        add(listScroll, BorderLayout.CENTER);
        add(side, BorderLayout.SOUTH);

        // init
        Moderators.getModerators().forEach(m_moderators::addElement);
    }

    public void addModerator(final String name)
    {
        if (Moderators.isModerator(name))
        {
            m_gui.getDialogs().warning("User \"" + name + "\" is already moderator.", "Already moderator");
        }
        else
        {
            Moderators.addModerator(name);

            // this is to ensure the list remains sorted
            if (m_moderators.isEmpty())
            {
                m_moderators.addElement(name);
            }
            else
            {
                int index = 0;
                String element;
                do
                {
                    element = m_moderators.get(index);
                    if (element.compareTo(name) > 0) // insert the element at the right index
                    {
                        m_moderators.add(index, name);
                        break;
                    }
                    index++;
                }
                while (index < m_moderators.size());
            }
        }
    }

    public void removeModerator(final String name)
    {
        if (!Moderators.isModerator(name))
        {
            m_gui.getDialogs().warning("User \"" + name + "\" is not moderator.", "Not moderator");
        }
        else
        {
            Moderators.removeModerator(name);
            m_moderators.removeElement(name);
        }
    }

}

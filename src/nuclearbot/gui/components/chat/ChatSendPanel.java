package nuclearbot.gui.components.chat;

import nuclearbot.gui.NuclearBotGUI;

import javax.swing.*;
import java.awt.*;

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
 * The GUI panel for the chat's send message panel.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ChatSendPanel extends JPanel {

    private final NuclearBotGUI m_gui;

    private final JTextField m_messageField;
    private final JButton m_sendButton;

    public ChatSendPanel(final NuclearBotGUI gui) {
        super(new BorderLayout());

        m_gui = gui;

        m_messageField = new JTextField();

        m_sendButton = new JButton("Send");

        m_messageField.setFont(m_messageField.getFont().deriveFont(Font.PLAIN));
        m_sendButton.setEnabled(false);

        m_messageField.addActionListener(e -> sendMessage());
        m_sendButton.addActionListener(e -> sendMessage());

        add(m_messageField, BorderLayout.CENTER);
        add(m_sendButton, BorderLayout.EAST);
    }

    private void sendMessage() {
        if (m_gui.isClientRunning()) {
            final String message = m_messageField.getText().trim();
            if (!message.isEmpty()) {
                m_gui.getClient().sendMessage(message);
                m_messageField.setText("");
            }
        }
    }

    public void toggleSendButton(final boolean enable) {
        m_sendButton.setEnabled(enable);
    }

}

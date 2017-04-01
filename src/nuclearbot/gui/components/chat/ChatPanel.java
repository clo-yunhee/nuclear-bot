package nuclearbot.gui.components.chat;

import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.util.HTML;

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
 * The GUI panel for the chat tab.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ChatPanel extends JPanel {

    private final LimitedStringList m_chatHistory;
    private final ChatSendPanel m_sendPanel;

    public ChatPanel(final NuclearBotGUI gui) {
        super(new BorderLayout());

        final JScrollPane chatScrollPane = new JScrollPane();
        m_chatHistory = new LimitedStringList();
        m_sendPanel = new ChatSendPanel(gui);

        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setViewportView(m_chatHistory);

        m_chatHistory.setComponentPopupMenu(gui.getTextPopupMenu());
        m_chatHistory.setFont(m_chatHistory.getFont().deriveFont(Font.PLAIN));

        add(chatScrollPane, BorderLayout.CENTER);
        add(m_sendPanel, BorderLayout.SOUTH);
    }

    public void addMessage(final String username, final String message) {
        m_chatHistory.add("<html><strong>" + username + " :</strong> " + HTML.escapeText(message) + "</html>");
    }

    public void toggleSendButton(final boolean enable) {
        m_sendPanel.toggleSendButton(enable);
    }

}

package nuclearbot.gui.components.chat

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.util.JavaWrapper
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextField

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
 * The GUI panel for the chat's send message panel.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ChatSendPanel(private val gui: NuclearBotGUI) : JPanel(BorderLayout()) {

    private val messageField = JTextField().apply {
        font = font.deriveFont(Font.PLAIN)
        addActionListener { sendMessage() }
    }

    private val m_sendButton = JButton("Send").apply {
        isEnabled = false
        addActionListener { sendMessage() }
    }

    init {
        add(messageField, BorderLayout.CENTER)
        add(m_sendButton, BorderLayout.EAST)
    }

    private fun sendMessage() {
        if (gui.isClientRunning) {
            val message = messageField.text.trim { it <= ' ' }
            if (!message.isEmpty()) {
                gui.client.sendMessage(message)
                messageField.text = ""
            }
        }
    }

    var isSendEnabled by JavaWrapper(m_sendButton::isEnabled, m_sendButton::setEnabled)

}

package nuclearbot.gui.components.chat

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.util.HTML
import nuclearbot.util.PropertyWrapper
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JScrollPane

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
 * The GUI panel for the chat tab.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ChatPanel(gui: NuclearBotGUI) : JPanel(BorderLayout()) {

    private val chatHistory: LimitedStringList
    private val sendPanel: ChatSendPanel

    init {

        val chatScrollPane = JScrollPane()
        chatHistory = LimitedStringList()
        sendPanel = ChatSendPanel(gui)

        chatScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        chatScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        chatScrollPane.setViewportView(chatHistory)

        chatHistory.componentPopupMenu = gui.popupMenu
        chatHistory.font = chatHistory.font.deriveFont(Font.PLAIN)

        add(chatScrollPane, BorderLayout.CENTER)
        add(sendPanel, BorderLayout.SOUTH)
    }

    fun addMessage(username: String, message: String) {
        chatHistory.add("<html><strong>$username :</strong> ${HTML.escapeText(message)}</html>")
    }

    var isSendEnabled by PropertyWrapper(sendPanel::isSendEnabled)

}

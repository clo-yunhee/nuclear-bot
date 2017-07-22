package nuclearbot.gui.components.console

import nuclearbot.gui.NuclearBotGUI
import java.awt.Cursor
import java.awt.Font
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.text.DefaultCaret
import javax.swing.text.Document

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
 * The GUI panel for the console output.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ConsolePanel(gui: NuclearBotGUI, consoleDocument: Document) : JScrollPane() {

    init {
        val consoleTextArea = JTextArea(consoleDocument).apply {
            isEditable = false
            caret = DefaultCaret().apply {
                isVisible = true
                updatePolicy = DefaultCaret.ALWAYS_UPDATE
            }
            cursor = Cursor(Cursor.TEXT_CURSOR)
            componentPopupMenu = gui.popupMenu
            font = Font("Courier New", Font.PLAIN, 14)
        }

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS)
        setViewportView(consoleTextArea)
    }

}

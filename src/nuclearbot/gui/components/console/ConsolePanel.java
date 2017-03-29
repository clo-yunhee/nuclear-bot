package nuclearbot.gui.components.console;

import nuclearbot.gui.NuclearBotGUI;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
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
 * The GUI panel for the console output.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ConsolePanel extends JScrollPane {

    public ConsolePanel(final NuclearBotGUI gui, final Document consoleDocument)
    {
        final JTextArea consoleTextArea = new JTextArea(consoleDocument);
        final DefaultCaret caret = new DefaultCaret();

        caret.setVisible(true);
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        consoleTextArea.setEditable(false);
        consoleTextArea.setCaret(caret);
        consoleTextArea.setCursor(new Cursor(Cursor.TEXT_CURSOR));
        consoleTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        consoleTextArea.setComponentPopupMenu(gui.getTextPopupMenu());

        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setViewportView(consoleTextArea);
    }

}

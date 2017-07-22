package nuclearbot.gui.components

import nuclearbot.gui.utils.HyperlinkListener
import java.awt.BorderLayout
import java.awt.Cursor
import javax.swing.JLabel
import javax.swing.JPanel

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
 * The GUI panel for the window footer.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class FooterPanel : JPanel(BorderLayout()) {

    init {
        val copyrightAndLicenseLabel = JLabel("Copyright \u00a9 2017 NuclearCoder. Licensed under A-GPLv3.").apply {
            font = font.deriveFont(10f)
        }

        val sourceLinkLabel = JLabel("<html><a href=\"\">Source code here</a></html>").apply {
            addMouseListener(HyperlinkListener(GITHUB_URL))
            toolTipText = GITHUB_URL
            cursor = Cursor(Cursor.HAND_CURSOR)
            font = font.deriveFont(10f)
        }

        add(copyrightAndLicenseLabel, BorderLayout.WEST)
        add(sourceLinkLabel, BorderLayout.EAST)
    }

    companion object {
        private const val GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/"
    }

}
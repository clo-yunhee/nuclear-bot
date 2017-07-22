package nuclearbot.gui.components.commands

import nuclearbot.gui.NuclearBotGUI
import java.awt.GridLayout
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
 * The GUI panel for command overview/edition.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class CommandPanel(gui: NuclearBotGUI) : JPanel(GridLayout(0, 2)) {

    private val overviewPanel = CommandOverviewPanel(gui).also { add(it) }
    private val editPanel = CommandEditPanel(gui).also { add(it) }

    fun addCommandList(name: String) {
        overviewPanel.addCommand(name)
    }

    fun removeCommandList(name: String) {
        overviewPanel.removeCommand(name)
    }

    fun registerCommands() {
        editPanel.registerCommands()
    }

    fun unregisterCommands() {
        overviewPanel.clearCommandList()
    }

}

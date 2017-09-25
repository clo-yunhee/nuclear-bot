package nuclearbot.gui.components

import nuclearbot.client.Moderators
import nuclearbot.gui.NuclearBotGUI
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.border.CompoundBorder
import javax.swing.border.EmptyBorder

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
 * GUI panel for the moderator list.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ModeratorPanel(private val gui: NuclearBotGUI) : JPanel(BorderLayout()) {

    private val moderators = DefaultListModel<String>()

    init {
        val list = JList(moderators).apply {
            layoutOrientation = JList.VERTICAL_WRAP
            setCellRenderer(object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(list: JList<*>, value: Any?,
                                                          index: Int, isSelected: Boolean,
                                                          cellHasFocus: Boolean) =
                        (super.getListCellRendererComponent(list, value, index,
                                isSelected, cellHasFocus) as JLabel).apply {
                            val matteBorder = BorderFactory.createMatteBorder(1, 1, 1, 1,
                                    UIManager.getColor("Table.gridColor"))
                            val margin = EmptyBorder(0, 5, 0, 5)
                            border = CompoundBorder(matteBorder, margin)
                            horizontalAlignment = JLabel.CENTER
                            verticalAlignment = JLabel.CENTER
                        }
            })
        }

        val listScroll = JScrollPane(list).apply {
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
        }

        val side = JPanel(FlowLayout()).apply {
            val nameField = JTextField(20)
            val addButton = JButton("Add")
            val removeButton = JButton("Remove").apply {
                addActionListener {
                    list.selectedValuesList.forEach(this@ModeratorPanel::removeModerator)
                }
            }

            val addAction = { _: ActionEvent ->
                val name = nameField.text.toLowerCase().trim { it <= ' ' }
                if (!name.isEmpty()) {
                    nameField.text = ""
                    addModerator(name)
                }
            }

            nameField.addActionListener(addAction)
            addButton.addActionListener(addAction)

            add(nameField)
            add(addButton)
            add(removeButton)
        }

        add(listScroll, BorderLayout.CENTER)
        add(side, BorderLayout.SOUTH)

        // init
        Moderators.getModerators().forEach(moderators::addElement)
    }

    fun addModerator(name: String) {
        if (Moderators.isModerator(name)) {
            gui.dialogs.warning("User \"$name\" is already moderator.", "Already moderator")
        } else {
            Moderators.addModerator(name)

            // this is to ensure the list remains sorted
            if (moderators.isEmpty) {
                moderators.addElement(name)
            } else {
                moderators.add(moderators.elements().asSequence().indexOfLast { it < name }, name)
            }
        }
    }

    fun removeModerator(name: String) {
        if (!Moderators.isModerator(name)) {
            gui.dialogs.warning("User \"$name\" is not moderator.", "Not moderator")
        } else {
            Moderators.removeModerator(name)
            moderators.removeElement(name)
        }
    }

}

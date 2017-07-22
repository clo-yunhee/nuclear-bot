package nuclearbot.gui.components.chat

import java.util.*
import javax.swing.AbstractListModel
import javax.swing.JList

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
 * Custom JList that uses a custom model which only keeps the last lines.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class LimitedStringList(capacity: Int = 50) : JList<String>() {

    private val listModel = LimitedStringList.Model(capacity).also { model = it }

    /**
     * Adds an element to the model. If the size of the list
     * will exceed the maximum capacity, the first element
     * is removed before adding the new element to the end
     * of the list.
     *
     * @param text the line to add to the list
     */
    fun add(text: String) {
        listModel.add(text)
    }

    /**
     * The ListModel used by the LimitedStringList class
     */
    class Model(private val capacity: Int) : AbstractListModel<String>() {

        private val arrayList = ArrayList<String>(capacity)

        /**
         * Adds an element to the model. If the size of the list
         * will exceed the maximum capacity, the first element
         * is removed before adding the new element to the end
         * of the list.
         *
         * @param text the line to add to the list
         */
        fun add(text: String) {
            val index0: Int
            val index1: Int
            if (arrayList.size == capacity) {
                index0 = 0
                index1 = capacity - 1
                arrayList.removeAt(0)
            } else {
                index1 = arrayList.size
                index0 = index1
            }
            arrayList.add(text)
            fireContentsChanged(this, index0, index1)
        }

        override fun getSize(): Int {
            return arrayList.size
        }

        override fun getElementAt(index: Int): String {
            return arrayList[index]
        }

    }

}

package nuclearbot.client

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nuclearbot.util.Logger
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.*

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
 * Static class for moderator list.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
object Moderators {

    private const val FILE_NAME = "moderators.json"

    private val file = File(FILE_NAME).also {
        if (!it.exists() && it.mkdirs() && it.delete()) {
            try {
                if (it.createNewFile()) {
                    FileWriter(it, false).use { writer -> writer.write("[]") }
                }
            } catch (e: IOException) {
                Logger.warning("(mod) Could not create \"$FILE_NAME\" for persistence.")
                Logger.warning("(mod) Moderators will only last one lifetime.")
                Logger.printStackTrace(e)
            }

        }
    }

    private val moderators = Collections.synchronizedSortedSet(TreeSet<String>())

    init {
        loadModerators()
    }

    fun loadModerators() {
        try {
            FileReader(file).use {
                Gson().fromJson<List<String>>(it, object : TypeToken<List<String>>() {}.type)
                        .forEach(this::addModerator)
            }
        } catch (e: IOException) {
            Logger.warning("(mod) Could not load the moderator list.")
            Logger.printStackTrace(e)
        }

    }

    fun saveModerators() {
        try {
            FileWriter(file).use { Gson().toJson(moderators, List::class.java, it) }
        } catch (e: IOException) {
            Logger.warning("(mod) Could not save the moderator list.")
            Logger.printStackTrace(e)
        }

    }

    fun addModerator(name: String) {
        moderators.add(name)
        saveModerators()
    }

    fun removeModerator(name: String) {
        moderators.remove(name)
        saveModerators()
    }

    fun isModerator(name: String): Boolean {
        return moderators.contains(name)
    }

    fun getModerators(): SortedSet<String> {
        return Collections.unmodifiableSortedSet(moderators)
    }

}

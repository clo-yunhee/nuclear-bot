package nuclearbot.util

import java.util.*
import java.util.concurrent.TimeUnit
import java.util.function.BooleanSupplier

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
 * Utility for watcher tasks.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
object Watcher {

    private val tasks = mutableMapOf<String, TimerTask>()

    private val timer = Timer("watcher", true)

    private val DELAY = TimeUnit.SECONDS.toMillis(0)
    private val PERIOD = TimeUnit.SECONDS.toMillis(3)

    /**
     * Schedules a watcher task with the given predicate and task.
     * Fails silently if there is already a task with such a name.
     * @param name      the name of the task
     * @param predicate the predicate to tell whether to run the task or not
     * @param task      the task to run if the predicate is true
     */
    fun schedule(name: String, predicate: BooleanSupplier, task: Runnable) {
        if (!tasks.containsKey(name)) {
            object : TimerTask() {
                override fun run() {
                    if (predicate.asBoolean) task.run()
                }
            }.let {
                tasks.put(name, it)
                timer.schedule(it, DELAY, PERIOD)
            }
        }
    }

    /**
     * Cancels the watcher task with the given name.
     * Fails silently if there is no task with such a name.

     * @param name the name of the task
     */
    fun cancel(name: String) {
        if (tasks.containsKey(name)) {
            tasks.remove(name)?.cancel()
            timer.purge()
        }
    }

}

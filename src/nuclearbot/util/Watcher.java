package nuclearbot.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

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
 * Utility for watcher tasks.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class Watcher {

    private static final Map<String, TimerTask> tasks = new HashMap<>();

    private static final Timer timer = new Timer("watcher", true);

    private static final long DELAY = TimeUnit.SECONDS.toMillis(0);
    private static final long PERIOD = TimeUnit.SECONDS.toMillis(3);

    private Watcher()
    {
    }

    /**
     * Schedules a watcher task with the given predicate and task.
     * Fails silently if there is already a task with such a name.
     *
     * @param name      the name of the task
     * @param predicate the predicate to tell whether to run the task or not
     * @param task      the task to run if the predicate is true
     */
    public static final void schedule(final String name, final BooleanSupplier predicate,
            final Runnable task)
    {
        if (!tasks.containsKey(name))
        {
            final TimerTask timerTask = new TimerTask() {
                @Override public void run()
                {
                    if (predicate.getAsBoolean())
                        task.run();
                }
            };
            tasks.put(name, timerTask);
            timer.schedule(timerTask, DELAY, PERIOD);
        }
    }

    /**
     * Cancels the watcher task with the given name.
     * Fails silently if there is no task with such a name.
     *
     * @param name the name of the task
     */
    public static final void cancel(final String name)
    {
        if (tasks.containsKey(name))
        {
            tasks.remove(name).cancel();
            timer.purge();
        }
    }

}

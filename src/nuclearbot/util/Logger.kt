package nuclearbot.util

import java.io.FileWriter
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
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
 * Static class for a custom logger.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
object Logger {

    private const val LOG = "[%s] %s: %s"

    private val timeFormat = SimpleDateFormat("yyyy-MM-d hh:mm:ss")

    private val fileOut = try {
        PrintWriter(FileWriter("nuclearbot.log", true), true)
    } catch (e: IOException) {
        System.err.println("Couldn't open the log file. Logging to console only.")
        e.printStackTrace()
        null
    }

    init {
        // we don't need a shutdown hook if we didn't open the log file
        if (fileOut != null) {
            Runtime.getRuntime().addShutdownHook(Thread(LoggerShutdownHook()))
        }
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler())
    }

    /**
     * Logs raw text.

     * @param string the text to log
     */
    @Synchronized fun write(string: String) {
        print(string)
        fileOut?.print(string)
    }

    /**
     * Logs raw text, followed by a line break.

     * @param string the text to log
     */
    @Synchronized fun writeln(string: String) {
        println(string)
        fileOut?.println(string)
    }

    /**
     * Logs text with timestamp and specified prefix.

     * @param string the text to log
     * *
     * @param level  the prefix to put
     */
    @Synchronized fun log(string: String, level: String) {
        writeln(String.format(LOG, timeFormat.format(Date()), level, string))
    }

    /**
     * Logs text at INFO level

     * @param string the text to log
     */
    @Synchronized fun info(string: String) {
        log(string, "INFO")
    }

    /**
     * Logs text at WARNING level

     * @param string the text to log
     */
    @Synchronized fun warning(string: String) {
        log(string, "WARNING")
    }

    /**
     * Logs text at ERROR level

     * @param string the text to log
     */
    @Synchronized fun error(string: String) {
        log(string, "ERROR")
    }

    /**
     * Logs a Throwable and its backtrace.

     * @param throwable the Throwable to log
     */
    @Synchronized fun printStackTrace(throwable: Throwable) {
        throwable.printStackTrace(System.out)
        if (fileOut != null) {
            throwable.printStackTrace(fileOut)
        }
    }

    private class LoggerShutdownHook : Runnable {

        override fun run() {
            Logger.info("(Exit) Closing log file...")
            fileOut?.close()
        }

    }

    private class UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {

        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            Logger.error("Uncaught exception in thread \"${thread.name}\":")
            Logger.printStackTrace(throwable)
            System.exit(1)
        }

    }

}

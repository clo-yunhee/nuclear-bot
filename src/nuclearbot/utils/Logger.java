package nuclearbot.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Copyright (C) 2016 NuclearCoder
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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Static class for a custom logger.
 */
public class Logger {
	
	private static final String LOG = "[%s] %s: %s";
	
	private static DateFormat timeFormat;
	private static PrintWriter fileOut;
	
	static
	{
		Runtime.getRuntime().addShutdownHook(new Thread(new LoggerShutdownHook()));
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
		timeFormat = new SimpleDateFormat("hh:mm:ss");
		try
		{
			fileOut = new PrintWriter(new FileWriter("nuclearbot.log", true), true);
		}
		catch (IOException e)
		{
			System.err.println("Couldn't open the log file. Logging to console only.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Logs raw text.
	 * @param str the text to log
	 */
	public synchronized static void write(String str)
	{
		System.out.print(str);
		fileOut.print(str);
	}
	
	/**
	 * Logs raw text, followed by a line break.
	 * @param str the text to log
	 */
	public synchronized static void writeln(String str)
	{
		System.out.println(str);
		fileOut.println(str);
	}
	
	/**
	 * Logs text with timestamp and specified prefix.
	 * @param str the text to log
	 * @param level the prefix to put
	 */
	public synchronized static void log(String log, String level)
	{
		writeln(String.format(LOG, timeFormat.format(new Date()), level, log));
	}
	
	/**
	 * Logs text at INFO level
	 * @param str the text to log
	 */
	public synchronized static void info(String str)
	{
		log(str, "INFO");
	}
	
	/**
	 * Logs text at WARNING level
	 * @param str the text to log
	 */
	public synchronized static void warning(String str)
	{
		log(str, "WARNING");
	}
	
	/**
	 * Logs text at ERROR level
	 * @param str the text to log
	 */
	public synchronized static void error(String str)
	{
		log(str, "ERROR");
	}
	
	/**
	 * Logs a Throwable and its backtrace.
	 * @param thr the Throwable to log
	 */
	public synchronized static void printStackTrace(Throwable thr)
	{
		thr.printStackTrace(System.out);
		thr.printStackTrace(fileOut);
	}
	
	private static class LoggerShutdownHook implements Runnable {
		
		@Override
		public void run()
		{
			Logger.info("(Exit) Closing log file...");
			fileOut.close();
		}
	}
	
	private static class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread thread, Throwable throwable)
		{
			Logger.error("Uncaught exception in thread \"" + thread.getName() + "\":");
			Logger.printStackTrace(throwable);
		}
		
	}
	
}

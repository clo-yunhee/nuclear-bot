package nuclearbot.builtin.osu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import nuclearbot.utils.Logger;
import nuclearbot.utils.OSUtils;

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
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)<br>
 * <br>
 * Static class to get osu!'s current song with a cross-platform call.
 */
public class NowPlaying {
	
	private static final String UNKNOWN_OS = "Couldn't determine the operating system.";
	
	private static final String NOT_RUNNING = "osu! isn't running.";
	private static final String NOTHING = "No song is playing right now.";
	private static final String ERROR = "An error occurred while fetching osu! window title.";
	private static final String NOW_PLAYING = "Now playing \"%s\".";
	
	private static String parseTitle(final String windowTitle)
	{
		final String[] titleArray = windowTitle.split(" - ", 2); // remove the heading 'osu! - ' prefix
		if (titleArray.length < 2)
		{
			return NOTHING;
		}
		else
		{
			return String.format(NOW_PLAYING, titleArray[1]);
		}
	}
	
	private static String linux()
	{
		String windowTitle = null;
		try // first, list osu! windows
		{
			final Process processWin = Runtime.getRuntime().exec("xdotool search --classname osu");
			final BufferedReader inWin = new BufferedReader(new InputStreamReader(processWin.getInputStream()));
			boolean foundOsuWindow = false;
			String lineWin = null;
	        while (!foundOsuWindow && (lineWin = inWin.readLine()) != null)
	        {
	        	final int window = Integer.parseInt(lineWin.trim()); // for each window, check the title
	        	final Process processTitle = Runtime.getRuntime().exec("xdotool getwindowname " + window);
				final BufferedReader inTitle = new BufferedReader(new InputStreamReader(processTitle.getInputStream()));
				
				final String name = inTitle.readLine().trim();
				
				inTitle.close();
				// only one should have "osu!" in it (windows should be: X container, .NET container, actual osu! game)
				if (name.contains("osu!"))
				{
					windowTitle = name;
					foundOsuWindow = true;
				}
	        }
			inWin.close();
			
			if (windowTitle == null)
			{
				Logger.error("(osu!) None of the windows were titled osu.");
				return NOT_RUNNING;
			}
		}
		catch (NumberFormatException e)
		{
			Logger.error(NOT_RUNNING);
			return NOT_RUNNING;
		}
		catch (IOException | IllegalStateException e)
		{
			Logger.error(ERROR);
			Logger.printStackTrace(e);
			return ERROR;
		}
		
		return parseTitle(windowTitle);
	}
	
	private static String windows()
	{
		String output = "";
		try
		{
			final Process process = Runtime.getRuntime().exec("tasklist /fo csv /nh /fi \"imagename eq osu!.exe\" /v");
			final BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
	        while ((line = in.readLine()) != null)
	        {
	        	output += line + '\n';
	        }
			in.close();
		}
		catch (IOException e)
		{
			Logger.error(ERROR);
			Logger.printStackTrace(e);
			return ERROR;
		}
		if (!output.startsWith("\"osu!.exe\""))
		{
			Logger.error(NOT_RUNNING);
			return NOT_RUNNING;
		}
		
		final String[] columns = output.split(","); // parse csv
		String windowTitle = columns[8];
		windowTitle = windowTitle.substring(1, windowTitle.length() - 1);
		
		return parseTitle(windowTitle);
	}
	
	/**
	 * Cross-platform method, delegates to platform-dependent methods depending on the OS.
	 * Currently only Linux and Windows 8+ are supported for sure. (W7 wasn't tested yet)
	 * @return a chat-ready text
	 */
	public static String getCurrentSong()
	{
		switch (OSUtils.getOS())
		{
		case LINUX:
			return linux();
		case WINDOWS:
			return windows();
		case UNKNOWN:
		default:
			return UNKNOWN_OS;
		}
	}
	
}

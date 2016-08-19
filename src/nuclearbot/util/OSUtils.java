package nuclearbot.utils;

import java.nio.file.Paths;

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
 * Static class for some OS utility methods.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class OSUtils {
	
	public static enum OSType {
		/** Unknown, undefined, or malformed os.name property */
		UNKNOWN,
		/** GNU/Linux */
		LINUX,
		/** Windows */
		WINDOWS
	}
	
	private static final OSType os;
	private static final String workingDir;
	
	static
	{
		final String osName = System.getProperty("os.name");
		
		if (osName.equals("Linux"))
		{
			os = OSType.LINUX;
		}
		else if (osName.contains("Windows"))
		{
			os = OSType.WINDOWS;
		}
		else
		{
			os = OSType.UNKNOWN;
		}
		
		workingDir = Paths.get(".").toAbsolutePath().toString();
	}
	
	/**
	 * Returns a statically defined OSType enum for this system. 
	 * @return a OSType enum value
	 */
	public static OSType getOS()
	{
		return os;
	}
	
	/**
	 * Returns a statically defined absolute path string of the working directory.
	 * More specifically, of the working directory <em>when the OSUtils class was initialized</em>.
	 */
	public static String workingDir()
	{
		return workingDir;
	}
	
}

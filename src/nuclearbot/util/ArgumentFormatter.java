package nuclearbot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * String formatter for command arguments. Inspired by java.util.Formatter.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class ArgumentFormatter {

	private static final Pattern PATTERN_ARG = Pattern.compile("\\{\\$([0-9]+)\\}|\\$([0-9]+)");
	
	private final FormatString[] m_formatStringArray;
	
	private StringBuilder m_out;
	
	/**
	 * Constructs an argument formatter with the specified format.
	 * Tokens "$n" or "{$n}" are replaced with their corresponding argument.
	 * The zero-th token is the sender's username.
	 * @param format the format string
	 */
	public ArgumentFormatter(final String format)
	{
		m_formatStringArray = parse(format);
		m_out = null;
	}

	private FormatString[] parse(final String format)
	{
		final List<FormatString> list = new ArrayList<FormatString>();
		final Matcher matcher = PATTERN_ARG.matcher(format);
		final int length = format.length();
		for (int i = 0; i < length;)
		{
			if (matcher.find(i))
			{
				// Anything between the start of the string and the beginning
				// of the format specifier is either fixed text or contains
				// an invalid format string.
				if (matcher.start() != i)
				{
					// Assume previous characters were fixed text
					list.add(new FixedString(format.substring(i, matcher.start())));
				}

				list.add(new FormatSpecifier(matcher));
				i = matcher.end();
			}
			else
			{
				// The rest of the string is fixed text
				list.add(new FixedString(format.substring(i)));
				break;
			}
		}
		return list.toArray(new FormatString[0]);
	}

	/**
	 * Returns the formatted string corresponding to the given list of arguments.
	 * @param username the sender's username, token zero
	 * @param args the argument array as given by the onCommand method
	 * @return the formatted string
	 */
	public String format(final String username, final String... args)
	{
		m_out = new StringBuilder();
		final FormatString[] array = m_formatStringArray;
		FormatString formatString;
		int index;
		String arg;
		for (int i = 0; i < array.length; i++)
		{
			formatString = array[i];
			index = formatString.index();
			
			switch (index)
			{
			case -2: // fixed text
				arg = null;
				break;
			case -1: // error in index parse
				return null;
			case 0: // sender username
				arg = username;
				break;
			default: // regular index
				if (args == null || index >= args.length)
					return null;
				arg = args[index];
			}
			formatString.print(arg);
		}
		return m_out.toString();
	}

	private interface FormatString {
		
		public int index();
		public void print(String arg);
		
	}
	
	private class FixedString implements FormatString {
		
		private final String m_str;
		
		public FixedString(final String str)
		{
			m_str = str;
		}
		
		public int index()
		{
			return -2;
		}
		
		public void print(final String arg)
		{
			m_out.append(m_str);
		}
		
	}
	
	private class FormatSpecifier implements FormatString {

		private int m_index;
		
		public FormatSpecifier(final Matcher matcher)
		{
			String indexStr = matcher.group(1);
			if (indexStr == null)
			{
				indexStr = matcher.group(2);
			}
			if (indexStr != null)
			{
				try
				{
					m_index = Integer.parseInt(indexStr);
				}
				catch (NumberFormatException x)
				{
					m_index = -1;
				}
			}
			else
			{
				m_index = -1;
			}
		}

		public int index()
		{
			return m_index;
		}
		
		public void print(final String arg)
		{
			m_out.append(arg);
		}
		
	}
	
}

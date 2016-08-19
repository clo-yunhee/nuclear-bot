package nuclearbot.util;

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
 * Static class for escaping HTML text.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class HTML {
	
	/**
	 * Escapes characters in an HTML string.
	 * This method assumes that the string is encoded in UTF-8,
	 * thus nothng other than &amp;&lt;&gt; need to be escaped.
	 * @param s the string to HTML-escape
	 * @return the corrected string
	 */
	public static String escapeText(final String s)
	{
		final int length = s.length();
		final char[] chars = s.toCharArray();
		final StringBuilder sb = new StringBuilder(Math.max(16, length));
		char ch;
		for (int i = 0; i < length; i++)
		{
			ch = chars[i];
			if (ch == '&')
			{
				sb.append("&amp;");
			}
			else if (ch == '<')
			{
				sb.append("&lt;");
			}
			else if (ch == '>')
			{
				sb.append("&gt;");
			}
			else
			{
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
}

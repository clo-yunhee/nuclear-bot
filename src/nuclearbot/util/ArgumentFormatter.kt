package nuclearbot.util

import java.util.regex.Matcher

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
 * String formatter for command arguments. Inspired by java.util.Formatter.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class ArgumentFormatter
/**
 * Constructs an argument formatter with the specified format.
 * Tokens "$n" or "{$n}" are replaced with their corresponding argument.
 * The zero-th token is the sender's username.

 * @param format the format string
 */
(format: String) {

    private val formatStringArray = parse(format)
    private val out = StringBuilder(20)

    private fun parse(format: String): Array<FormatString> {
        val matcher = PATTERN_ARG.matcher(format)
        val list = mutableListOf<FormatString>()

        var i = 0
        while (i < format.length) {
            if (matcher.find(i)) {
                // Anything between the start of the string and the beginning
                // of the format specifier is either fixed text or contains
                // an invalid format string.
                if (matcher.start() != i) {
                    // Assume previous characters were fixed text
                    list.add(FixedString(format.substring(i, matcher.start())))
                }

                list.add(FormatSpecifier(matcher))
                i = matcher.end()
            } else {
                // The rest of the string is fixed text
                list.add(FixedString(format.substring(i)))
                break
            }
        }

        return list.toTypedArray()
    }

    /**
     * Returns the formatted string corresponding to the given list of arguments.

     * @param username the sender's username, token zero
     * *
     * @param args     the argument array as given by the onCommand method
     * *
     * @return the formatted string
     */
    fun format(username: String, args: Array<String>): String? {
        out.setLength(0)
        for (formatString in formatStringArray) {
            val index = formatString.index
            val arg: String?

            when (index) {
            // fixed text
                -2 -> arg = null
            // error in index parse
                -1 -> return null
            // sender username
                0 -> arg = username
            // regular index
                else -> {
                    if (index >= args.size)
                        return null
                    arg = args[index]
                }
            }

            formatString.print(arg)
        }
        return out.toString()
    }

    private interface FormatString {
        val index: Int
        fun print(arg: String?)
    }

    private inner class FixedString(private val m_str: String) : FormatString {
        override val index = -2
        override fun print(arg: String?) {
            out.append(m_str)
        }
    }

    private inner class FormatSpecifier(matcher: Matcher) : FormatString {

        override val index = (matcher.group(1) ?: matcher.group(2)).let {
            if (it != null) {
                try {
                    Integer.parseInt(it)
                } catch (e: NumberFormatException) {
                    -1
                }
            } else {
                -1
            }
        }

        override fun print(arg: String?) {
            out.append(arg)
        }

    }

    companion object {
        private val PATTERN_ARG = "\\{\\$([0-9]+)}|\\$([0-9]+)".toPattern()
    }

}

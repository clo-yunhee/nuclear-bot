package nuclearbot.client

import nuclearbot.plugin.CommandExecutor

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
 * Public API for a command.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
interface Command {

    /**
     * The command's label.
     * This is the text following the exclamation mark.
     * For instance, if the command `!example`
     * is registered, this will equal `"example"`.
     */
    val label: String

    /**
     * The command's executor instance.
     */
    val executor: CommandExecutor

    /**
     * The command's usage.
     * This is the text shown when the command fails.
     * The convention for arguments is &lt;required-argument&gt; [optional-argument].
     */
    var usage: String

    /**
     * A description of the command.
     * This is the text used in the help command.
     * If there is no description it will be a string equal to "".
     */
    var description: String?

}

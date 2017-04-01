package nuclearbot.gui.commands;/*
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

import nuclearbot.client.ChatClient;
import nuclearbot.client.Command;
import nuclearbot.client.Moderators;
import nuclearbot.plugin.CommandExecutor;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;

/**
 * Command registered by the GUI for moderation of user-defined commands.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class UserCommandModeration implements CommandExecutor {

    private final UserCommandManager m_commands;

    public UserCommandModeration(final UserCommandManager commands) {
        m_commands = commands;
    }

    @Override
    public boolean onCommand(final ChatClient client, final String username, final Command command, final String label, final String[] args)
            throws IOException {
        if (!Moderators.isModerator(username)) {
            // fail silently
            return true;
        }

        final boolean correct;

        if (label.equalsIgnoreCase("cmdadd")) {
            // usage: !cmdadd <name> <response>
            correct = args.length >= 3;
            if (correct) {
                final String response = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                addCommand(client, username, args[1], response);
            }
        } else if (label.equalsIgnoreCase("cmdrem")) {
            // usage: !cmdrem <name>
            correct = args.length >= 2;
            if (correct) {
                removeCommand(client, username, args[1]);
            }
        } else if (label.equalsIgnoreCase("cmdusage")) {
            // usage: !cmdusage <name> <usage>
            correct = args.length >= 3;
            if (correct) {
                final String usage = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                setUsage(client, username, args[1], usage);
            }
        } else if (label.equalsIgnoreCase("cmddesc")) {
            // usage: !cmddesc <name> <description>
            correct = args.length >= 3;
            if (correct) {
                final String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                setDescription(client, username, args[1], description);
            }
        } else {
            correct = false;
        }

        return correct;
    }

    private void addCommand(final ChatClient client, final String username, final String command, final String response) {
        if (client.isCommandRegistered(command)) {
            client.sendMessage("Command already exists, @" + username);
        } else {
            eventQueueCreate(command, "!" + command, "Nothing here!", response);
            client.sendMessage("Command created, @" + username);
        }
    }

    private void removeCommand(final ChatClient client, final String username, final String command) {
        if (!m_commands.contains(command)) {
            client.sendMessage("Command doesn't exist, @" + username);
        } else {
            EventQueue.invokeLater(() -> m_commands.removeCommand(command, true));
            client.sendMessage("Command removed, @" + username);
        }
    }

    private void setUsage(final ChatClient client, final String username, final String command, final String usage) {
        if (!client.isCommandRegistered(command)) {
            client.sendMessage("Command doesn't exist, @" + username);
        } else {
            final UserCommandManager.CommandInfo info = m_commands.getCommand(command);
            eventQueueCreate(command, usage, info.description, info.response);
            client.sendMessage("Command usage updated, @" + username);
        }
    }

    private void setDescription(final ChatClient client, final String username, final String command, final String description) {
        if (!client.isCommandRegistered(command)) {
            client.sendMessage("Command doesn't exist, @" + username);
        } else {
            final UserCommandManager.CommandInfo info = m_commands.getCommand(command);
            eventQueueCreate(command, info.usage, description, info.response);
            client.sendMessage("Command description updated, @" + username);
        }
    }

    private void eventQueueCreate(final String label, final String usage, final String description, final String response) {
        // UserCommandManager alters GUI components, so we need to call it from the EDT
        EventQueue.invokeLater(() -> m_commands.createUserCommand(label, usage, description, response, true));
    }

}

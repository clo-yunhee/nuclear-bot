package nuclearbot.gui.commands;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import nuclearbot.client.ChatClient;
import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.gui.utils.DialogUtil;
import nuclearbot.util.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

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
 * Manager for user-defined commands.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class UserCommandManager {

    private static final String FILE_NAME = "commands.json";
    private final NuclearBotGUI m_gui;
    private final DialogUtil m_dialogs;
    private final JComboBox<String> m_combo;
    private final File m_file;

    private final Map<String, CommandInfo> m_commands;

    // contains the commands that failed to register to the client
    private final Set<String> m_failedRegister;

    public UserCommandManager(final NuclearBotGUI gui, final JComboBox<String> combo)
    {
        m_gui = gui;
        m_commands = new HashMap<>();
        m_failedRegister = new HashSet<>();

        m_dialogs = gui.getDialogs();
        m_combo = combo;

        m_file = new File(FILE_NAME);

        createFileIfNeeded();
    }

    private void createFileIfNeeded()
    {
        if (!m_file.exists() && m_file.mkdirs() && m_file.delete())
        {
            try
            {
                if (m_file.createNewFile())
                {
                    try (final FileWriter writer = new FileWriter(m_file, false))
                    {
                        writer.write("[]");
                    }
                }
            }
            catch (IOException e)
            {
                Logger.warning("(uCmd) Could not create \"" + FILE_NAME + "\" for persistence.");
                Logger.warning("(uCmd) User-defined commands will only last one lifetime.");
                Logger.printStackTrace(e);
            }
        }
    }

    public void createUserCommand(final String name, final String usage, final String description, final String response, final boolean silent)
    {
        final ChatClient client = m_gui.getClient();

        if (m_commands.containsKey(name))
        {
            Logger.info("(uCmd) Updating command \"" + name + "\"...");
            if (m_gui.isClientRunning())
            {
                client.unregisterCommand(name);
            }
            m_commands.remove(name);
            m_combo.removeItem(name);
        }
        else
        {
            Logger.info("(uCmd) Creating command \"" + name + "\"...");
        }

        if (m_gui.isClientRunning())
        {
            if (!client.isCommandRegistered(name))
            {
                client.registerCommand(name, usage, new UserCommand(response)).setDescription(description);
                m_failedRegister.remove(name);
            }
            else
            {
                Logger.warning("(uCmd) Command \"" + name + "\" is already registered.");
                if (!silent)
                {
                    m_dialogs.warning("Command \"" + name + "\" has already been registered.", "Command already registered");
                }
                m_failedRegister.add(name);
            }
        }
        else
        {
            Logger.warning("(uCmd) Command \"" + name + "\" will be registered when the client starts.");
            if (!silent)
            {
                m_dialogs.warning("Command \"" + name + "\" will be registered when the client starts.", "Client is not running");
            }
        }

        m_commands.put(name, new CommandInfo(name, usage, description, response));
        m_combo.addItem(name);

        saveCommands(silent);

        Logger.info("(uCmd) Command \"" + name + "\" created successfully.");
        if (!silent)
        {
            m_dialogs.info("Command \"" + name + "\" created.", "Command created");
        }
    }

    private void saveCommands(final boolean silent)
    {
        try (final FileWriter writer = new FileWriter(m_file, false))
        {
            new Gson().toJson(m_commands.values(), Collection.class, writer);
        }
        catch (IOException e)
        {
            Logger.error("(uCmd) Couldn't save persistent user command:");
            Logger.printStackTrace(e);
            if (!silent)
            {
                m_dialogs.error("Couldn't save persistent user command. Check console for details.", "Couldn't save config");
            }
        }
    }

    public void loadCommands()
    {
        m_commands.clear();

        try (final FileReader reader = new FileReader(m_file))
        {
            final Type type = new TypeToken<List<CommandInfo>>() {
            }.getType();

            final List<CommandInfo> entries = new Gson().fromJson(reader, type);
            for (final CommandInfo command : entries)
            {
                createUserCommand(command.name, command.usage, command.description, command.response, true);
            }
        }
        catch (JsonSyntaxException | IOException e)
        {
            Logger.error("(uCmd) Error while loading user commands:");
            Logger.printStackTrace(e);
            m_dialogs.error("Error in the user commands configuration. Check console for details.", "JSON syntax error");
        }
    }

    public CommandInfo getCommand(final String name)
    {
        return m_commands.get(name);
    }

    public void removeCommand(final String name, final boolean silent)
    {
        if (m_commands.containsKey(name))
        {
            if (m_gui.isClientRunning() && !m_failedRegister.contains(name))
                m_gui.getClient().unregisterCommand(name);

            m_commands.remove(name);
            m_combo.removeItem(name);
            if (!silent)
            {
                Logger.info("(uCmd) Command \"" + name + "\" removed successfully.");
                m_dialogs.info("Command \"" + name + "\" removed successfully.", "Command removed");
            }
        }
        else
        {
            if (!silent)
            {
                Logger.info("(uCmd) Command \"" + name + "\" is not a user command.");
                m_dialogs.warning("Command \"" + name + "\" is not a user command.", "Not a user command");
            }
        }
    }

    public boolean contains(String command)
    {
        return m_commands.containsKey(command);
    }

    public void registerCommands()
    {
        m_failedRegister.clear();
        for (final String name : m_commands.keySet())
        {
            final CommandInfo command = m_commands.get(name);
            final UserCommand executor = new UserCommand(command.response);

            try
            {
                m_gui.getClient().registerCommand(command.name, command.usage, executor)
                        .setDescription(command.description);
            }
            catch (IllegalArgumentException e)
            {
                Logger.warning("(uCmd) User command \"" + command.name + "\" was already registered by something else.");
                m_dialogs.warning("User command \"" + command.name + "\" was already registered by something else.", "Command already registered");
                m_failedRegister.add(name);
            }
        }
    }

    public static class CommandInfo {

        public final String name;
        public final String usage;
        public final String description;
        public final String response;

        private CommandInfo(String name, String usage, String description, String response)
        {
            this.name = name;
            this.usage = usage;
            this.description = description;
            this.response = response;
        }

    }

}

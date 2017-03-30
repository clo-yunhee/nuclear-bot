package nuclearbot.gui.components.commands;

import nuclearbot.gui.NuclearBotGUI;

import javax.swing.*;
import java.awt.*;

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
 * The GUI panel for command overview/edition.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class CommandPanel extends JPanel {

    private final CommandOverviewPanel m_overviewPanel;
    private final CommandEditPanel m_editPanel;

    public CommandPanel(final NuclearBotGUI gui)
    {
        super(new GridLayout(0, 2));

        m_overviewPanel = new CommandOverviewPanel(gui);
        m_editPanel = new CommandEditPanel(gui);

        add(m_overviewPanel);
        add(m_editPanel);
    }

    public void addCommandList(final String name)
    {
        m_overviewPanel.addCommand(name);
    }

    public void removeCommandList(final String name)
    {
        m_overviewPanel.removeCommand(name);
    }

    public void registerCommands()
    {
        m_editPanel.registerCommands();
    }

    public void unregisterCommands()
    {
        m_overviewPanel.clearCommandList();
    }

}

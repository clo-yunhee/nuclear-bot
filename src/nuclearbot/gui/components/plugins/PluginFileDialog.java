package nuclearbot.gui.components.plugins;

import nuclearbot.util.OSUtils;

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
 * The GUI dialog for plugin file selection.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class PluginFileDialog extends FileDialog {

    public PluginFileDialog(final JFrame container)
    {
        super(container, "Choose a file", FileDialog.LOAD);
        setLocationRelativeTo(container);
        setDirectory(OSUtils.workingDir());
        setFile("*.jar");
        setFilenameFilter((dir, name) -> name.endsWith(".jar"));
    }

}

package nuclearbot.gui.utils;

import nuclearbot.util.Logger;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
 * A mouse listener to create pseudo-hyperlink components.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class HyperlinkListener extends MouseAdapter {

    private URI m_uri;

    public HyperlinkListener(final String url) {
        try {
            m_uri = new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            Logger.error("(GUI) Bad URL \"" + url + "\":");
            Logger.printStackTrace(e);
        }
    }

    @Override
    public void mouseClicked(final MouseEvent event) {
        if (m_uri != null && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(m_uri);
            } catch (IOException e) {
                Logger.error("(GUI) Browser issued an I/O exception (\"" + m_uri.toString() + "\"):");
                Logger.printStackTrace(e);
            }
        } else {
            Logger.error("(GUI) Desktop not supported.");
        }
    }

}

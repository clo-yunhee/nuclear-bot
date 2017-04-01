package nuclearbot.gui.components;

import nuclearbot.gui.utils.HyperlinkListener;

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
 * The GUI panel for the window footer.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class FooterPanel extends JPanel {

    private static final String GITHUB_URL = "https://github.com/NuclearCoder/nuclear-bot/";

    public FooterPanel() {
        super(new BorderLayout());

        final JLabel copyrightAndLicenseLabel = new JLabel("Copyright \u00a9 2017 NuclearCoder. Licensed under A-GPLv3.");
        final JLabel sourceLinkLabel = new JLabel("<html><a href=\"\">Source code here</a></html>");

        copyrightAndLicenseLabel.setFont(copyrightAndLicenseLabel.getFont().deriveFont(10F));

        sourceLinkLabel.addMouseListener(new HyperlinkListener(GITHUB_URL));
        sourceLinkLabel.setToolTipText(GITHUB_URL);
        sourceLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sourceLinkLabel.setFont(sourceLinkLabel.getFont().deriveFont(10F));

        add(copyrightAndLicenseLabel, BorderLayout.WEST);
        add(sourceLinkLabel, BorderLayout.EAST);
    }

}
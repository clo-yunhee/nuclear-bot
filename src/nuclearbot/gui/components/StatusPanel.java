package nuclearbot.gui.components;

import nuclearbot.gui.NuclearBotGUI;
import nuclearbot.gui.utils.VerticalLayout;

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
 * The GUI panel for the status tab.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class StatusPanel extends JPanel {

    private final JLabel m_statusLabel;
    private final JButton m_startButton;
    private final JButton m_stopButton;
    private final JButton m_restartButton;
    private final JLabel m_pluginLabel;

    public StatusPanel(final NuclearBotGUI gui)
    {
        super(new VerticalLayout());

        final JPanel controls = new JPanel(new FlowLayout());
        {
            m_statusLabel = new JLabel("Not running");
            m_startButton = new JButton("Start");
            m_stopButton = new JButton("Stop");
            m_restartButton = new JButton("Restart");

            m_statusLabel.setFont(m_statusLabel.getFont().deriveFont(Font.ITALIC));
            m_startButton.setEnabled(true);
            m_stopButton.setEnabled(false);
            m_restartButton.setEnabled(false);

            m_startButton.addActionListener(e -> gui.startClient());

            m_stopButton.addActionListener(e ->
            {
                gui.setRestartClient(false);
                gui.stopClient();
            });

            m_restartButton.addActionListener(e ->
            {
                gui.setRestartClient(true);
                gui.stopClient();
            });

            controls.add(m_statusLabel);
            controls.add(m_startButton);
            controls.add(m_stopButton);
            controls.add(m_restartButton);
        }

        final JPanel currentPlugin = new JPanel(new FlowLayout());
        {
            final JLabel pluginPrefixLabel = new JLabel("<html><u>Current plugin:</u></html>");
            m_pluginLabel = new JLabel();

            m_pluginLabel.setFont(m_pluginLabel.getFont().deriveFont(Font.PLAIN));
            m_pluginLabel.setHorizontalAlignment(SwingConstants.CENTER);
            m_pluginLabel.setComponentPopupMenu(gui.getTextPopupMenu());

            currentPlugin.add(pluginPrefixLabel);
            currentPlugin.add(m_pluginLabel);
        }

        add(controls);
        add(currentPlugin);
    }

    public void setStatusText(final String text)
    {
        m_statusLabel.setText(text);
    }

    public void setPluginText(final String text, final String tooltipText)
    {
        m_pluginLabel.setText(text);
        m_pluginLabel.setToolTipText(tooltipText);
    }

    public void toggleStartButton(final boolean enable)
    {
        m_startButton.setEnabled(enable);
    }

    public void toggleStopButton(final boolean enable)
    {
        m_stopButton.setEnabled(enable);
    }

    public void toggleRestartButton(final boolean enable)
    {
        m_restartButton.setEnabled(enable);
    }

}

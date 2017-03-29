package nuclearbot.plugin;

import nuclearbot.client.ChatClient;

import java.io.IOException;

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
 * Implementation for JavaPlugin.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public abstract class ImplJavaPlugin implements JavaPlugin {

    private final Plugin m_plugin;
    private final boolean m_builtin;

    private final String m_className;

    public ImplJavaPlugin(final Plugin plugin, final boolean builtin)
    {
        m_plugin = plugin;
        m_builtin = builtin;

        m_className = plugin.getClass().getName();
    }

    @Override
    public void onLoad(final ChatClient client) throws IOException
    {
        m_plugin.onLoad(client);
    }

    @Override
    public void onStart(final ChatClient client) throws IOException
    {
        m_plugin.onStart(client);
    }

    @Override
    public void onStop(final ChatClient client) throws IOException
    {
        m_plugin.onStop(client);
    }

    @Override
    public void onMessage(final ChatClient client, final String username, final String message) throws IOException
    {
        m_plugin.onMessage(client, username, message);
    }

    @Override
    public String getClassName()
    {
        return m_className;
    }

    @Override
    public boolean isBuiltin()
    {
        return m_builtin;
    }

    @Override
    public Plugin getHandle()
    {
        return m_plugin;
    }

}

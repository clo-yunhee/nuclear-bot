package nuclearbot.builtin;

import java.io.IOException;

import nuclearbot.client.ChatClient;
import nuclearbot.plugin.Plugin;

/*
 * Copyright (C) 2016 NuclearCoder
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
 * A dummy client plugin that doesn't do anything.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class DummyPlugin implements Plugin {
	
	public void onMessage(final ChatClient client, final String username, final String message) throws IOException {}
	public void onLoad(final ChatClient client) throws IOException {}
	public void onStart(final ChatClient client) throws IOException {}
	public void onStop(final ChatClient client) throws IOException {}

}

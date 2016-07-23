package nuclearbot.builtin.osu;

import nuclearbot.client.ChatListener;
import nuclearbot.plugin.Plugin;
import nuclearbot.utils.Config;

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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Built-in osu! plugin.
 */
public class OsuPlugin implements Plugin {

	@Override
	public ChatListener init()
	{
		final String osuUser = Config.get("osu_user");
		final String osuApiKey = Config.get("osu_api_key");
		final String osuIrcKey = Config.get("osu_irc_key");
		
		return new OsuClient(osuApiKey, osuUser, osuIrcKey);
	}

}

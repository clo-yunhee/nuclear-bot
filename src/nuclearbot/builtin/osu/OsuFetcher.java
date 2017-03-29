package nuclearbot.builtin.osu;

import nuclearbot.util.HTTP;
import nuclearbot.util.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
 * An osu! API fetcher.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class OsuFetcher {

    private final String m_apiKey;

    @SuppressWarnings("deprecated")
    public OsuFetcher(final String apiKey)
    {
        String encoded;
        try
        {
            encoded = URLEncoder.encode(apiKey, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            Logger.warning("(osu!) Encoding UTF-8 was not found, falling back to default encoding.");
            encoded = URLEncoder.encode(apiKey);
        }
        m_apiKey = encoded;
    }

    // osu data fetchers
    private <T> T get(final String page, final String urlParameters, final Class<T> clazz)
    {
        return HTTP.fetchData("http://osu.ppy.sh/api/" + page, "k=" + m_apiKey + '&' + urlParameters, clazz);
    }

    /**
     * Fetches data for a beatmap set with the specified id.
     * Returns an empty array if there is no such map set.
     *
     * @param beatmapsetId the set id
     * @return the beatmap set
     */
    public DataBeatmap[] getBeatmapset(final int beatmapsetId)
    {
        return get("get_beatmaps", "s=" + beatmapsetId, DataBeatmap[].class);
    }

    /**
     * Fetches data about a beatmap with the specified id.
     * Returns null if there is no such beatmap.
     *
     * @param beatmapId the beatmap id
     * @return data about the beatmap
     */
    public DataBeatmap getBeatmap(final int beatmapId)
    {
        try
        {
            return get("get_beatmaps", "b=" + beatmapId, DataBeatmap[].class)[0];
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }
    }

    /**
     * Fetches data about a user with the specified username.
     * Returns null if there is no such user.
     *
     * @param username the username
     * @return data about the user
     */
    public DataUser getUser(final String username)
    {
        try
        {
            return get("get_user", "type=string&u=" + URLEncoder.encode(username, "UTF-8"), DataUser[].class)[0];
        }
        catch (Exception e)
        {
            return null;
        }
    }

}

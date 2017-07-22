package nuclearbot.builtin.osu

import nuclearbot.builtin.osu.data.DataBeatmap
import nuclearbot.builtin.osu.data.DataUser
import nuclearbot.util.HTTP
import nuclearbot.util.Logger

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

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
 * An osu! API fetcher.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class OsuFetcher(apiKey: String) {

    @Suppress("DEPRECATION")
    private fun safeEncode(string: String) = try {
        URLEncoder.encode(string, "UTF-8")
    } catch (e: UnsupportedEncodingException) {
        Logger.warning("(osu!) Encoding UTF-8 was not found, falling back to default encoding.")
        URLEncoder.encode(string)
    }

    private val apiKey = safeEncode(apiKey)

    // osu data fetchers
    private inline fun <reified T> get(page: String, urlParameters: String): T? {
        return HTTP.fetchData("http://osu.ppy.sh/api/$page", "k=$apiKey&$urlParameters", T::class.java)
    }

    /**
     * Fetches data for a beatmap set with the specified id.
     * Returns an empty array if there is no such map set.
     *
     * @param beatmapsetId the set id
     *
     * @return the beatmap set
     */
    fun getBeatmapset(beatmapsetId: Int) = get<Array<DataBeatmap>>("get_beatmaps", "s=$beatmapsetId")

    /**
     * Fetches data about a beatmap with the specified id.
     * Returns null if there is no such beatmap.
     *
     * @param beatmapId the beatmap id
     *
     * @return data about the beatmap
     */
    fun getBeatmap(beatmapId: Int) = get<Array<DataBeatmap>>("get_beatmaps", "b=$beatmapId")?.getOrNull(0)

    /**
     * Fetches data about a user with the specified username.
     * Returns null if there is no such user.
     *
     * @param username the username
     *
     * @return data about the user
     */
    fun getUser(username: String) = get<Array<DataUser>>("get_user", "type=string&u=${safeEncode(username)}")?.getOrNull(0)

}

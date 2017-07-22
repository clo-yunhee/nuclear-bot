package nuclearbot.builtin.osu.data

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
 * Data container for an osu! user data.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class DataUser {

    var userId: Int = 0

    var name: String? = null

    // Total amount for all ranked and approved beatmaps played
    var count300: Int = 0

    // Total amount for all ranked and approved beatmaps played
    var count100: Int = 0

    // Total amount for all ranked and approved beatmaps played
    var count50: Int = 0

    // Only counts ranked and approved beatmaps
    var playCount: Int = 0

    // Counts the best individual score on each ranked and approved beatmaps
    var rankedScore: Long = 0

    // Counts every score on ranked and approved beatmaps
    var totalScore: Long = 0

    var rank: Int = 0

    var level: Float = 0f

    var pp: Float = 0f

    var accuracy: Float = 0f

    // Counts for SS/S/A ranks on maps
    var countRankSS: Int = 0

    var countRankS: Int = 0

    var countRankA: Int = 0

    // Uses the ISO3166-1 alpha-2 country code naming.
    // http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2/wiki/ISO_3166-1_alpha-2)
    var country: String? = null

    // The user's rank in the country.
    var countryRank: Int = 0

    // Contains events for this user
    var events: List<DataEvent>? = null

}

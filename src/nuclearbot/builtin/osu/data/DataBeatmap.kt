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
 * Data container for an osu! beatmap.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class DataBeatmap {

    // 3 = qualified, 2 = approved, 1 = ranked, 0 = pending, -1 = WIP, -2 = graveyard
    var approved: Int = 0

    // date ranked, UTC+8 for now
    var approvedDate: String? = null

    // last update date, timezone same as above. May be after approved_date if map was unranked and reranked.
    var lastUpdate: String? = null

    var artist: String? = null

    // beatmap_id is per difficulty
    var beatmapId: Int = 0

    // beatmapset_id groups difficulties into a set
    var beatmapsetId: Int = 0

    var bpm: Float = 0.toFloat()

    var creator: String? = null

    // The amount of stars the map would have ingame and on the website
    var difficultyRating: Float = 0.toFloat()

    // Circle size varue (CS)
    var diffCS: Float = 0.toFloat()

    // Overall difficulty (OD)
    var diffOD: Float = 0.toFloat()

    // Approach Rate (AR)
    var diffAR: Float = 0.toFloat()

    // Healthdrain (HP)
    var diffHP: Float = 0.toFloat()

    // seconds from first note to last note not including breaks
    var hitLength: Int = 0

    var source: String? = null

    // 0 = any, 1 = unspecified, 2 = video game, 3 = anime, 4 = rock,
    // 5 = pop, 6 = other, 7 = novelty, 9 = hip hop, 10 = electronic
    // (note that there's no 8)
    var genreId: Int = 0

    // 0 = any, 1 = other, 2 = english, 3 = japanese, 4 = chinese,
    // 5 = instrumental, 6 = korean, 7 = french, 8 = german,
    // 9 = swedish, 10 = spanish, 11 = italian
    var languageId: Int = 0

    // song name
    var title: String? = null

    // seconds from first note to last note including breaks
    var totalLength: Int = 0

    // difficulty name
    var version: String? = null

    // md5 hash of the beatmap
    var fileMD5: String? = null

    // game mode
    var mode: Int = 0

    // Beatmap tags separated by spaces.
    private var _tags: String? = null

    val tags: List<String>?
        get() = _tags?.split(' ')?.dropLastWhile(String::isEmpty)

    // Number of times the beatmap was favourited. (americans: notice the ou!)
    var favouriteCount: Int = 0

    // Number of times the beatmap was played
    var playCount: Int = 0

    // Number of times the beatmap was passed, completed (the user didn't fail or retry)
    var passCount: Int = 0

    // The maximum combo a user can reach playing this beatmap.
    var maxCombo: Int = 0

}

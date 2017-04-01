package nuclearbot.builtin.osu.data;

import java.util.Arrays;
import java.util.List;

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
 * Data container for an osu! beatmap.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class DataBeatmap {

    // 3 = qualified, 2 = approved, 1 = ranked, 0 = pending, -1 = WIP, -2 = graveyard
    private int approved;

    // date ranked, UTC+8 for now
    private String approved_date;

    // last update date, timezone same as above. May be after approved_date if map was unranked and reranked.
    private String last_update;

    private String artist;

    // beatmap_id is per difficulty
    private int beatmap_id;

    // beatmapset_id groups difficulties into a set
    private int beatmapset_id;

    private float bpm;

    private String creator;

    // The amount of stars the map would have ingame and on the website
    private float difficultyrating;

    // Circle size value (CS)
    private float diff_size;

    // Overall difficulty (OD)
    private float diff_overall;

    // Approach Rate (AR)
    private float diff_approach;

    // Healthdrain (HP)
    private float diff_drain;

    // seconds from first note to last note not including breaks
    private int hit_length;

    private String source;

    // 0 = any, 1 = unspecified, 2 = video game, 3 = anime, 4 = rock, 5 = pop, 6 = other, 7 = novelty, 9 = hip hop, 10 = electronic (note that there's no 8)
    private int genre_id;

    // 0 = any, 1 = other, 2 = english, 3 = japanese, 4 = chinese, 5 = instrumental, 6 = korean, 7 = french, 8 = german, 9 = swedish, 10 = spanish, 11 = italian
    private int language_id;

    // song name
    private String title;

    // seconds from first note to last note including breaks
    private int total_length;

    // difficulty name
    private String version;

    // md5 hash of the beatmap
    private String file_md5;

    // game mode
    private int mode;

    // Beatmap tags separated by spaces.
    private String tags;

    // Number of times the beatmap was favourited. (americans: notice the ou!)
    private int favourite_count;

    // Number of times the beatmap was played
    private int playcount;

    // Number of times the beatmap was passed, completed (the user didn't fail or retry)
    private int passcount;

    // The maximum combo a user can reach playing this beatmap.
    private int max_combo;

    public int getApproved() {
        return approved;
    }

    public String getApprovedDate() {
        return approved_date;
    }

    public String getLastUpdate() {
        return last_update;
    }

    public String getArtist() {
        return artist;
    }

    public int getBeatmapId() {
        return beatmap_id;
    }

    public int getBeatmapsetId() {
        return beatmapset_id;
    }

    public int getBPM() {
        return Math.round(bpm);
    }

    public String getCreator() {
        return creator;
    }

    public float getDifficultyRating() {
        return difficultyrating;
    }

    public float getDiffCS() {
        return diff_size;
    }

    public float getDiffOD() {
        return diff_overall;
    }

    public float getDiffAR() {
        return diff_approach;
    }

    public float getDiffHP() {
        return diff_drain;
    }

    public int getHitLength() {
        return hit_length;
    }

    public String getSource() {
        return source;
    }

    public int getGenreId() {
        return genre_id;
    }

    public int getLanguageId() {
        return language_id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotalLength() {
        return total_length;
    }

    public String getVersion() {
        return version;
    }

    public String getFileMD5() {
        return file_md5;
    }

    public int getMode() {
        return mode;
    }

    public List<String> getTags() {
        return Arrays.asList(tags.split(" "));
    }

    public int getFavouriteCount() {
        return favourite_count;
    }

    public int getPlayCount() {
        return playcount;
    }

    public int getPassCount() {
        return passcount;
    }

    public int getMaxCombo() {
        return max_combo;
    }

}

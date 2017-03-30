package nuclearbot.builtin.osu.data;

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
 * Data container for an osu! user event.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class DataEvent {

    private String display_html;

    private int beatmap_id;

    private int beatmapset_id;

    private String date;

    // How "epic" this event is (between 1 and 32)
    private int epicfactor;

    public String getDisplayHTML()
    {
        return display_html;
    }

    public int getBeatmapId()
    {
        return beatmap_id;
    }

    public int getBeatmapsetId()
    {
        return beatmapset_id;
    }

    public String getDate()
    {
        return date;
    }

    public int getEpicFactor()
    {
        return epicfactor;
    }

}

package nuclearbot.builtin.osu;

import java.util.List;

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
 * Data container for an osu! user data.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class DataUser {
	
	private int user_id;
	
	private String username;
	
	// Total amount for all ranked and approved beatmaps played
	private int count300;
	
	// Total amount for all ranked and approved beatmaps played
	private int count100;
	
	// Total amount for all ranked and approved beatmaps played
	private int count50;
	
	// Only counts ranked and approved beatmaps
	private int playcount;
	
	// Counts the best individual score on each ranked and approved beatmaps
	private long ranked_score;
	
	// Counts every score on ranked and approved beatmaps
	private long total_score;
	
	private int pp_rank;
	
	private float level;
	
	private float pp_raw;
	
	private float accuracy;
	
	// Counts for SS/S/A ranks on maps
	private int count_rank_ss;
	
	private int count_rank_s;
	
	private int count_rank_a;
	
	// Uses the ISO3166-1 alpha-2 country code naming.
	// http://en.wikipedia.org/wiki/ISO_3166-1_alpha-2/wiki/ISO_3166-1_alpha-2)
	private String country;
	
	// The user's rank in the country.
	private int pp_country_rank;
	
	// Contains events for this user
	private List<DataEvent> events;

	public int getUserId()
	{
		return user_id;
	}

	public String getName()
	{
		return username;
	}

	public int getCount300()
	{
		return count300;
	}

	public int getCount100()
	{
		return count100;
	}

	public int getCount50()
	{
		return count50;
	}

	public int getPlayCount()
	{
		return playcount;
	}

	public long getRankedScore()
	{
		return ranked_score;
	}

	public long getTotalScore()
	{
		return total_score;
	}

	public int getRank()
	{
		return pp_rank;
	}

	public float getLevel()
	{
		return level;
	}

	public float getPP()
	{
		return pp_raw;
	}

	public float getAccuracy()
	{
		return accuracy;
	}

	public int getCountRankSS()
	{
		return count_rank_ss;
	}

	public int getCountRankS()
	{
		return count_rank_s;
	}

	public int getCountRankA()
	{
		return count_rank_a;
	}

	public String getCountry()
	{
		return country;
	}

	public int getCountryRank()
	{
		return pp_country_rank;
	}

	public List<DataEvent> getEvents()
	{
		return events;
	}
	
}

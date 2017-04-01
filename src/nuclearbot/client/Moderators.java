package nuclearbot.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nuclearbot.util.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
 * Static class for moderator list.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class Moderators {

    private static final String FILE_NAME = "moderators.json";

    private static final File file;
    private static final SortedSet<String> moderators;

    static {
        file = new File(FILE_NAME);
        moderators = Collections.synchronizedSortedSet(new TreeSet<>());

        if (!file.exists() && file.mkdirs() && file.delete()) {
            try {
                if (file.createNewFile()) {
                    try (final FileWriter writer = new FileWriter(file, false)) {
                        writer.write("[]");
                    }
                }
            } catch (IOException e) {
                Logger.warning("(mod) Could not create \"" + FILE_NAME + "\" for persistence.");
                Logger.warning("(mod) Moderators will only last one lifetime.");
                Logger.printStackTrace(e);
            }
        }

        loadModerators();
    }

    public static final void loadModerators() {
        try (final FileReader reader = new FileReader(file)) {
            final Type listType = new TypeToken<List<String>>() {
            }.getType();
            final List<String> list = new Gson().fromJson(reader, listType);

            list.forEach(Moderators::addModerator);
        } catch (IOException e) {
            Logger.warning("(mod) Could not load the moderator list.");
            Logger.printStackTrace(e);
        }
    }

    public static final void saveModerators() {
        try (final FileWriter writer = new FileWriter(file)) {
            new Gson().toJson(moderators, List.class, writer);
        } catch (IOException e) {
            Logger.warning("(mod) Could not save the moderator list.");
            Logger.printStackTrace(e);
        }
    }

    public static final void addModerator(final String name) {
        moderators.add(name);
        saveModerators();
    }

    public static final void removeModerator(final String name) {
        moderators.remove(name);
        saveModerators();
    }

    public static final boolean isModerator(final String name) {
        return moderators.contains(name);
    }

    public static final SortedSet<String> getModerators() {
        return Collections.unmodifiableSortedSet(moderators);
    }

}

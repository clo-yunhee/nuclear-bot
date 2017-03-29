package nuclearbot.builtin.osu;

import nuclearbot.util.Logger;
import nuclearbot.util.OSUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
 * Utility to get the currently playing song.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 *
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class OsuNowPlaying {

    public static final String UNKNOWN_OS = "Couldn't determine the operating system.";
    public static final String NOT_RUNNING = "osu! is not running.";
    public static final String NOTHING = "There is no song playing right now.";
    public static final String ERROR = "An error occurred while fetching the song name.";
    public static final String NOW_PLAYING = "Now playing \"%s\".";

    private static final Response parseTitle(final String windowTitle)
    {
        final String[] titleArray = windowTitle
                .split(" - ", 2); // remove the heading 'osu! - ' prefix
        if (titleArray.length == 1)
        {
            return new Response(NOTHING, null);
        }
        else
        {
            return new Response(NOW_PLAYING, titleArray[1]);
        }
    }

    private static final Response getLinux()
    {
        String osuWindowTitle = null;
        try // first, list osu! windows
        {
            final Process windowProcess = Runtime.getRuntime()
                    .exec("xdotool search --classname osu");
            final BufferedReader windowReader = new BufferedReader(
                    new InputStreamReader(windowProcess.getInputStream()));
            boolean foundOsuWindow = false;
            String line;

            int windowId;
            Process titleProcess;
            BufferedReader titleReader;
            String windowTitle;
            while (!foundOsuWindow && (line = windowReader.readLine()) != null)
            {
                windowId = Integer.parseInt(line.trim()); // for each window, check the title
                titleProcess = Runtime.getRuntime().exec("xdotool getwindowname " + windowId);
                titleReader = new BufferedReader(
                        new InputStreamReader(titleProcess.getInputStream()));
                windowTitle = titleReader.readLine().trim();

                titleReader.close();
                // only one should have "osu!" in it (windows should be: X container, .NET container, actual osu! game)
                if (windowTitle.contains("osu!"))
                {
                    osuWindowTitle = windowTitle;
                    foundOsuWindow = true;
                }
            }
            windowReader.close();

            if (osuWindowTitle == null)
            {
                return new Response(NOT_RUNNING, null);
            }
        }
        catch (NumberFormatException e)
        {
            return new Response(NOT_RUNNING, null);
        }
        catch (IOException | IllegalStateException e)
        {
            Logger.error(ERROR);
            Logger.printStackTrace(e);
            return new Response(ERROR, null);
        }

        return parseTitle(osuWindowTitle);
    }

    private static final Response getWindows()
    {
        final String output;
        final StringBuilder builder = new StringBuilder();
        try
        {
            final Process process = Runtime.getRuntime()
                    .exec("tasklist /fo csv /nh /fi \"imagename eq osu!.exe\" /v");
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
                builder.append('\n');
            }
            reader.close();
        }
        catch (IOException e)
        {
            Logger.error(ERROR);
            Logger.printStackTrace(e);
            return new Response(ERROR, null);
        }
        output = builder.toString();

        if (!output.startsWith("\"osu!.exe\""))
        {
            return new Response(NOT_RUNNING, null);
        }

        final String[] columns = output.split(","); // parse csv
        String windowTitle = columns[9];
        windowTitle = windowTitle.substring(1, windowTitle.length() - 2);

        return parseTitle(windowTitle);
    }

    public static final Response getSong()
    {
        switch (OSUtils.getOS())
        {
            case LINUX:
                return getLinux();
            case WINDOWS:
                return getWindows();
            case UNKNOWN:
            default:
                return new Response(UNKNOWN_OS, null);
        }
    }

    public static class Response {

        public final String text;
        public final String rawTitle;

        private Response(final String text, final String raw)
        {
            this.text = text;
            this.rawTitle = raw;
        }

    }

}

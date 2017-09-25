package nuclearbot

import nuclearbot.gui.NuclearBotGUI
import nuclearbot.util.Logger
import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper
import javax.swing.SwingUtilities
import javax.swing.UIManager

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
 * Program entry point.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        Logger::class // run Logger class static init block

        trySetLNF()

        SwingUtilities.invokeLater {
            NuclearBotGUI().open()
        }
    }

    private fun trySetLNF() {
        Logger.info("(GUI) Attempting to use BeautyEye look-and-feel...")
        try {
            BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.generalNoTranslucencyShadow
            BeautyEyeLNFHelper.launchBeautyEyeLNF()
            UIManager.put("RootPane.setupButtonVisible", java.lang.Boolean.FALSE)
        } catch (e: Exception) {
            Logger.warning("(GUI) Exception while setting look-and-feel, falling back to default:")
            Logger.printStackTrace(e)
        }
    }

}

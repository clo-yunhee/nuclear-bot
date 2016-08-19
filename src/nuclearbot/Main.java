package nuclearbot;

import java.io.IOException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

import nuclearbot.gui.ControlPanel;
import nuclearbot.util.Logger;

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
 * Program entry point.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class Main implements Runnable {
	
	public static void main(final String[] args) throws IOException
	{
		Logger.class.getName(); // run Logger class static init block
		
		Logger.info("(GUI) Attempting to use BeautyEye look-and-feel...");
		try
		{
			BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.generalNoTranslucencyShadow;
			BeautyEyeLNFHelper.launchBeautyEyeLNF();
			UIManager.put("RootPane.setupButtonVisible", Boolean.FALSE);
		}
		catch (Exception e)
		{
			Logger.warning("(GUI) Exception while setting look-and-feel, falling back to default:");
			Logger.printStackTrace(e);
		}
		
		SwingUtilities.invokeLater(new Main());
	}
	
	@Override
	public void run()
	{
		new ControlPanel().open();
	}
}

package nuclearbot.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

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
 * A vertical layout manager similar to java.awt.FlowLayout.
 * Like FlowLayout components do not expand to fill available space except when the horizontal alignment
 * is <code>BOTH</code>
 * in which case components are stretched horizontally. Unlike FlowLayout, components will not wrap to form another
 * column if there isn't enough space vertically. VerticalLayout can optionally anchor components to the top or bottom
 * of the display area or center them between the top and bottom.<br>
 * <br>
 * Revision date 12th July 2001<br>
 * <br>
 * @author Colin Mummery  e-mail: colin_mummery@yahoo.com Homepage:www.kagi.com/equitysoft -
 * Based on 'FlexLayout' in Java class libraries Vol 2 Chan/Lee Addison-Wesley 1998
 */
public class VerticalLayout implements LayoutManager {

	/**
	 * Horizontal aligment constant for centering, or center anchoring.
	 */
	public final static int CENTER = 0;
	
	/**
	 * Horizontal alignment constant for right justification.
	 */
	public final static int RIGHT = 1;
	
	/**
	 * Horizontal alignment constant for left justification.
	 */
	public final static int LEFT = 2;
	
	/**
	 * Horizontal alignment constant for stretching horizontally.
	 */
	public final static int BOTH = 3;

	/**
	 * Anchoring constant for anchoring to top.
	 */
	public final static int TOP = 1;
	
	/**
	 * Anchoring constant for anchoring to bottom.
	 */
	public final static int BOTTOM = 2;
	
	// the vertical gap between components, defaults to 5
	private final int vgap;
	
	// LEFT, RIGHT, CENTER or BOTH, how the components are justified
	private final int alignment;
	
	// TOP, BOTTOM or CENTER, where are the components positioned in an overlarge space
	private final int anchor;
	
	/**
	 * Constructs an instance of VerticalLayout with a vertical gap of 5px,
	 * horizontal stretching and anchored to the top of the display area.
	 */
	public VerticalLayout()
	{
		this(5, BOTH, TOP);
	}

	/**
	 * Constructs a VerticalLayout instance with horizontal stretching,
	 * anchored to the top with the specified vertical gap.
	 *
	 * @param vgap the vertical gap between components
	 */
	public VerticalLayout(final int vgap)
	{
		this(vgap, BOTH, TOP);
	}

	/**
	 * Constructs a VerticalLayout instance anchored to top,
	 * with the specified vertical gap and horizontal alignment.
	 *
	 * @param vgap the vertical gap between components
	 * @param alignment an int value of <code>RIGHT, LEFT, CENTER, BOTH</code>
	 */
	public VerticalLayout(final int vgap, final int alignment)
	{
		this(vgap, alignment, TOP);
	}

	/**
	 * Constructs a VerticalLayout instance with the specified
	 * vertical gap, horizontal alignment and anchoring.
	 *
	 * @param vgap the vertical gap between components
	 * @param alignment an int value of <code>RIGHT, LEFT, CENTER, BOTH</code>
	 * @param anchor an int value of <code>TOP, BOTTOM, CENTER</code>
	 */
	public VerticalLayout(final int vgap, final int alignment, final int anchor)
	{
		this.vgap = vgap;
		this.alignment = alignment;
		this.anchor = anchor;
	}

	private Dimension layoutSize(final Container parent, final boolean minimum)
	{
		final Dimension dim = new Dimension(0, 0);
		Dimension d;
		synchronized (parent.getTreeLock())
		{
			final Component[] comps = parent.getComponents();
			final int n = parent.getComponentCount();
			for (int i = 0; i < n; i++)
			{
				final Component c = comps[i];
				if (c.isVisible())
				{
					d = minimum ? c.getMinimumSize() : c.getPreferredSize();
					dim.width = Math.max(dim.width, d.width);
					dim.height += d.height;
					if (i > 0)
						dim.height += vgap;
				}
			}
		}
		final Insets insets = parent.getInsets();
		dim.width += insets.left + insets.right;
		dim.height += insets.top + insets.bottom + vgap + vgap;
		return dim;
	}

	public void layoutContainer(final Container parent)
	{
		final Insets insets = parent.getInsets();
		synchronized (parent.getTreeLock())
		{
			final Component[] comps = parent.getComponents();
			final int n = parent.getComponentCount();
			final Dimension parentDim = parent.getSize();
			int y = 0;
			// work out the total size
			for (int i = 0; i < n; i++)
			{
				final Dimension dim = comps[i].getPreferredSize();
				y += dim.height + vgap;
			}
			y -= vgap; // otherwise there's a vgap too many
			// work out the anchor paint
			if (anchor == TOP)
			{
				y = insets.top;
			}
			else if (anchor == CENTER)
			{
				y = (parentDim.height - y) / 2;
			}
			else
			{
				y = parentDim.height - y - insets.bottom;
			}
			// do layout
			for (int i = 0; i < n; i++)
			{
				final Dimension d = comps[i].getPreferredSize();
				int x = insets.left;
				int width = d.width;
				if (alignment == CENTER)
				{
					x = (parentDim.width - d.width) / 2;
				}
				else if (alignment == RIGHT)
				{
					x = parentDim.width - d.width - insets.right;
				}
				else if (alignment == BOTH)
				{
					width = parentDim.width - insets.left - insets.right;
				}
				comps[i].setBounds(x, y, width, d.height);
				y += d.height + vgap;
			}
		}
	}

	public Dimension minimumLayoutSize(final Container parent)
	{
		return layoutSize(parent, false);
	}

	public Dimension preferredLayoutSize(final Container parent)
	{
		return layoutSize(parent, false);
	}

	public void addLayoutComponent(final String name, final Component comp)
	{
		// Not used.
	}

	public void removeLayoutComponent(final Component comp)
	{
		// Not used.
	}

	public String toString()
	{
		return getClass().getName() + "[vgap=" + vgap + " align=" + alignment + " anchor=" + anchor + "]";
	}
}

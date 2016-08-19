package nuclearbot.gui;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.JList;

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
 * Custom JList that uses a custom model which only keeps the last lines.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class LimitedStringList extends JList<String> {

	private static final long serialVersionUID = 7161165350219517866L;

	private final LimitedStringList.Model m_listModel;
	
	/**
	 * Constructs a LimitedStringList instance
	 * with a default capacity of 50 lines.
	 */
	public LimitedStringList()
	{
		this(50);
	}
	
	/**
	 * Constructs a LimitedStringList instance
	 * with the specified capacity.
	 * @param capacity the maximum number of lines to keep at once
	 */
	public LimitedStringList(final int capacity)
	{
		super();
		m_listModel = new LimitedStringList.Model(capacity);
		setModel(m_listModel);
	}
	
	/**
	 * Adds an element to the model. If the size of the list
	 * will exceed the maximum capacity, the first element
	 * is removed before adding the new element to the end
	 * of the list. 
	 * @param text the line to add to the list
	 */
	public void add(final String text)
	{
		m_listModel.add(text);
	}
	
	/**
	 * The ListModel used by the LimitedStringList class
	 */
	public class Model extends AbstractListModel<String> {

		private static final long serialVersionUID = -188931640082603939L;

		private final ArrayList<String> m_arrayList;
		private final int m_capacity;
		
		/**
		 * Constructs a LimitedStringList.Model instance
		 * with specified element count.
		 * @param capacity the highest number of elements this model can have
		 */
		public Model(final int capacity)
		{
			m_arrayList = new ArrayList<String>(capacity);
			m_capacity = capacity;
		}
		
		/**
		 * Adds an element to the model. If the size of the list
		 * will exceed the maximum capacity, the first element
		 * is removed before adding the new element to the end
		 * of the list. 
		 * @param text the line to add to the list
		 */
		public void add(final String text)
		{
			int index0, index1;
			if (m_arrayList.size() == m_capacity)
			{
				index0 = 0;
				index1 = m_capacity - 1;
				m_arrayList.remove(0);
			}
			else
			{
				index0 = index1 = m_arrayList.size();
			}
			m_arrayList.add(text);
			fireContentsChanged(this, index0, index1);
		}
		
		@Override
		public int getSize()
		{
			return m_arrayList.size();
		}

		@Override
		public String getElementAt(int index)
		{
			return m_arrayList.get(index);
		}
		
	}
	
}

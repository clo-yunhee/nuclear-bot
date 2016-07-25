package nuclearbot.gui;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

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
 * NuclearBot (https://osu.ppy.sh/forum/t/479653)<br>
 * @author NuclearCoder (contact on the forum)<br>
 * <br>
 * Custom OutputStream wrapper that writes output to both OutputStream and Document.
 */
public class DocumentOutputStream extends OutputStream {

	private final Document m_document;
	private final OutputStream m_outputStream;
	
    public DocumentOutputStream(final Document document, final OutputStream outputStream)
    {
        m_outputStream = outputStream;
        m_document = document;
    }

	@Override
	public void close() throws IOException
	{
		m_outputStream.close();
	}

	@Override
	public void flush() throws IOException
	{
		m_outputStream.flush();
	}

	@Override
	public synchronized void write(int b) throws IOException
	{
		write(new byte[] { (byte) b }, 0, 1);
	}

	@Override
	public synchronized void write(byte cbuf[], int off, int len) throws IOException
	{
		m_outputStream.write(cbuf, off, len);
		try
		{
			int start = off;
			int end = start + 1;
			while (end < off + len)
			{
				if (cbuf[end] == '\n')
				{
					m_document.insertString(m_document.getLength(), new String(cbuf, start, end - start), null);
					start = end++;
				}
				++end;
			}
			if (cbuf[start] == '\n')
			{
				m_document.insertString(m_document.getLength(), "\n", null);
			}
			else if (start < off + len)
			{
				m_document.insertString(m_document.getLength(), new String(cbuf, start, len - start - off), null);
			}
		}
		catch (BadLocationException e)
		{
			throw new IOException(e.getMessage());
		}
	}

}

package nuclearbot.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

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
 * Custom OutputStream wrapper that writes output to both OutputStream and Document.<br>
 * <br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br>
 * @author NuclearCoder (contact on the GitHub repo)
 */
public class DocumentOutputStream extends OutputStream {

	private final Document m_document;
	private final OutputStream m_outputStream;
	
	/**
	 * Instantiates a DocumentOutputStream
	 * with the specified document and output stream.
	 * @param document the document to copy output to
	 * @param outputStream the original output stream 
	 */
	public DocumentOutputStream(final Document document, final OutputStream outputStream)
	{
		m_outputStream = outputStream;
		m_document = document;
	}
	
	/**
	 * Changes <code>System.out</code> to also write all output to a Swing Document.
	 * @param document the document to redirect to
	 */
	public synchronized static void redirectSystemOut(final Document document)
	{
		System.setOut(new PrintStream(new DocumentOutputStream(document, System.out), true));
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

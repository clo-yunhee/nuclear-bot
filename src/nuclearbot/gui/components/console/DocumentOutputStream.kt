package nuclearbot.gui.components.console

import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import javax.swing.text.BadLocationException
import javax.swing.text.Document

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
 * Custom OutputStream wrapper that writes output to both OutputStream and Document.<br></br>
 * <br></br>
 * NuclearBot (https://github.com/NuclearCoder/nuclear-bot/)<br></br>

 * @author NuclearCoder (contact on the GitHub repo)
 */
class DocumentOutputStream(private val document: Document, private val outputStream: OutputStream) : OutputStream() {

    companion object {

        /**
         * Changes `System.out` to also write all output to a Swing Document.

         * @param document the document to redirect to
         */
        @Synchronized fun redirectSystemOut(document: Document) {
            System.setOut(PrintStream(DocumentOutputStream(document, System.out), true))
        }
    }

    override fun close() {
        outputStream.close()
    }

    override fun flush() {
        outputStream.flush()
    }

    override fun write(b: Int) {
        write(byteArrayOf(b.toByte()), 0, 1)
    }

    override fun write(buf: ByteArray, off: Int, len: Int) {
        outputStream.write(buf, off, len)
        try {
            var start = off
            var end = start + 1
            while (end < off + len) {
                if (buf[end] == '\n'.toByte()) {
                    document.insertString(document.length, String(buf, start, end - start), null)
                    start = end++
                }
                ++end
            }
            if (buf[start] == '\n'.toByte()) {
                document.insertString(document.length, "\n", null)
            } else if (start < off + len) {
                document.insertString(document.length, String(buf, start, len - start - off), null)
            }
        } catch (e: BadLocationException) {
            throw IOException(e.message)
        }

    }

}

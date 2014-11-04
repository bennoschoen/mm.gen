/**
 * Copyright (C) 2014 Benno Schön
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 01.07.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONException;

/**
 * Writer that writes Objects derived from Streamable to a file or an
 * OutputStream. Files written with this Writer can be read using
 * StreamableIterator.
 * 
 * @author benno
 */
public class StreamableWriter<T extends Streamable> {
	private final ObjectOutputStream oos;
	private final BufferedWriter bw;

	/**
	 * Constructor for writing Streamable Objects to a File.
	 * 
	 * @param f
	 *            File that is used for writing Streamables to. An existing file
	 *            will be overwritten.
	 * @throws IOException
	 */
	public StreamableWriter(final File f) throws IOException {
		this(new FileOutputStream(f));
	}

	/**
	 * Constructor for writing the Streamable Objects to any OutputStream.
	 * 
	 * @param os
	 *            destination for the Streamables.
	 * @throws IOException
	 */
	public StreamableWriter(final OutputStream os) throws IOException {
		ObjectOutputStream tempOos = null;
		BufferedWriter tempBw = null;
		if (Streamable.jsonSupport) {
			tempBw = new BufferedWriter(new OutputStreamWriter(os));
		} else {
			tempOos = new HeaderLessObjectOutput(new BufferedOutputStream(os, 1 << 20));
		}

		this.oos = tempOos;
		this.bw = tempBw;
	}

	/**
	 * writes a single object to the underlaying stream
	 * 
	 * @param streamable
	 *            the object to write to the stream
	 * @throws IOException
	 */
	public void writeStreamable(final T streamable) throws IOException {
		try {
			if (Streamable.jsonSupport) {
				final String json = streamable.toJSON().toString();
				bw.write(json);
				bw.write("\r\n");
			} else {
				streamable.writeExternal(oos);
			}
		} catch (final JSONException e) {
			throw new IOException("Streaming failed", e);
		}
	}

	/**
	 * closes the Stream and all wrapped stream as well.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (Streamable.jsonSupport) {
			bw.close();
		} else {
			oos.close();
		}
	}

	/**
	 * ObjectOutputStream that is not having a stream header.
	 * 
	 * @author benno
	 */
	private class HeaderLessObjectOutput extends ObjectOutputStream {

		/**
		 * @param out
		 * @throws IOException
		 */
		public HeaderLessObjectOutput(final OutputStream out) throws IOException {
			super(out);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.ObjectOutputStream#writeStreamHeader()
		 */
		@Override
		protected void writeStreamHeader() throws IOException {
			// Do not write a header, that the stream is connectable .
		}
	}
}

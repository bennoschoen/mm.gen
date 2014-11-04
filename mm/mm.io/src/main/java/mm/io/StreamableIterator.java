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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import mm.io.base.DecoupledInputStream;

/**
 * This class iterates over a file or an InputStream containing Streamables. It
 * offers a classic Iterator signature to easily consuming the object. When the
 * end of the file or the inputstream is reached it is automatically closed.
 * When there is no need to consume all contained objects the stream can also be
 * closed using the close method.
 * 
 * @author benno
 */
public class StreamableIterator<T extends Streamable> {

	private final ObjectInputStream ois;
	private final BufferedReader br;
	private T next = null;
	private boolean brOpen = true;
	private final StreamableFactory<T> factory;

	/**
	 * Constructor for iterating over file containing Streamables.
	 * 
	 * @param f
	 *            File containing streamable object representations.
	 * @param factory
	 *            Factory constructing the concrete implementation of the
	 *            Streamable
	 * @throws IOException
	 */
	public StreamableIterator(final File f, final StreamableFactory<T> factory) throws IOException {
		this(new FileInputStream(f), factory);
	}

	/**
	 * Constructor for iterating over an InputStream
	 * 
	 * @param in
	 *            InputStream containing streamable object representations.
	 * @param factory
	 *            Factory constructing the concrete implementation of the
	 *            Streamable
	 * @throws IOException
	 */
	public StreamableIterator(final InputStream in, final StreamableFactory<T> factory)
			throws IOException {
		BufferedReader tempBr = null;
		ObjectInputStream tempOis = null;
		this.factory = factory;
		if (Streamable.jsonSupport) {
			tempBr = new BufferedReader(new InputStreamReader(new DecoupledInputStream(in)));
		} else {
			tempOis = new HeaderlessObjectInput(new DecoupledInputStream(in));
		}
		brOpen = true;
		br = tempBr;
		ois = tempOis;
		next = findNext();
	}

	/**
	 * @return true when there is at least one element that can be returned
	 *         calling next
	 */
	public boolean hasNext() {
		return next != null;
	}

	/**
	 * the next element in the stream.
	 * 
	 * @return Null when there are no more elements
	 * @throws IOException
	 */
	public T next() throws IOException {
		final T ret = next;
		next = findNext();
		return ret;
	}

	/**
	 * Finds the next element in the stream.
	 * 
	 * @return null when there are no more elements available
	 * @throws IOException
	 */
	protected T findNext() throws IOException {
		T ret = null;
		String json = null;
		try {
			if (brOpen) {
				if (Streamable.jsonSupport) {
					json = br.readLine();
					if (json == null) {
						br.close();
						brOpen = false;
					} else {
						ret = factory.construct();
						ret.fromJSON(json);
					}
				} else {
					ret = factory.construct();
					try {
						ret.readExternal(ois);
					} catch (final IOException e) {
						ret = null;
					}
					if (ret == null) {
						ois.close();
						brOpen = false;
					}

				}
			}
		} catch (final Exception e) {
			throw new IOException("Error reading next", e);
		}
		return ret;
	}

	/**
	 * Closes the underlaying stream when no more objects are needed. Its not
	 * necessary to call this method when the iterator is called until hasNext
	 * returns false
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		if (Streamable.jsonSupport) {
			br.close();
		} else {
			ois.close();
		}
		brOpen = false;
	}

	/**
	 * ObjectInputStream that reads a stream created by the ObjectOutputStream
	 * within StreamableWriter
	 * 
	 * @author benno
	 */
	private static class HeaderlessObjectInput extends ObjectInputStream {

		/**
		 * @param in
		 * @throws IOException
		 */
		public HeaderlessObjectInput(final InputStream in) throws IOException {
			super(in);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.ObjectInputStream#readStreamHeader()
		 */
		@Override
		protected void readStreamHeader() throws IOException, StreamCorruptedException {
			// StreamableWriter is not writing an ObjectInputStream Header
		}
	}
}

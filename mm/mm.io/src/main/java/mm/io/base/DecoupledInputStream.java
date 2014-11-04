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
 * @since 08.10.2014
 * @version 1.0
 * @author benno
 */
package mm.io.base;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * This inputStream uses a second thread for reading the data from the source
 * input stream. When there is a blocking device behind this thread, the user of
 * the DecoupledInputstream might not been blocked, because it can read data out
 * of the buffer. Only when buffer is empty it must wait until the reader thread
 * has gathered enough data for continuing.
 * 
 * @author benno
 */
public class DecoupledInputStream extends InputStream {

	private final PipedInputStream in;
	private final Thread readerThread;
	private final InputStream source;
	private static int defaultBufferSize = 1 << 20;
	private static int defaultReadBufferSize = 1 << 16;

	public DecoupledInputStream(final InputStream source) throws IOException {
		super();
		this.source = source;
		in = new PipedInputStream(defaultBufferSize);
		readerThread = new Thread(new SourceReader(source, new PipedOutputStream(in)));
		readerThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		return in.read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return in.read(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		return in.read(b, off, len);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(final long n) throws IOException {
		return in.skip(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available() throws IOException {
		return in.available();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#close()
	 */
	@Override
	public void close() throws IOException {
		synchronized (source) {
			source.close();
		}
		in.close();
		readerThread.interrupt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#mark(int)
	 */
	@Override
	public synchronized void mark(final int readlimit) {
		in.mark(readlimit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#reset()
	 */
	@Override
	public synchronized void reset() throws IOException {
		in.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {
		return in.markSupported();
	}

	private static class SourceReader implements Runnable {
		private final PipedOutputStream dest;
		private final InputStream source;

		public SourceReader(final InputStream source, final PipedOutputStream dest) {
			super();
			this.source = source;
			this.dest = dest;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			final byte buffer[] = new byte[defaultReadBufferSize];
			try {
				int got;
				synchronized (source) {
					got = source.read(buffer);
				}
				while (got > 0) {
					dest.write(buffer, 0, got);
					synchronized (source) {
						got = source.read(buffer);
					}
				}
			} catch (final IOException e) {
			} finally {
				try {
					dest.close();
				} catch (final Throwable e1) {
				}
			}
		}
	}
}

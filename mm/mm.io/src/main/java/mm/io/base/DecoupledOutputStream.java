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
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author benno
 * 
 */
public class DecoupledOutputStream extends OutputStream {

	private static int defaultBufferSize = 1 << 20;
	private static int defaultReadBufferSize = 1 << 16;
	private final Thread readerThread;
	private final PipedOutputStream out;
	private final OutputStream dest;
	private final AtomicInteger bufferSize = new AtomicInteger(0);

	public DecoupledOutputStream(final OutputStream dest) throws IOException {
		super();
		this.dest = dest;

		out = new PipedOutputStream();
		readerThread = new Thread(new SourceReader(new PipedInputStream(out, defaultBufferSize),
				dest));
		readerThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(int)
	 */
	@Override
	public void write(final int b) throws IOException {
		bufferSize.addAndGet(1);
		try {
			out.write(b);
		} catch (final Exception e) {
			bufferSize.set(0);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[])
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		bufferSize.addAndGet(b.length);
		try {
			out.write(b);
		} catch (final Exception e) {
			bufferSize.set(0);
			throw e;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		bufferSize.addAndGet(len);
		try {
			out.write(b, off, len);
		} catch (final Exception e) {
			bufferSize.set(0);
			throw e;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.OutputStream#close()
	 */
	@Override
	public void close() throws IOException {
		int retry = 1;
		final int maxRetry = 200;
		while (bufferSize.get() > 0 && retry < maxRetry) {
			out.flush();
			try {
				Thread.sleep(10 * retry);
			} catch (final Exception e) {
			}
			retry++;
		}
		out.close();
		synchronized (dest) {

			dest.close();
		}
		readerThread.interrupt();
		if (retry >= maxRetry) {
			throw new IOException("Stream didn't close intime");
		}
	}

	private class SourceReader implements Runnable {
		private final OutputStream dest;
		private final PipedInputStream source;

		public SourceReader(final PipedInputStream source, final OutputStream dest) {
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
				int got = source.read(buffer);
				bufferSize.addAndGet(got > 0 ? -got : 0);
				while (got > 0) {
					synchronized (dest) {
						dest.write(buffer, 0, got);
					}
					got = source.read(buffer);
					bufferSize.addAndGet(got > 0 ? -got : 0);
				}
			} catch (final IOException e) {
			} finally {
				try {
					source.close();
					bufferSize.set(0);
				} catch (final Throwable e1) {
				}
			}
		}
	}
}

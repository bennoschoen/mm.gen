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
 * @since 12.09.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * This class extends the RandomAccessFile of the package java.io by using
 * buffers for read and write operations.
 * 
 * @author benno
 */
public class RandomAccessFile extends java.io.RandomAccessFile {

	private long position = 0;
	private static final int quotient = 14;
	private static final int PageSize = 1 << quotient;
	private static final int PageNumber = 1024;
	private long fileLength = 0;
	@SuppressWarnings("serial")
	private final LinkedHashMap<Long, Page> pageMap = new LinkedHashMap<Long, Page>(16, 0.75f, true) {
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
		 */
		@Override
		protected boolean removeEldestEntry(final Entry<Long, Page> eldest) {
			if (size() > PageNumber) {
				try {
					final Page page = eldest.getValue();
					if (page.isDirty()) {
						page.flushToFile();
					}
				} catch (final Exception e) {
				}
				return true;
			}
			return false;
		}
	};

	/**
	 * Overwrites the constructor of the super class. This constructor
	 * initializes the cache automatically.
	 * 
	 * @param name
	 *            name of the file
	 * @param mode
	 *            mode in which the file shall be opened
	 * @throws IOException
	 */
	public RandomAccessFile(final String name, final String mode) throws IOException {
		this(new File(name), mode);
	}

	/**
	 * Overwrites the constructor of the super class. This constructor
	 * initializes the cache automatically.
	 * 
	 * @param file
	 *            file to create or use if exists
	 * @param mode
	 *            mode in which the file shall be opened
	 * @throws IOException
	 */
	public RandomAccessFile(final File file, final String mode) throws IOException {
		super(file, mode);
		position = 0;
		fileLength = super.length();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#read()
	 */
	@Override
	public int read() throws IOException {
		final Page page = getCurrentPage();
		final int value = page.read(position);
		if (value >= 0) {
			position++;
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#read(byte[])
	 */
	@Override
	public int read(final byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#read(byte[], int, int)
	 */
	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		Page page = getCurrentPage();
		int got = page.read(position, b, off, len);
		int total = got;
		while (got > 0) {
			position += got;
			page = getCurrentPage();
			got = page.read(position, b, off + total, len - total);
			total += got > 0 ? got : 0;
		}
		return total;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#skipBytes(int)
	 */
	@Override
	public int skipBytes(final int n) throws IOException {
		int skip;
		if (position + n <= fileLength) {
			skip = n;
		} else {
			skip = (int) (fileLength - position);
		}
		position += skip;
		return skip;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#write(int)
	 */
	@Override
	public void write(final int b) throws IOException {
		final Page page = getCurrentPage();
		if (page.write(position, b) >= 0) {
			position++;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#write(byte[])
	 */
	@Override
	public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#write(byte[], int, int)
	 */
	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		Page page = getCurrentPage();
		int written = page.write(position, b, off, len);
		int totalWritten = 0;
		while (written > 0) {
			totalWritten += written;
			position += written;
			page = getCurrentPage();
			written = page.write(position, b, off + totalWritten, len - totalWritten);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#getFilePointer()
	 */
	@Override
	public long getFilePointer() throws IOException {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#seek(long)
	 */
	@Override
	public void seek(final long pos) throws IOException {
		position = pos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#length()
	 */
	@Override
	public long length() throws IOException {
		return fileLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#setLength(long)
	 */
	@Override
	public void setLength(final long newLength) throws IOException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#close()
	 */
	@Override
	public void close() throws IOException {
		for (final Entry<Long, Page> set : pageMap.entrySet()) {
			set.getValue().flushToFile();
		}
		super.close();
	}

	/**
	 * @return the page the current file position lays in.
	 * @throws IOException
	 */
	private Page getCurrentPage() throws IOException {
		final long pageId = position >> quotient;
		Page page = pageMap.get(Long.valueOf(pageId));
		if (page == null) {
			page = new Page();
			page.setStartPos(pageId << quotient);
			page.readFromFile();
			pageMap.put(Long.valueOf(pageId), page);
		}
		return page;
	}

	/**
	 * flushes a byte buffer back to the underlying file system.
	 * 
	 * @param position
	 *            position where the buffer starts in the file
	 * @param buffer
	 *            the byte buffer to flush
	 * @param length
	 *            the length of data within buffer that shall be flushed
	 * @throws IOException
	 */
	private void flushToFile0(final long position, final byte buffer[], final int length)
			throws IOException {
		super.seek(position);
		super.write(buffer, 0, length);
	}

	/**
	 * reads as many data into the buffer that is available in the file starting
	 * from the position
	 * 
	 * @param position
	 *            the point where the underlying RandomAccessFile shall start to
	 *            read
	 * @param buffer
	 *            the buffer the data shall be copied to
	 * @return the number of bytes read
	 * @throws IOException
	 */
	private int readFromFile0(final long position, final byte buffer[]) throws IOException {
		super.seek(position);
		int got = super.read(buffer);
		int count = 0;
		while (got > 0) {
			count += got;
			got = super.read(buffer, count, buffer.length - count);
		}
		return count;
	}

	/**
	 * This represents a Page of data on the file system. It is not needed, that
	 * the Page size must correspond with any data size that is stored in this
	 * RandomAccessFile. The Page keeps track, whether it was written to it, but
	 * there is no compare done. Whenever one of the write methods is called, it
	 * is marked as dirty.
	 * 
	 * @author benno
	 */
	protected class Page {
		private final byte buffer[] = new byte[PageSize];
		private long startPos = 0;
		private boolean dirty = false;
		private int length = PageSize;

		/**
		 * Reads a portion of data from this page.
		 * 
		 * @param currentPos
		 *            the position where to read from relative to the start of
		 *            the file, not the page!
		 * @param b
		 *            the buffer where the data shall be copied to
		 * @param off
		 *            offset within the buffer.
		 * @param len
		 *            number of data to read starting at the offset
		 * @return number of bytes read, or -1 when EOF is reached.
		 */
		public int read(final long currentPos, final byte b[], final int off, final int len) {
			final int pageOffset = (int) (currentPos % PageSize);
			int bytesToRead = Math.min(b.length, off + len) - off;
			if (bytesToRead > 0) {
				bytesToRead = Math.min(length, pageOffset + bytesToRead) - pageOffset;
				if (bytesToRead > 0) {
					System.arraycopy(buffer, pageOffset, b, off, bytesToRead);
				} else if (currentPos >= fileLength) {
					bytesToRead = -1;
				}
			}
			return bytesToRead;
		}

		/**
		 * reads one byte from the page
		 * 
		 * @param currentPos
		 *            the position where to read from relative to the start of
		 *            the file, not the page!
		 * @return the byte that was read or any negative value when EOF is
		 *         reached.
		 */
		public int read(final long currentPos) {
			final int pageOffset = (int) (currentPos % PageSize);
			if (pageOffset < length) {
				return buffer[pageOffset] & 0xff;
			}
			return -1;
		}

		/**
		 * writes as many data to this page as fits into the pages boundaries.
		 * 
		 * @param currentPos
		 *            the position where to write from relative to the start of
		 *            the file, not the page!
		 * @param b
		 *            the buffer where the data shall be copied from
		 * @param off
		 *            offset within the buffer.
		 * @param len
		 *            number of data to write starting at the offset
		 * @return number of bytes written
		 */
		public int write(final long currentPos, final byte b[], final int off, final int len) {
			final int pageOffset = (int) (currentPos % PageSize);
			int bytesToWrite = Math.min(b.length, off + len) - off;
			if (bytesToWrite > 0) {
				bytesToWrite = Math.min(PageSize, pageOffset + bytesToWrite) - pageOffset;
				if (bytesToWrite > 0) {
					System.arraycopy(b, off, buffer, pageOffset, bytesToWrite);
					length = Math.max(length, pageOffset + bytesToWrite);
					fileLength = Math.max(fileLength, currentPos + bytesToWrite);
					dirty = true;
				}
			}
			return bytesToWrite;
		}

		/**
		 * writes a single byte to the page
		 * 
		 * @param currentPos
		 *            the position where to write from relative to the start of
		 *            the file, not the page!
		 * @param b
		 *            the byte to write
		 * @return -1 when position is not within this page
		 */
		public int write(final long currentPos, final int b) {
			final int pageOffset = (int) (currentPos % PageSize);
			if (pageOffset < PageSize) {
				dirty = true;
				length = Math.max(length, pageOffset + 1);
				fileLength = Math.max(fileLength, currentPos + 1);
				buffer[pageOffset] = (byte) (b & 0xff);
				return 1;
			}
			return -1;
		}

		/**
		 * @return the startPos
		 */
		public long getStartPos() {
			return startPos;
		}

		/**
		 * @param startPos
		 *            the startPos to set
		 */
		public void setStartPos(final long startPos) {
			this.startPos = startPos;
		}

		/**
		 * @return the dirty
		 */
		public boolean isDirty() {
			return dirty;
		}

		/**
		 * flushes this page back to FileSystem
		 * 
		 * @throws IOException
		 */
		public void flushToFile() throws IOException {
			flushToFile0(startPos, buffer, length);
			dirty = false;
		}

		/**
		 * reads this page from FileSystem
		 * 
		 * @throws IOException
		 */
		public void readFromFile() throws IOException {
			length = readFromFile0(startPos, buffer);
		}

		/**
		 * resets the Page to its original state
		 */
		public void reset() {
			dirty = false;
			position = 0;
		}
	}
}

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
 * @since 13.09.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import mm.io.RandomAccessFile;

import org.junit.Test;

/**
 * @author benno
 * 
 */
public class RandomAccessFileTestCase {

	@Test
	public void testWrite() throws IOException {
		final long numberOfWrites = 8000;
		final File f = File.createTempFile("random", "test");
		final RandomAccessFile raf = new RandomAccessFile(f, "rw");

		for (long i = 0; i < numberOfWrites; ++i) {
			raf.writeLong(i);
		}
		raf.close();
		final java.io.RandomAccessFile testRaf = new java.io.RandomAccessFile(f, "r");
		for (long i = 0; i < numberOfWrites; ++i) {
			final long red = testRaf.readLong();
			assertEquals(i, red);
		}
		testRaf.close();
	}

	@Test
	public void testWriteBuffer() throws IOException {
		final long numberOfWrites = 8000;
		final ByteBuffer buffer = ByteBuffer.allocate((int) numberOfWrites * 8);
		final File f = File.createTempFile("random", "test");
		final RandomAccessFile raf = new RandomAccessFile(f, "rw");

		for (long i = 0; i < numberOfWrites; ++i) {
			buffer.putLong(i);
		}
		raf.write(buffer.array());
		raf.close();
		final java.io.RandomAccessFile testRaf = new java.io.RandomAccessFile(f, "r");
		for (long i = 0; i < numberOfWrites; ++i) {
			final long red = testRaf.readLong();
			assertEquals(i, red);
		}
		testRaf.close();
	}
}

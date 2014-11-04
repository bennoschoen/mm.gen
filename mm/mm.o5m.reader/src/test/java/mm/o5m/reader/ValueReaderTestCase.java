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
 * @since 08.09.2014
 * @version 1.0
 * @author benno
 */
package mm.o5m.reader;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

/**
 * @author benno
 * 
 */
public class ValueReaderTestCase {

	/**
	 * Test method for
	 * {@link mm.o5m.reader.O5mReader#readUnsigned32(java.nio.ByteBuffer)}
	 * .
	 */
	@Test
	public void testReadUnsigned32() {
		final O5mReader reader = new O5mReader();
		assertEquals(5, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { 0x05 })));
		assertEquals(127, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { 0x7f })));
		assertEquals(323, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { (byte) 0xc3, 0x02 })));
		assertEquals(16384, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { (byte) 0x80,
				(byte) 0x80, 0x01 })));

	}

	/**
	 * Test method for
	 * {@link mm.o5m.reader.O5mReader#readSigned32(java.nio.ByteBuffer)}
	 * .
	 */
	@Test
	public void testReadSigned32() {
		final O5mReader reader = new O5mReader();
		assertEquals(4, reader.readSigned32(ByteBuffer.wrap(new byte[] { 0x08 })));
		assertEquals(64, reader.readSigned32(ByteBuffer.wrap(new byte[] { (byte) 0x80, 0x01 })));
		assertEquals(-2, reader.readSigned32(ByteBuffer.wrap(new byte[] { 0x03 })));
		assertEquals(-3, reader.readSigned32(ByteBuffer.wrap(new byte[] { 0x05 })));
		assertEquals(-65, reader.readSigned32(ByteBuffer.wrap(new byte[] { (byte) 0x81, 0x01 })));
	}

	/**
	 * Test method for
	 * {@link mm.o5m.reader.O5mReader#readUnsigned64(java.nio.ByteBuffer)}
	 * .
	 */
	@Test
	public void testReadUnsigned64() {
		final O5mReader reader = new O5mReader();
		assertEquals(5, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { 0x05 })));
		assertEquals(127, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { 0x7f })));
		assertEquals(323, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { (byte) 0xc3, 0x02 })));
		assertEquals(16384, reader.readUnsigned32(ByteBuffer.wrap(new byte[] { (byte) 0x80,
				(byte) 0x80, 0x01 })));
	}

	/**
	 * Test method for
	 * {@link mm.o5m.reader.O5mReader#readSigned64(java.nio.ByteBuffer)}
	 * .
	 */
	@Test
	public void testReadSigned64() {
		final O5mReader reader = new O5mReader();
		assertEquals(4, reader.readSigned64(ByteBuffer.wrap(new byte[] { 0x08 })));
		assertEquals(64, reader.readSigned64(ByteBuffer.wrap(new byte[] { (byte) 0x80, 0x01 })));
		assertEquals(-2, reader.readSigned64(ByteBuffer.wrap(new byte[] { 0x03 })));
		assertEquals(-3, reader.readSigned64(ByteBuffer.wrap(new byte[] { 0x05 })));
		assertEquals(-65, reader.readSigned64(ByteBuffer.wrap(new byte[] { (byte) 0x81, 0x01 })));
	}

	/**
	 * Test method for
	 * {@link mm.o5m.reader.O5mReader#readStringPair(java.nio.ByteBuffer)}
	 * .
	 */
	@Test
	public void testStringReading() throws IOException {
		final byte b[] = new byte[] { 0x00, 0x6f, 0x6e, 0x65, 0x77, 0x61, 0x79, 0x00, 0x79, 0x65,
				0x73, 0x00, 0x00, 0x61, 0x74, 0x6d, 0x00, 0x6e, 0x6f, 0x00, 0x02, 0x00, 0x6f, 0x6e,
				0x65, 0x77, 0x61, 0x79, 0x00, 0x6e, 0x6f, 0x00, 0x02, 0x03, 0x01 };
		final ByteBuffer buffer = ByteBuffer.wrap(b);
		final O5mReader reader = new O5mReader();
		String pair[] = reader.readStringPair(buffer);
		assertEquals(pair[0], "oneway");
		assertEquals(pair[1], "yes");
		pair = reader.readStringPair(buffer);
		assertEquals(pair[0], "atm");
		assertEquals(pair[1], "no");
		pair = reader.readStringPair(buffer);
		assertEquals(pair[0], "oneway");
		assertEquals(pair[1], "yes");
		pair = reader.readStringPair(buffer);
		assertEquals(pair[0], "oneway");
		assertEquals(pair[1], "no");
		pair = reader.readStringPair(buffer);
		assertEquals(pair[0], "atm");
		assertEquals(pair[1], "no");
		pair = reader.readStringPair(buffer);
		assertEquals(pair[0], "oneway");
		assertEquals(pair[1], "yes");
		pair = reader.readStringPair(buffer);
		assertEquals(pair[0], "oneway");
		assertEquals(pair[1], "no");
	}

	/**
	 * Test method for
	 * {@link mm.o5m.reader.O5mReader#readAuthor(java.nio.ByteBuffer)}
	 * .
	 */
	@Test
	public void testAuthorReading() throws IOException {
		final byte b[] = new byte[] { 0x00, (byte) 0xfc, 0x07, 0x00, 0x4a, 0x6f, 0x68, 0x4e, 0x00 };
		final ByteBuffer buffer = ByteBuffer.wrap(b);
		final O5mReader reader = new O5mReader();
		final String pair[] = reader.readAuthor(buffer);
		assertEquals(pair[0], "1020");
		assertEquals(pair[1], "JohN");
	}

}

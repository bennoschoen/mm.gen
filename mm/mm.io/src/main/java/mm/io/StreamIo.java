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
 * @since 03.07.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author benno
 * 
 */
public final class StreamIo {

	private StreamIo() {

	}

	/**
	 * Writes a String to an ObjectOutput
	 * 
	 * @param out
	 *            the destination for the string
	 * @param str
	 *            the string that shall be stored
	 * @throws IOException
	 */
	public static void writeString(final ObjectOutput out, final String str) throws IOException {
		final byte b[] = str.getBytes();
		out.writeInt(b.length);
		out.write(b);
	}

	/**
	 * opposite function for write string. This method reads a string that was
	 * written with writeString
	 * 
	 * @param in
	 *            source from which the string will be read.
	 * @return the string
	 * @throws IOException
	 */
	public static String readString(final ObjectInput in) throws IOException {
		final int length = in.readInt();
		final byte b[] = new byte[length];
		in.readFully(b);
		return new String(b);
	}
}

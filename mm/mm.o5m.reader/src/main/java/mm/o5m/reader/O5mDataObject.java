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
 * @since 14.09.2014
 * @version 1.0
 * @author benno
 */
package mm.o5m.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * @author benno
 * 
 */
public class O5mDataObject {
	private int version;
	private int timeStamp;
	private int lastTimeStamp;
	private int changeSet;
	private int lastChangeSet;
	private String author;
	private String uid;
	protected HashMap<String, String> props = new HashMap<>();

	public void readFromBuffer(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		props.clear();
	}

	public void readUidAuthorTSFromBuffer(final ByteBuffer buffer, final O5mReader reader)
			throws IOException {
		version = reader.readUnsigned32(buffer);
		if (version != 0) {
			lastTimeStamp = timeStamp = lastTimeStamp + reader.readSigned32(buffer);
			if (timeStamp != 0) {
				lastChangeSet = changeSet = reader.readSigned32(buffer) + lastChangeSet;
				final String author[] = reader.readAuthor(buffer);
				uid = author[0];
				this.author = author[1];
			}
		}
	}

	public void readProps(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		while (buffer.hasRemaining()) {
			final String prop[] = reader.readStringPair(buffer);
			final String key = prop[0];
			final String val = prop[1];
			if (prop[1] == null) {
				System.out.println();
			}
			props.put(key, val);
		}
	}

	public void reset() {
		lastTimeStamp = 0;
		lastChangeSet = 0;
	}

	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return the timeStamp
	 */
	public long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * @return the changeSet
	 */
	public int getChangeSet() {
		return changeSet;
	}

	/**
	 * @return the author
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @return the props
	 */
	public HashMap<String, String> getProps() {
		return props;
	}
}

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

/**
 * @author benno
 * 
 */
public class O5mNode extends O5mDataObject {

	private long lastNodeId;
	private int lastLat;
	private int lastLon;
	private long nodeId;
	private int lat;
	private int lon;

	@Override
	public void readFromBuffer(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		super.readFromBuffer(buffer, reader);
		lastNodeId = nodeId = lastNodeId + reader.readSigned64(buffer);
		if (buffer.hasRemaining()) {
			super.readUidAuthorTSFromBuffer(buffer, reader);
		} else {
			return;
		}
		if (buffer.hasRemaining()) {
			lastLon = lon = lastLon + reader.readSigned32(buffer);
			lastLat = lat = lastLat + reader.readSigned32(buffer);
		} else {
			return;
		}
		super.readProps(buffer, reader);
	}

	@Override
	public void reset() {
		lastNodeId = 0;
		lastLat = 0;
		lastLon = 0;
		super.reset();
	}

	/**
	 * @return the nodeId
	 */
	public long getNodeId() {
		return nodeId;
	}

	/**
	 * @return the lat
	 */
	public int getLat() {
		return lat;
	}

	/**
	 * @return the lon
	 */
	public int getLon() {
		return lon;
	}
}

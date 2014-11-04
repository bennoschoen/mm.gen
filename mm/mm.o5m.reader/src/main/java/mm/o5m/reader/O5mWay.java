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
 * @since 16.09.2014
 * @version 1.0
 * @author benno
 */
package mm.o5m.reader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author benno
 * 
 */
public class O5mWay extends O5mDataObject {

	private long lastWayId;
	private long wayId;
	private long lastReference;
	private final List<Long> refs = new ArrayList<>();

	@Override
	public void readFromBuffer(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		super.readFromBuffer(buffer, reader);
		refs.clear();
		lastWayId = wayId = lastWayId + reader.readSigned64(buffer);
		if (buffer.hasRemaining()) {
			super.readUidAuthorTSFromBuffer(buffer, reader);
		} else {
			return;
		}
		if (buffer.hasRemaining()) {
			final int sizeOfRefs = reader.readUnsigned32(buffer);
			final int refstart = buffer.position();
			while (buffer.position() < sizeOfRefs + refstart) {
				lastReference += reader.readSigned64(buffer);
				refs.add(Long.valueOf(lastReference));
			}
		} else {
			return;
		}
		super.readProps(buffer, reader);
	}

	@Override
	public void reset() {
		lastWayId = 0;
		lastReference = 0;
		refs.clear();
		super.reset();
	}

	/**
	 * @return the wayId
	 */
	public long getWayId() {
		return wayId;
	}

	/**
	 * @return the refs
	 */
	public List<Long> getRefs() {
		return refs;
	}
}

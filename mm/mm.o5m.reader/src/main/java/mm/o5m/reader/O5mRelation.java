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
public class O5mRelation extends O5mDataObject {

	private long lastRelationId;
	private long relationId;
	private final long lastReference[] = new long[RefType.MAX.ordinal()];
	private final List<Reference> refs = new ArrayList<>();

	@Override
	public void readFromBuffer(final ByteBuffer buffer, final O5mReader reader) throws IOException {
		super.readFromBuffer(buffer, reader);
		refs.clear();
		lastRelationId = relationId = lastRelationId + reader.readSigned64(buffer);
		if (buffer.hasRemaining()) {
			super.readUidAuthorTSFromBuffer(buffer, reader);
		} else {
			return;
		}
		if (buffer.hasRemaining()) {
			final int sizeOfRefs = reader.readUnsigned32(buffer);
			final int refstart = buffer.position();
			while (buffer.position() < sizeOfRefs + refstart) {
				refs.add(new Reference(buffer, reader));
			}
		} else {
			return;
		}
		super.readProps(buffer, reader);
	}

	@Override
	public void reset() {
		lastRelationId = 0;
		relationId = 0;
		for (int i = 0; i < lastReference.length; ++i) {
			lastReference[i] = 0;
		}
		refs.clear();
		super.reset();
	}

	/**
	 * @return the wayId
	 */
	public long getRelationId() {
		return relationId;
	}

	/**
	 * @return the refs
	 */
	public List<Reference> getRefs() {
		return refs;
	}

	public class Reference {
		private final long id;
		private final RefType type;
		private final String role;

		public Reference(final ByteBuffer buffer, final O5mReader reader) throws IOException {
			final long deltaId = reader.readSigned64(buffer);
			final String rawRole = reader.readSingleString(buffer);
			final int rawType = rawRole.charAt(0) - 0x30;
			if (rawType >= 0 && rawType < RefType.MAX.ordinal()) {
				type = RefType.values()[rawType];
			} else {
				type = RefType.MAX;
			}
			id = lastReference[type.ordinal()] += deltaId;
			role = rawRole.substring(1);
		}

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @return the type
		 */
		public RefType getType() {
			return type;
		}

		/**
		 * @return the role
		 */
		public String getRole() {
			return role;
		}
	}

	public enum RefType {
		NODE, WAY, RELATION, MAX
	}
}

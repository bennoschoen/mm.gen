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
 * @since 23.02.2014
 * @version 1.0
 * @author benno
 */
package mm.lay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author benno
 * 
 */
public class CellTable {

	protected Set<CellPointer> pointers = new TreeSet<CellPointer>();
	protected final File f;

	public class CellPointer implements Comparable<CellPointer> {
		protected final int id;
		protected final int startPointer;
		protected final int length;

		public CellPointer(final int id, final int startPointer, final int length) {
			super();
			this.id = id;
			this.startPointer = startPointer;
			this.length = length;
		}

		/**
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * @return the startPointer
		 */
		public int getStartPointer() {
			return startPointer;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(final CellPointer o) {
			return Integer.valueOf(id).compareTo(Integer.valueOf(o.getId()));
		}
	}

	public CellTable(final File f) {
		this.f = f;
	}

	public void writeTable() throws IOException {
		final FileOutputStream fos = new FileOutputStream(f);
		final byte b[] = new byte[12];
		final ByteBuffer bb = ByteBuffer.wrap(b);
		bb.order(ByteOrder.LITTLE_ENDIAN);

		for (final CellPointer cellPointer : pointers) {
			bb.position(0);
			bb.putInt(cellPointer.getId());
			bb.putInt(cellPointer.getStartPointer());
			bb.putInt(cellPointer.getLength());
			fos.write(b);
		}
		fos.close();
	}

	/**
	 * @return the pointers
	 */
	public Set<CellPointer> getCellPointers() {
		return pointers;
	}

	public int getMaxCellLength() {
		int maxLength = 0;
		for (final CellPointer cellPointer : pointers) {
			maxLength = Math.max(maxLength, cellPointer.getLength());
		}
		return maxLength;
	}

	public void addCellPoint(final int id, final int offset, final int length) {
		pointers.add(new CellPointer(id, offset, length));
	}
}

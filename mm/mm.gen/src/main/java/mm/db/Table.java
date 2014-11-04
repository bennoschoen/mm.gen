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
 * @since 10.02.2014
 * @version 1.0
 * @author benno
 */
package mm.db;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author benno
 * 
 */
public class Table {
	protected final File f;
	protected final FieldType[] fields;
	protected final String names[];
	protected final String name;

	protected final int recordLength;
	protected final int alignedRecordLength;
	protected int numberOfRecords = 0;
	protected static final int pageSize = 512;
	protected static final int stringLength = 248;
	protected final RandomAccessFile rao;
	protected int numberOfPages = 0;
	protected final ByteOrder byteOrder = ByteOrder.LITTLE_ENDIAN;
	protected final int pageOffset; // Offset at the beginning of each page
	protected int recordsPerPage = 0; // Number of rows in a page
	protected List<Page> pageCache = new ArrayList<Table.Page>();

	public enum FieldType {
		BYTE(1), SHORT(2), INTEGER(4), INT_ARRAY(156), SHORT_STR(25), STRING(248);

		private final int length;

		private FieldType(final int length) {
			this.length = length;
		}

		/**
		 * @return the length
		 */
		public int getLength() {
			return length;
		}
	}

	protected class Page {
		protected final int pageNumber;
		protected final byte b[];
		protected final ByteBuffer buffer;

		public Page(final int pageNumber, final byte b[]) {
			super();
			this.pageNumber = pageNumber;
			this.b = b;
			buffer = ByteBuffer.wrap(b);
			buffer.order(byteOrder);
		}

		/**
		 * @return the pageNumber
		 */
		public int getPageNumber() {
			return pageNumber;
		}

		/**
		 * @return the b
		 */
		public byte[] getB() {
			return b;
		}

		public void writeByte(final int pos, final byte b) {
			if (pos + 1 > pageSize) {
				throw new IllegalArgumentException("You try to write out of the page");
			}
			buffer.put(pos, b);
		}

		public byte readByte(final int pos) {
			if (pos + 1 > pageSize) {
				throw new IllegalArgumentException("You try to read out of the page");
			}
			return buffer.get(pos);
		}

		public void writeShort(final int pos, final short s) {
			if (pos + 2 > pageSize) {
				throw new IllegalArgumentException("You try to write out of the page");
			}
			buffer.putShort(pos, s);
		}

		public short readShort(final int pos) {
			if (pos + 2 > pageSize) {
				throw new IllegalArgumentException("You try to read out of the page");
			}
			return buffer.getShort(pos);
		}

		public void writeInt(final int pos, final int i) {
			if (pos + 4 > pageSize) {
				throw new IllegalArgumentException("You try to write out of the page");
			}
			buffer.putInt(pos, i);
		}

		public int readInt(final int pos) {
			if (pos + 4 > pageSize) {
				throw new IllegalArgumentException("You try to read out of the page");
			}
			return buffer.getInt(pos);
		}

		public void writeString(final int pos, final byte str[]) {
			final byte bb[] = new byte[stringLength];
			if (str.length > stringLength) {
				throw new IllegalArgumentException("String must have a fixed size");
			}
			for (int i = 0; i < bb.length; i++) {
				if (i < str.length) {
					bb[i] = str[i];
				} else {
					bb[i] = 0;
				}
			}
			buffer.position(pos);
			buffer.put(str);
		}

		public byte[] readString(final int pos) {
			final byte bb[] = new byte[stringLength];
			if (pos + stringLength > pageSize) {
				throw new IllegalArgumentException("You try to read out of the page");
			}
			buffer.position(pos);
			buffer.get(bb);
			return bb;
		}

		public void flush() {
			try {
				buffer.position(0);
				buffer.putInt((int) (System.currentTimeMillis() / 1000));
				rao.seek((pageNumber + 1) * pageSize);
				rao.write(b);
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Table(final File f, final String name, final FieldType[] fields, final String names[],
			final int pageOffset) {
		super();
		this.pageOffset = pageOffset;
		this.names = names;
		this.name = name;
		int tempLength = 0;
		RandomAccessFile tempRao = null;
		this.f = f;
		this.fields = fields;

		for (int i = 0; i < fields.length; i++) {
			tempLength += fields[i].getLength();
		}
		recordLength = tempLength;
		alignedRecordLength = recordLength + recordLength % 2;
		recordsPerPage = (pageSize - pageOffset) / alignedRecordLength;
		try {
			final byte header[] = new byte[pageSize];
			tempRao = new RandomAccessFile(f, "rw");
			numberOfPages = (int) tempRao.length() / pageSize - 1;
			if (numberOfPages < 0) {
				tempRao.write(header);
				numberOfPages = 0;
			} else {
				tempRao.seek(0);
				tempRao.readFully(header);
				final ByteBuffer bh = ByteBuffer.wrap(header);
				bh.order(ByteOrder.LITTLE_ENDIAN);
				numberOfRecords = Math.max(0, bh.getInt(4) - 1);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		rao = tempRao;
	};

	public void close() {
		try {
			final byte header[] = new byte[512];
			final ByteBuffer bh = ByteBuffer.wrap(header);
			bh.order(ByteOrder.LITTLE_ENDIAN);
			for (final Page page : pageCache) {
				page.flush();
			}
			pageCache.clear();
			bh.putInt(0).putInt(numberOfRecords + 1).putInt(1)
					.putInt((int) (System.currentTimeMillis() / 1000)).putInt(0)
					.put("Raima Database Manager 4.5 [Build 17]".getBytes());
			rao.seek(0);
			rao.write(header);
			rao.close();
		} catch (final IOException exp) {
			exp.printStackTrace();
		}
	}

	protected Page getPage(final int pageNumber) {
		if (pageNumber < numberOfPages) {
			for (final Page page : pageCache) {
				if (page.getPageNumber() == pageNumber) {
					return page;
				}
			}
			final byte b[] = new byte[pageSize];
			try {
				rao.seek((pageNumber + 1) * pageSize);
				rao.readFully(b);
			} catch (final IOException e) {
				e.printStackTrace();
			}
			final Page ret = new Page(pageNumber, b);
			pageCache.add(ret);
			while (pageCache.size() > 10) {
				final Page rem = pageCache.remove(0);
				rem.flush();
			}
			return ret;
		}
		return null;
	}

	/**
	 * adds a row to the table
	 * 
	 * @return the row index of the fresh added row
	 */
	public int addRow() {
		if (numberOfRecords / recordsPerPage >= numberOfPages) {
			try {
				final byte b[] = new byte[pageSize];
				rao.seek((numberOfPages + 1) * pageSize);
				rao.write(b);
				numberOfPages++;
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		numberOfRecords++;
		return numberOfRecords - 1;
	}

	protected int getOffsetInPage(final int row, final int column) {
		final int offsetInPage = pageOffset + (row % recordsPerPage) * alignedRecordLength;
		int fieldOffset = 0;
		for (int i = 0; i < column; ++i) {
			fieldOffset += fields[i].length;
		}
		return offsetInPage + fieldOffset;
	}

	public void writeByte(final int row, final int column, final byte b) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.BYTE.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			page.writeByte(offsetInPage, b);
		}
	}

	public byte readByte(final int row, final int column) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.BYTE.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			return page.readByte(offsetInPage);
		}
	}

	public void writeShort(final int row, final int column, final short s) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.SHORT.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			page.writeShort(offsetInPage, s);
		}
	}

	public short readShort(final int row, final int column) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.SHORT.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			return page.readShort(offsetInPage);
		}
	}

	public void writeInt(final int row, final int column, final int i) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.INTEGER.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			page.writeInt(offsetInPage, i);
		}
	}

	public int readInt(final int row, final int column) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.INTEGER.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			return page.readInt(offsetInPage);
		}
	}

	public void writeString(final int row, final int column, final byte str[]) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.STRING.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			page.writeString(offsetInPage, str);
		}
	}

	public byte[] readString(final int row, final int column) {
		if (column >= fields.length) {
			throw new IllegalArgumentException("Column is outside of range of columns");
		} else if (!FieldType.STRING.equals(fields[column])) {
			throw new IllegalArgumentException("In this column no bytes are stored");
		} else {
			final int pageNr = row / recordsPerPage;
			final int offsetInPage = getOffsetInPage(row, column);
			final Page page = getPage(pageNr);
			return page.readString(offsetInPage);
		}
	}

	/**
	 * @return the pageOffset
	 */
	public int getPageOffset() {
		return pageOffset;
	}

	/**
	 * @return the recordsPerPage
	 */
	public int getRecordsPerPage() {
		return recordsPerPage;
	}

	/**
	 * @param recordsPerPage
	 *            the recordsPerPage to set
	 */
	public void setRecordsPerPage(final int recordsPerPage) {
		this.recordsPerPage = recordsPerPage;
	}

	/**
	 * @return the numberOfRecords
	 */
	public int getNumberOfRecords() {
		return numberOfRecords;
	}

	/**
	 * @param numberOfRecords
	 *            the numberOfRecords to set
	 */
	public void setNumberOfRecords(final int numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	/**
	 * @return the fields
	 */
	public FieldType[] getFields() {
		return fields;
	}

	public byte[] getFileDescriptor() {
		final byte desc[] = new byte[60];
		final byte nb[] = new byte[49];
		final byte name[] = f.getName().getBytes();
		final ByteBuffer buffer = ByteBuffer.wrap(desc);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		System.arraycopy(name, 0, nb, 0, name.length);
		buffer.put(nb);
		buffer.put((byte) 0x00);
		buffer.put((byte) 0x63); // Closed
		buffer.put((byte) 0x64); // Data
		buffer.putShort((short) getRecordsPerPage());
		buffer.putShort((short) alignedRecordLength);
		buffer.putShort((short) 512); // page Size
		buffer.putShort((short) 0x0040); // compressed
		return desc;
	}

	public byte[] getTableDescriptor(final int tableNumber, final int fieldOffset) {
		final byte desc[] = new byte[12];
		final ByteBuffer buffer = ByteBuffer.wrap(desc);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putShort((short) tableNumber);
		buffer.putShort((short) recordLength);
		buffer.putShort((short) 0x06);
		buffer.putShort((short) fieldOffset);
		buffer.putShort((short) (fields.length - 2));
		buffer.putShort((short) 0x00);
		return desc;
	}

	public byte[] getFieldDescriptor(final int tableNumber) {
		final byte desc[] = new byte[20 * fields.length - 40];
		final ByteBuffer buffer = ByteBuffer.wrap(desc);
		int fieldOffset = 6;
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		for (int i = 2; i < fields.length; ++i) {
			buffer.put((byte) 0x6E);
			switch (fields[i]) {
			case BYTE:
				buffer.put((byte) 0x63);
				buffer.putShort((short) 0x01); // FieldLength
				buffer.putShort((short) 0x00); // dim
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00); // key Filed
				buffer.putShort((short) 0x00); // key number
				buffer.putShort((short) fieldOffset); // Offset in record
				buffer.putShort((short) tableNumber); // Record
				buffer.putShort((short) 0x04); // Flag
				fieldOffset += 1;
				break;
			case INTEGER:
				buffer.put((byte) 0x6C);
				buffer.putShort((short) 0x04); // FieldLength
				buffer.putShort((short) 0x00); // dim
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00); // key Filed
				buffer.putShort((short) 0x00); // key number
				buffer.putShort((short) fieldOffset); // Offset in record
				buffer.putShort((short) tableNumber); // Record
				buffer.putShort((short) 0x04); // Flag
				fieldOffset += 4;
				break;
			case SHORT:
				buffer.put((byte) 0x73);
				buffer.putShort((short) 0x02);
				buffer.putShort((short) 0x00); // dim
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00); // key Filed
				buffer.putShort((short) 0x00); // key number
				buffer.putShort((short) fieldOffset); // Offset in record
				buffer.putShort((short) tableNumber); // Record
				buffer.putShort((short) 0x04); // Flag
				fieldOffset += 2;
				break;
			case STRING:
				buffer.put((byte) 0x63);
				buffer.putShort((short) 248);
				buffer.putShort((short) 248); // dim
				buffer.putShort((short) 1);
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00); // key Filed
				buffer.putShort((short) 0x00); // key number
				buffer.putShort((short) fieldOffset); // Offset in record
				buffer.putShort((short) tableNumber); // Record
				buffer.putShort((short) 0x00); // Flag
				fieldOffset += 248;
				break;
			case INT_ARRAY:
				buffer.put((byte) 0x6C);
				buffer.putShort((short) 156);
				buffer.putShort((short) 39); // dim
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00); // key Filed
				buffer.putShort((short) 0x00); // key number
				buffer.putShort((short) fieldOffset); // Offset in record
				buffer.putShort((short) tableNumber); // Record
				buffer.putShort((short) 0x04); // Flag
				fieldOffset += 156;
				break;
			case SHORT_STR:
				buffer.put((byte) 0x63);
				buffer.putShort((short) 25);
				buffer.putShort((short) 25); // dim
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00);
				buffer.putShort((short) 0x00); // key Filed
				buffer.putShort((short) 0x00); // key number
				buffer.putShort((short) fieldOffset); // Offset in record
				buffer.putShort((short) tableNumber); // Record
				buffer.putShort((short) 0x00); // Flag
				fieldOffset += 25;
				break;
			default:
				break;
			}
		}
		return desc;
	}

	/**
	 * @return the names
	 */
	public String[] getNames() {
		return names;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the f
	 */
	public File getTableFile() {
		return f;
	}
}

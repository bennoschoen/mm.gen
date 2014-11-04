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
 * @since 28.03.2014
 * @version 1.0
 * @author benno
 */
package mm.text;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import mm.db.DataBase;
import mm.db.Table;
import mm.db.Table.FieldType;

/**
 * @author benno
 * 
 */
public class TextDbGenerator {

	protected final File textDb;
	protected final File linkDb;
	protected final File clpDb;

	protected final Table extTable;
	protected final Table clpTable;
	protected final Table auxTable;
	protected final int textDbNr;
	protected final int linkDbNr;
	protected final int clpDbNr;

	protected int auxRow = -1;

	protected final TextLink noLink = new TextLink(0, 255);

	protected String lastText = null;
	protected TextLink lastLink = null;
	protected final int lookUp[] = new int[39];

	public TextDbGenerator(final File targetFolder, final DataBase db, final int group) {
		super();
		textDb = new File(targetFolder, "00gr" + group + ".aux");
		linkDb = new File(targetFolder, "00gr" + group + ".ext");
		clpDb = new File(targetFolder, "00gr" + group + ".clp");
		linkDbNr = 3 * group + 2;
		clpDbNr = linkDbNr + 1;
		textDbNr = clpDbNr + 1;

		if (textDb.exists()) {
			textDb.delete();
		}
		if (clpDb.exists()) {
			clpDb.delete();
		}
		if (linkDb.exists()) {
			linkDb.delete();
		}

		extTable = new Table(linkDb, "R_GR" + group, new FieldType[] { FieldType.SHORT,
				FieldType.INTEGER, FieldType.INTEGER, FieldType.INTEGER, FieldType.SHORT,
				FieldType.BYTE }, new String[] { "NAME_REF", "CELL_NUM", "N_IN_C", "OBJ_TYPE" }, 4);

		clpTable = new Table(clpDb, "RC_GR" + group, new FieldType[] { FieldType.SHORT,
				FieldType.INTEGER, FieldType.INTEGER, FieldType.SHORT }, new String[] { "CELL_NUM",
				"N_IN_C" }, 4);

		auxTable = new Table(textDb, "AUX_GR" + group, new FieldType[] { FieldType.SHORT,
				FieldType.INTEGER, FieldType.STRING }, new String[] { "NAME_BUF" }, 4);
		db.addTable(extTable);
		db.addTable(clpTable);
		db.addTable(auxTable);

		for (int i = 0; i < lookUp.length; i++) {
			lookUp[i] = 0;
		}
	}

	public static class TextLink {
		protected final int offset;
		protected final int row;

		public TextLink(final int row, final int offset) {
			super();
			this.row = row;
			this.offset = offset;
		}

		/**
		 * @return the offset
		 */
		public int getOffset() {
			return offset;
		}

		/**
		 * @return the row
		 */
		public int getRow() {
			return row;
		}
	}

	protected int getNextWritePos(final byte bb[]) {
		int ret = 0;
		for (int i = 0; i < bb.length; ++i) {
			if (bb[i] != 0) {
				ret = i + 2;
			}
		}
		return ret;
	}

	public TextLink addText(final String text, final int cell, final int element, final int objType) {
		if (lastText == null || lastLink == null || !lastText.equals(text)) {
			lastLink = inneraddText(text, cell, element, objType);
			lastText = text;
		} else {
			final int extRow = extTable.addRow();
			extTable.writeShort(extRow, 0, (short) linkDbNr);
			extTable.writeInt(extRow, 1, (linkDbNr << 25) + (extRow + 1));
			extTable.writeInt(extRow, 2, (lastLink.getOffset() << 24) + lastLink.getRow());
			extTable.writeInt(extRow, 3, cell);
			extTable.writeShort(extRow, 4, (short) (element + 1));
			extTable.writeByte(extRow, 5, (byte) objType);
		}
		return lastLink;
	}

	protected void checkLookUp(final String text, final int extRow) {
		if (!(text == null || text.isEmpty())) {
			int index = -1;
			final char c = text.toUpperCase().charAt(0);
			if (c < '0') {
				index = 0;
			} else if (c <= '9') {
				index = 1 + c - '0';
			} else if (c >= 'A' && c <= 'Z') {
				index = 12 + c - 'A';
			} else {
				index = 38;
			}
			if (index >= 0 && lookUp[index] == 0) {
				lookUp[index] = extRow;
			}
		}
	}

	protected TextLink inneraddText(final String text, final int cell, final int element,
			final int objType) {
		TextLink ret = null;
		String tempString = text;

		if (auxRow < 0) {
			auxRow = auxTable.addRow();
			auxTable.writeShort(auxRow, 0, (short) textDbNr);
			auxTable.writeInt(auxRow, 1, (textDbNr << 25) + (auxRow + 1));
			auxRow = auxTable.addRow();
			auxTable.writeShort(auxRow, 0, (short) textDbNr);
			auxTable.writeInt(auxRow, 1, (textDbNr << 25) + (auxRow + 1));
		}
		byte bb[] = auxTable.readString(auxRow, 2);
		try {
			while (tempString.getBytes("ISO-8859-1").length > bb.length - 1) {
				tempString = tempString.substring(0, tempString.length() - 1);
			}
			final byte bs[] = tempString.getBytes("ISO-8859-1");
			int last = getNextWritePos(bb);
			if (last >= 0 && last + 1 + bs.length < bb.length) {
				System.arraycopy(bs, 0, bb, last, bs.length);
			} else {
				auxRow = auxTable.addRow();
				auxTable.writeShort(auxRow, 0, (short) textDbNr);
				auxTable.writeInt(auxRow, 1, (textDbNr << 25) + (auxRow + 1));
				bb = auxTable.readString(auxRow, 2);
				System.arraycopy(bs, 0, bb, 0, bs.length);
				last = 0;
			}
			auxTable.writeString(auxRow, 2, bb);
			final int extRow = extTable.addRow();
			extTable.writeShort(extRow, 0, (short) linkDbNr);
			extTable.writeInt(extRow, 1, (linkDbNr << 25) + (extRow + 1));
			extTable.writeInt(extRow, 2, (last << 24) + auxRow);
			extTable.writeInt(extRow, 3, cell);
			extTable.writeShort(extRow, 4, (short) (element + 1));
			extTable.writeByte(extRow, 5, (byte) objType);
			ret = new TextLink(auxRow, last);
			checkLookUp(text, extRow + 1);
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void finish() {
		if (auxRow >= 0) {
			final byte firstLine[] = auxTable.readString(0, 2);
			final ByteBuffer buffer = ByteBuffer.wrap(firstLine);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			for (final int i : lookUp) {
				buffer.putInt(i);
			}
			auxTable.writeString(0, 2, firstLine);
		}
		extTable.close();
		auxTable.close();
		clpTable.close();
	}
}

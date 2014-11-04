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
 * @since 30.03.2014
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
public class DataBase {

	protected final byte version[] = { 0x56, 0x33, 0x2E, 0x30, 0x30, 0x1A };
	protected final int pageSize = 512;

	protected List<Table> tables = new ArrayList<>();
	protected final File f;
	protected final RandomAccessFile rao;

	public DataBase(final File f) {
		super();
		RandomAccessFile tempRao = null;
		this.f = f;
		try {
			tempRao = new RandomAccessFile(f, "rw");
		} catch (final Exception e) {
			e.printStackTrace();
		}
		rao = tempRao;
	}

	public int addTable(final Table table) {
		tables.add(table);
		return tables.size() - 1;
	}

	protected void writeHeader(final RandomAccessFile rao) throws IOException {
		final byte header[] = new byte[22];
		int fieldCount = 0;
		final ByteBuffer buffer = ByteBuffer.wrap(header);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(version);
		buffer.putShort((short) pageSize);
		buffer.putShort((short) (tables.size() * 2));
		buffer.putShort((short) tables.size());
		for (final Table table : tables) {
			fieldCount += table.getFields().length - 2;
		}
		buffer.putShort((short) fieldCount);
		rao.write(header);
	}

	public void close() {
		int fieldOffset = 0;
		try {
			final List<CompressTable> compressionTables = new ArrayList<>();
			rao.seek(0);
			writeHeader(rao);
			for (final Table table : tables) {
				final CompressTable compressTable = new CompressTable(table.getTableFile());
				rao.write(table.getFileDescriptor());
				rao.write(compressTable.getFileDescriptor());
				compressionTables.add(compressTable);
			}
			for (int i = 0; i < tables.size(); ++i) {
				final Table table = tables.get(i);
				rao.write(table.getTableDescriptor(i * 2, fieldOffset));
				fieldOffset += table.getFields().length - 2;
			}
			for (int i = 0; i < tables.size(); ++i) {
				final Table table = tables.get(i);
				rao.write(table.getFieldDescriptor(i));
			}
			final StringBuilder builder = new StringBuilder();
			for (final Table table : tables) {
				builder.append(table.getName());
				builder.append("\n");
			}
			for (final Table table : tables) {
				for (final String fieldName : table.getNames()) {
					builder.append(fieldName);
					builder.append("\n");
				}
			}
			rao.write(builder.toString().getBytes());
			rao.close();
			for (final CompressTable compressTable : compressionTables) {
				compressTable.compressTable();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}

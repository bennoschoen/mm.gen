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
 * @since 08.06.2014
 * @version 1.0
 * @author benno
 */
package mm.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.Deflater;

import mm.db.Table.FieldType;

/**
 * @author benno
 * 
 */
public class CompressTable {
	private final File tableFile;
	private final File f;
	private final Table compressTable;

	public CompressTable(final File tableFile) {
		super();
		f = getCompressionFile(tableFile);
		this.tableFile = tableFile;
		compressTable = new Table(f, "Expand", new FieldType[] { FieldType.INTEGER,
				FieldType.SHORT, FieldType.SHORT }, new String[] { "SHORT" }, 4);
		compressTable.addRow();
		compressTable.setNumberOfRecords(0);
	}

	private static File getCompressionFile(final File f) {
		File ret = null;
		try {
			final String fileName = f.getCanonicalPath();
			final int index = fileName.lastIndexOf(".");
			final String cFileName = fileName.substring(0, index) + "c" + fileName.substring(index);
			ret = new File(cFileName);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	protected int readBuffer(final FileInputStream fis, final byte b[]) throws IOException {
		int total = 0;
		int got = fis.read(b);
		total += got > 0 ? got : 0;
		while (got > 0 && total < b.length) {
			got = fis.read(b, total, b.length - total);
			total += got > 0 ? got : 0;
		}
		return total;
	}

	public void compressTable() {
		final byte b[] = new byte[512];
		final byte dest[] = new byte[1024];
		int offset = 512;
		try {
			final File tempFile = new File(tableFile.getCanonicalPath() + ".temp");
			tableFile.renameTo(tempFile);
			final FileOutputStream fos = new FileOutputStream(tableFile);
			final FileInputStream fis = new FileInputStream(tempFile);
			if (readBuffer(fis, b) == 512) {
				fos.write(b);
			}
			while (readBuffer(fis, b) == 512) {
				final Deflater compressor = new Deflater(8);
				compressor.setInput(b);
				compressor.finish();
				final int compressedLength = compressor.deflate(dest);
				fos.write(dest, 0, compressedLength);
				final int row = compressTable.addRow();
				compressTable.writeInt(row, 0, offset);
				compressTable.writeShort(row, 1, (short) compressedLength);
				compressTable.writeShort(row, 2, (short) 0x7608);
				offset += compressedLength;
				compressor.end();
			}
			fis.close();
			fos.close();
			compressTable.close();
			tempFile.delete();
		} catch (final IOException e) {
			e.printStackTrace();
		}
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
		buffer.put((byte) 0x63); // Compression Table
		buffer.putShort((short) 63);
		buffer.putShort((short) 8);
		buffer.putShort((short) 512); // page Size
		buffer.putShort((short) 0x00); // not compressed
		return desc;
	}
}

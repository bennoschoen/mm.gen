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
 * @since 25.04.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.imi;

import java.io.File;
import java.io.FileInputStream;
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
public class ImiGenerator {
	protected final byte signature[] = { 0x4d, 0x41, 0x47, 0x45, 0x4c, 0x4c, 0x41, 0x4e };

	protected final File sourceFolder;
	protected final File destFile;

	protected byte checkSum[] = new byte[2];
	protected int chkSumPos = 0;

	protected void updateChecksum(final byte b[], final int length) {
		for (int i = 0; i < b.length && i < length; i++) {
			checkSum[chkSumPos] ^= b[i];
			chkSumPos = 1 - chkSumPos;
		}
	}

	protected void resetCheckSum() {
		chkSumPos = 0;
		checkSum[0] = 0;
		checkSum[1] = 0;
	}

	public ImiGenerator(final File sourceFolder, final File destFile) {
		super();
		this.sourceFolder = sourceFolder;
		this.destFile = destFile;
		if (destFile.exists()) {
			destFile.delete();
		}
	}

	public void genFile() {
		final byte header[];
		final File d[] = sourceFolder.listFiles();
		final List<File> dd = new ArrayList<>();
		final List<TocFile> tocs = new ArrayList<>();
		final ByteBuffer headerBuffer;
		for (final File file : d) {
			if (file.isFile() && !file.getName().startsWith(".")) {
				dd.add(file);
			}
		}
		try {
			final byte b[] = new byte[10000];
			int start = 40 + dd.size() * 24;
			header = new byte[start];
			final RandomAccessFile rao = new RandomAccessFile(destFile, "rw");

			headerBuffer = ByteBuffer.wrap(header);
			headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
			headerBuffer.putInt(dd.size());
			headerBuffer.putInt(dd.size());

			for (final File file : dd) {
				final FileInputStream fis = new FileInputStream(file);
				int total = 0;
				int got = fis.read(b);
				while (got > 0) {
					total += got;
					got = fis.read(b);
				}
				fis.close();
				tocs.add(new TocFile(file.getName(), start, total));
				start += total;
			}
			for (final TocFile tocFile : tocs) {
				headerBuffer.put(tocFile.getBuffer());
			}
			updateChecksum(header, start - 32);
			headerBuffer.put(checkSum[0]);
			headerBuffer.put(checkSum[1]);
			headerBuffer.put(signature);
			resetCheckSum();
			updateChecksum(header, header.length);

			rao.seek(0);
			rao.write(header);

			for (final File file : dd) {
				final FileInputStream fis = new FileInputStream(file);
				int got = fis.read(b);
				while (got > 0) {
					rao.write(b, 0, got);
					updateChecksum(b, got);
					got = fis.read(b);
				}
				fis.close();
			}
			updateChecksum(signature, signature.length);
			rao.write(signature);
			if (start % 2 == 1) {
				rao.write(0);
			}
			rao.write(checkSum);
			rao.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected class TocFile {
		protected final String name;
		protected final int offset;
		protected final int length;

		public TocFile(final String name, final int offset, final int length) {
			super();
			this.name = name;
			this.offset = offset;
			this.length = length;
		}

		public byte[] getBuffer() {
			final byte b[] = new byte[24];
			final ByteBuffer buffer = ByteBuffer.wrap(b);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			final int index = name.indexOf('.');
			if (index >= 0) {
				buffer.put(name.substring(0, index).getBytes());
				buffer.position(9);
				buffer.put(name.substring(index + 1).getBytes());
			} else {
				buffer.put(name.getBytes());
			}
			buffer.position(12);
			buffer.putInt(0);
			buffer.putInt(offset);
			buffer.putInt(length);

			return b;
		}
	}
}

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
 * @since 08.09.2014
 * @version 1.0
 * @author benno
 */
package mm.o5m.reader;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;

/**
 * @author benno
 * 
 */
public final class O5mReader {

	public static final int STRING_PAIR_CACHE_SIZE = 15000;
	public static final int MAX_CACHEABLE_STRING_LENGTH = 252;

	public static final int NODE_ENTRY = 0x010;
	public static final int WAY_ENTRY = 0x11;
	public static final int RELATION_ENTRY = 0x12;
	public static final int BOUNDING_BOX = 0xDB;
	public static final int FILE_TIMESTAMP = 0xDC;
	public static final int HEADER = 0xE0;
	public static final int SYNC_POINT = 0xEE;
	public static final int JUMP_POINT = 0xEF;
	public static final int END_OF_FILE = 0xFE;
	public static final int RESET = 0xFF;

	public String stringPairs[][] = new String[STRING_PAIR_CACHE_SIZE][2];
	public int cacheWritePos = 0;

	public O5mReader() {

	}

	public void readFile(final File f, final O5mHandler handler) throws IOException {
		ByteBuffer buffer = null;
		final BufferedInputStream raf = new BufferedInputStream(new FileInputStream(f), 1 << 16);
		final int bufferSize = 1000;
		byte readBuffer[] = new byte[bufferSize];
		long numberOfNodes = 0;
		long numberOfWays = 0;
		long numberOfRelations = 0;
		boolean goOn = true;
		System.out.println(new Date());
		final O5mNode node = new O5mNode();
		final O5mWay way = new O5mWay();
		final O5mRelation relation = new O5mRelation();
		while (goOn) {
			final int blockType = raf.read();

			if (blockType >= 0 && blockType <= 0xEF) {
				final int blockLength = readUnsigned32(raf);
				if (blockLength > readBuffer.length) {
					readBuffer = new byte[blockLength + bufferSize];
				}
				int got = raf.read(readBuffer, 0, blockLength);
				while (got < blockLength) {
					got += raf.read(readBuffer, got, blockLength - got);
				}
				buffer = ByteBuffer.wrap(readBuffer, 0, blockLength);
			}
			switch (blockType) {
			case NODE_ENTRY:
				numberOfNodes++;
				if (numberOfNodes % 1000000 == 0) {
					System.out.print(".");
				}
				if (numberOfNodes % 50000000 == 0) {
					System.out.println();
				}
				if (numberOfNodes == 231944) {
					System.out.println();
				}
				node.readFromBuffer(buffer, this);
				handler.newNode(node);
				break;
			case WAY_ENTRY:
				if (numberOfWays == 0) {
					System.out.println();
					System.out.println("--------------   WAYS   -------------------");
				}
				numberOfWays++;
				if (numberOfWays % 1000000 == 0) {
					System.out.print(".");
				}
				if (numberOfWays % 50000000 == 0) {
					System.out.println();
				}
				way.readFromBuffer(buffer, this);
				handler.newWay(way);
				break;
			case RELATION_ENTRY:
				if (numberOfRelations == 0) {
					System.out.println();
					System.out.println("--------------   RELATIONS   -------------------");
				}
				numberOfRelations++;
				if (numberOfRelations % 1000000 == 0) {
					System.out.print(".");
				}
				if (numberOfRelations % 50000000 == 0) {
					System.out.println();
				}
				relation.readFromBuffer(buffer, this);
				handler.newRelation(relation);
				break;
			case BOUNDING_BOX:
				break;
			case FILE_TIMESTAMP:
				break;
			case HEADER:
				break;
			case SYNC_POINT:
				System.out.println("Here is a sync point");
				break;
			case JUMP_POINT:
				System.out.println("Here is a jump point");
				break;
			case RESET:
				node.reset();
				way.reset();
				relation.reset();
				break;
			case END_OF_FILE:
				goOn = false;
				break;
			default:
				throw new IOException("Unexpected Block Type in File " + blockType);
			}
		}
		System.out.println();
		System.out.println("There are so many Nodes :" + numberOfNodes);
		System.out.println("There are so many Ways :" + numberOfWays);
		System.out.println(new Date());
	}

	public String[] getRelStringPair(final int rel) {
		return stringPairs[(STRING_PAIR_CACHE_SIZE + cacheWritePos - rel) % STRING_PAIR_CACHE_SIZE];
	}

	public void storeStringPair(final String pair[]) {
		stringPairs[cacheWritePos][0] = pair[0];
		stringPairs[cacheWritePos][1] = pair[1];
		cacheWritePos++;
		cacheWritePos %= STRING_PAIR_CACHE_SIZE;
	}

	public String[] readAuthor(final ByteBuffer buffer) throws IOException {
		final ByteArrayOutputStream bao[] = new ByteArrayOutputStream[] {
				new ByteArrayOutputStream(), new ByteArrayOutputStream() };
		final int reference = readUnsigned32(buffer);
		String ret[] = null;
		if (reference == 0) {
			int wc = 0; // Word Counter
			int bc = 0; // Byte Counter
			ret = new String[2];
			while (wc < 2) {
				final byte b = buffer.get();
				bc++;
				if (b == 0) {
					wc++;
				} else {
					bao[wc].write(b);
				}
			}
			if (bao[0].size() > 0) {
				final long uid = readUnsigned64(ByteBuffer.wrap(bao[0].toByteArray()));
				if (uid == 0) {
					ret[0] = "";
				} else {
					ret[0] = Long.toString(uid);
				}
			} else {
				ret[0] = "";
			}
			if (bao[1].size() > 0) {
				ret[1] = new String(bao[1].toByteArray(), "UTF-8");
			} else {
				ret[1] = "";
			}
			if (bc <= MAX_CACHEABLE_STRING_LENGTH) {
				storeStringPair(ret);
			}
		} else {
			ret = getRelStringPair(reference);
		}

		return ret;

	}

	public String[] readStringPair(final ByteBuffer buffer) throws IOException {
		final ByteArrayOutputStream bao[] = new ByteArrayOutputStream[] {
				new ByteArrayOutputStream(), new ByteArrayOutputStream() };
		final int reference = readUnsigned32(buffer);
		String ret[] = null;
		if (reference == 0) {
			int wc = 0; // Word Counter
			int bc = 0; // Byte Counter
			ret = new String[2];
			while (wc < 2) {
				final byte b = buffer.get();
				bc++;
				if (b == 0) {
					wc++;
				} else {
					bao[wc].write(b);
				}
			}
			ret[0] = new String(bao[0].toByteArray(), "UTF-8");
			ret[1] = new String(bao[1].toByteArray(), "UTF-8");
			if (bc <= MAX_CACHEABLE_STRING_LENGTH) {
				storeStringPair(ret);
			}
		} else {
			ret = getRelStringPair(reference);
		}

		return ret;
	}

	public String readSingleString(final ByteBuffer buffer) throws IOException {
		final ByteArrayOutputStream bao = new ByteArrayOutputStream();
		final int reference = readUnsigned32(buffer);
		String ret[] = null;
		if (reference == 0) {
			int wc = 0; // Word Counter
			int bc = 0; // Byte Counter
			ret = new String[2];
			while (wc < 1) {
				final byte b = buffer.get();
				bc++;
				if (b == 0) {
					wc++;
				} else {
					bao.write(b);
				}
			}
			ret[0] = new String(bao.toByteArray(), "UTF-8");
			ret[1] = "";
			if (bc <= MAX_CACHEABLE_STRING_LENGTH) {
				storeStringPair(ret);
			}
		} else {
			ret = getRelStringPair(reference);
		}

		return ret[0];
	}

	/**
	 * reads a Unsigned int 32 from a ByteBuffer. Note that the result is a
	 * signed value, maybe you have to do something magical in case of negative
	 * values
	 * 
	 * @param buffer
	 * @return
	 */
	public int readUnsigned32(final ByteBuffer buffer) {
		int shift = 0;
		byte b = buffer.get();
		int ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = buffer.get();
			ret += (b & 0x7f) << shift;
		}
		return ret;
	}

	public int readSigned32(final ByteBuffer buffer) {
		long ret = readUnsigned64(buffer);
		if ((ret & 0x01) == 0x01) {
			ret = -1 - (ret >> 1);
		} else {
			ret = ret >> 1;
		}
		return (int) ret;
	}

	public long readUnsigned64(final BufferedInputStream raf) throws IOException {
		int shift = 0;
		int b = raf.read();
		long ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = raf.read();
			ret += ((long) b & 0x7f) << shift;
		}
		return ret;
	}

	public int readUnsigned32(final BufferedInputStream raf) throws IOException {
		int shift = 0;
		int b = raf.read();
		int ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = raf.read();
			ret += (b & 0x7f) << shift;
		}
		return ret;
	}

	public long readUnsigned64(final ByteBuffer buffer) {
		int shift = 0;
		byte b = buffer.get();
		long ret = b & 0x7f;
		while ((b & 0x80) == 0x80) {
			shift += 7;
			b = buffer.get();
			ret += ((long) b & 0x7f) << shift;
		}
		return ret;
	}

	public long readSigned64(final ByteBuffer buffer) {
		int shift = 6;
		byte b = buffer.get();
		long ret = (b & 0x7e) >> 1;
		final long sign = b & 0x01;
		while ((b & 0x80) == 0x80) {
			b = buffer.get();
			ret += ((long) b & 0x7f) << shift;
			shift += 7;
		}
		if (sign == 1) {
			ret = -1 - ret;
		}
		return ret;
	}
}

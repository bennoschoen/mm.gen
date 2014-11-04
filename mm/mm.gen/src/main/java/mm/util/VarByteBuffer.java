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
package mm.util;

import java.nio.Buffer;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author benno
 * 
 */
public class VarByteBuffer {
	int capacity = 1024;
	int incCapacity = 1024;
	int maxPosition = 0;
	ByteBuffer wrap = ByteBuffer.allocate(capacity);

	protected void increaseCapacity() {
		final ByteBuffer temp = ByteBuffer.allocate(capacity + incCapacity);
		capacity += incCapacity;
		final int position = wrap.position();
		wrap.position(0);
		temp.put(wrap);
		temp.position(position);
		wrap = temp;
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#get()
	 */
	public byte get() {
		return wrap.get();
	}

	/**
	 * @param dst
	 * @param offset
	 * @param length
	 * @return
	 * @see java.nio.ByteBuffer#get(byte[], int, int)
	 */
	public ByteBuffer get(final byte[] dst, final int offset, final int length) {
		return wrap.get(dst, offset, length);
	}

	/**
	 * @param dst
	 * @return
	 * @see java.nio.ByteBuffer#get(byte[])
	 */
	public ByteBuffer get(final byte[] dst) {
		return wrap.get(dst);
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#get(int)
	 */
	public byte get(final int index) {
		return wrap.get(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getChar()
	 */
	public char getChar() {
		return wrap.getChar();
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#getChar(int)
	 */
	public char getChar(final int index) {
		return wrap.getChar(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getDouble()
	 */
	public double getDouble() {
		return wrap.getDouble();
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#getDouble(int)
	 */
	public double getDouble(final int index) {
		return wrap.getDouble(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getFloat()
	 */
	public float getFloat() {
		return wrap.getFloat();
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#getFloat(int)
	 */
	public float getFloat(final int index) {
		return wrap.getFloat(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getInt()
	 */
	public int getInt() {
		return wrap.getInt();
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#getInt(int)
	 */
	public int getInt(final int index) {
		return wrap.getInt(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getLong()
	 */
	public long getLong() {
		return wrap.getLong();
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#getLong(int)
	 */
	public long getLong(final int index) {
		return wrap.getLong(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#getShort()
	 */
	public short getShort() {
		return wrap.getShort();
	}

	/**
	 * @param index
	 * @return
	 * @see java.nio.ByteBuffer#getShort(int)
	 */
	public short getShort(final int index) {
		return wrap.getShort(index);
	}

	/**
	 * @return
	 * @see java.nio.ByteBuffer#order()
	 */
	public final ByteOrder order() {
		return wrap.order();
	}

	/**
	 * @param bo
	 * @return
	 * @see java.nio.ByteBuffer#order(java.nio.ByteOrder)
	 */
	public final ByteBuffer order(final ByteOrder bo) {
		return wrap.order(bo);
	}

	/**
	 * @return
	 * @see java.nio.Buffer#position()
	 */
	public final int position() {
		return wrap.position();
	}

	/**
	 * @param newPosition
	 * @return
	 * @see java.nio.Buffer#position(int)
	 */
	public final Buffer position(final int newPosition) {
		return wrap.position(newPosition);
	}

	/**
	 * @param b
	 * @return
	 * @see java.nio.ByteBuffer#put(byte)
	 */
	public ByteBuffer put(final byte b) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.put(b);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param src
	 * @param offset
	 * @param length
	 * @return
	 * @see java.nio.ByteBuffer#put(byte[], int, int)
	 */
	public ByteBuffer put(final byte[] src, final int offset, final int length) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.put(src, offset, length);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param src
	 * @return
	 * @see java.nio.ByteBuffer#put(byte[])
	 */
	public final ByteBuffer put(final byte[] src) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.put(src);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param src
	 * @return
	 * @see java.nio.ByteBuffer#put(java.nio.ByteBuffer)
	 */
	public ByteBuffer put(final ByteBuffer src) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.put(src);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;

	}

	/**
	 * @param index
	 * @param b
	 * @return
	 * @see java.nio.ByteBuffer#put(int, byte)
	 */
	public ByteBuffer put(final int index, final byte b) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.put(index, b);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putChar(char)
	 */
	public ByteBuffer putChar(final char value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putChar(value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param index
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putChar(int, char)
	 */
	public ByteBuffer putChar(final int index, final char value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putChar(index, value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putDouble(double)
	 */
	public ByteBuffer putDouble(final double value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putDouble(value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param index
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putDouble(int, double)
	 */
	public ByteBuffer putDouble(final int index, final double value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putDouble(index, value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putFloat(float)
	 */
	public ByteBuffer putFloat(final float value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putFloat(value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param index
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putFloat(int, float)
	 */
	public ByteBuffer putFloat(final int index, final float value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putFloat(index, value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param index
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putInt(int, int)
	 */
	public ByteBuffer putInt(final int index, final int value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putInt(index, value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putInt(int)
	 */
	public ByteBuffer putInt(final int value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putInt(value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param index
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putLong(int, long)
	 */
	public ByteBuffer putLong(final int index, final long value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putLong(index, value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putLong(long)
	 */
	public ByteBuffer putLong(final long value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putLong(value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param index
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putShort(int, short)
	 */
	public ByteBuffer putShort(final int index, final short value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putShort(index, value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @param value
	 * @return
	 * @see java.nio.ByteBuffer#putShort(short)
	 */
	public ByteBuffer putShort(final short value) {
		ByteBuffer ret = null;
		while (ret == null) {
			try {
				ret = wrap.putShort(value);
			} catch (final BufferOverflowException boe) {
				increaseCapacity();
			}
		}
		maxPosition = Math.max(maxPosition, wrap.position());
		return ret;
	}

	/**
	 * @return the maxPosition
	 */
	public int getMaxPosition() {
		return maxPosition;
	}

	public byte[] getBytes() {
		final byte ret[] = new byte[maxPosition];
		final int position = wrap.position();
		wrap.position(0);
		wrap.get(ret, 0, maxPosition);
		wrap.position(position);
		return ret;
	}
}

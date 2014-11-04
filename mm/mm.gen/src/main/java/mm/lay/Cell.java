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
 * @since 06.04.2014
 * @version 1.0
 * @author benno
 */
package mm.lay;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import mm.gen.db.WayClassifier.Graphics;
import mm.lay.types.BaseGraphic;
import mm.util.VarByteBuffer;

/**
 * @author benno
 * 
 */
public class Cell {
	protected final Graphics graphics;
	protected final byte objectType;
	protected final int cellId;
	protected final List<BaseGraphic> baseGraphics = new ArrayList<>();

	public Cell(final Graphics graphics, final int cellId, final byte objectType) {
		super();
		this.graphics = graphics;
		this.objectType = objectType;
		this.cellId = cellId;
	}

	public int addBaseGraphics(final BaseGraphic polyline) {
		baseGraphics.add(polyline);
		return baseGraphics.size() - 1;
	}

	public byte[] getCellStream(final CellBounds bounds) {
		byte ret[] = {};
		if (!baseGraphics.isEmpty()) {
			final VarByteBuffer varBuffer = new VarByteBuffer();
			varBuffer.order(ByteOrder.LITTLE_ENDIAN);
			varBuffer.putShort((short) baseGraphics.size());
			varBuffer.putShort((short) 0);
			for (final BaseGraphic polyline : baseGraphics) {
				byte vSize = 0;
				int ebCount = 0;
				final byte pb[] = polyline.getAsStream();
				final byte eb[] = new byte[19];
				final int bb[] = {
				/** Lon */
				(int) polyline.getBottomLeft().getX() - bounds.getX(),
				/** Lat */
				(int) polyline.getBottomLeft().getY() - bounds.getY(),
				/** Width */
				(int) polyline.getTopRight().getX() - (int) polyline.getBottomLeft().getX(),
				/** Height */
				(int) polyline.getTopRight().getY() - (int) polyline.getBottomLeft().getY() };
				for (int i = 0; i < bb.length; ++i) {
					final int b = bb[bb.length - i - 1];
					vSize = (byte) ((vSize << 2) & 0xff);
					if (b == 0) {
						vSize |= 3;
					} else if (b <= 255) {
						vSize |= 2;
					} else if (b <= 65535) {
						vSize |= 1;
					}
				}

				final ByteBuffer elementHeader = ByteBuffer.wrap(eb);
				elementHeader.order(ByteOrder.LITTLE_ENDIAN);
				elementHeader.putShort((short) (18 + pb.length));
				elementHeader.put(vSize);
				ebCount = 3;
				for (int i = 0; i < bb.length; ++i) {
					final int b = bb[i];
					switch ((vSize >> (i * 2)) & 0x3) {
					case 0:
						elementHeader.putInt(b);
						ebCount += 4;
						break;
					case 1:
						elementHeader.putShort((short) (b & 0xffff));
						ebCount += 2;
						break;
					case 2:
						elementHeader.put((byte) (b & 0xff));
						ebCount += 1;
						break;
					case 3:
						break;
					}
				}
				varBuffer.put(eb, 0, ebCount);
				varBuffer.put(pb);
				// for (int i = 0; i < ebCount; ++i) {
				// System.out.print(eb[i] + " , ");
				// }
				// for (int i = 0; i < pb.length; ++i) {
				// System.out.print(pb[i] + " , ");
				// }
				// System.out.println();
			}
			ret = varBuffer.getBytes();
		}
		return ret;
	}

	public int getNumberOfBaseGraphics() {
		return baseGraphics.size();
	}

	/**
	 * @return the cellId
	 */
	public int getCellId() {
		return cellId;
	}
}

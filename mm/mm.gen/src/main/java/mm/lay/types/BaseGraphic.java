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
 * @since 16.05.2014
 * @version 1.0
 * @author benno
 */
package mm.lay.types;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.atomic.AtomicInteger;

import mm.gen.db.WayClassifier.Graphics;
import mm.gen.db.WayClassifier.LayerType;
import mm.io.StreamIo;
import mm.io.Streamable;
import mm.lay.Point;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public abstract class BaseGraphic implements Streamable {
	protected static AtomicInteger lineIdCounter = new AtomicInteger(1);
	protected Graphics graphics = Graphics.POLYLINE;
	protected LayerType layer = LayerType.HIGHWAY;
	protected String name;
	protected byte textOffset = (byte) 0xff;
	protected short textIndex = -1;
	protected byte objectType = (byte) -1;
	protected int cellId = -1;;
	protected int numberInCell = -1;
	protected int lineId = lineIdCounter.incrementAndGet();

	@Override
	public JSONObject toJSON() {
		final JSONObject ret = new JSONObject();
		try {
			ret.put("type", graphics.name());
			ret.put("layer", layer.name());
			ret.put("name", name);
			ret.put("cell", cellId);
			ret.put("nrInCell", numberInCell);
			ret.put("id", lineId);
			ret.put("textOffset", textOffset);
			ret.put("textIndex", textIndex);
			ret.put("objectType", objectType);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(graphics.ordinal());
		out.writeInt(layer.ordinal());
		if (name == null) {
			out.writeBoolean(true);
		} else {
			out.writeBoolean(false);
			StreamIo.writeString(out, name);
		}
		out.writeInt(cellId);
		out.writeInt(numberInCell);
		out.writeInt(lineId);
		out.writeByte(textOffset);
		out.writeShort(textIndex);
		out.writeByte(objectType);
	}

	@Override
	public void fromJSON(final String json) {
		try {
			final JSONObject way = new JSONObject(json);
			fromJSON(way);
		} catch (final JSONException e) {
			System.out.println(json);
			e.printStackTrace();
		}
	}

	@Override
	public void fromJSON(final JSONObject json) {
		try {
			graphics = Graphics.valueOf(json.getString("type"));
			layer = LayerType.valueOf(json.getString("layer"));
			name = json.getString("name");
			cellId = json.getInt("cell");
			numberInCell = json.getInt("nrInCell");
			lineId = json.getInt("id");
			textOffset = (byte) (json.optInt("textOffset", 0xff) & 0xff);
			textIndex = (short) (json.optInt("textIndex", -1));
			objectType = (byte) (json.optInt("objectType", 0) & 0xff);
		} catch (final JSONException e) {
			System.out.println(json.toString());
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		graphics = Graphics.values()[in.readInt()];
		layer = LayerType.values()[in.readInt()];
		if (in.readBoolean()) {
			name = null;
		} else {
			name = StreamIo.readString(in);
		}
		cellId = in.readInt();
		numberInCell = in.readInt();
		lineId = in.readInt();
		textOffset = in.readByte();
		textIndex = in.readShort();
		objectType = in.readByte();
	}

	/**
	 * @return the cellId
	 */
	public int getCellId() {
		return cellId;
	}

	/**
	 * @param cellId
	 *            the cellId to set
	 */
	public void setCellId(final int cellId) {
		this.cellId = cellId;
	}

	/**
	 * @return the textOffset
	 */
	public byte getTextOffset() {
		return textOffset;
	}

	/**
	 * @param textOffset
	 *            the textOffset to set
	 */
	public void setTextOffset(final byte textOffset) {
		this.textOffset = textOffset;
	}

	/**
	 * @return the textIndex
	 */
	public short getTextIndex() {
		return textIndex;
	}

	/**
	 * @param textIndex
	 *            the textIndex to set
	 */
	public void setTextIndex(final short textIndex) {
		this.textIndex = textIndex;
	}

	/**
	 * @return the objectType
	 */
	public byte getObjectType() {
		return objectType;
	}

	/**
	 * @param objectType
	 *            the objectType to set
	 */
	public void setObjectType(final byte objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return the lineId
	 */
	public int getLineId() {
		return lineId;
	}

	/**
	 * @return the graphics
	 */
	public Graphics getGraphics() {
		return graphics;
	}

	/**
	 * @param graphics
	 *            the graphics to set
	 */
	public void setGraphics(final Graphics graphics) {
		this.graphics = graphics;
	}

	public abstract byte[] getAsStream();

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the layer
	 */
	public LayerType getLayer() {
		return layer;
	}

	/**
	 * @param layer
	 *            the layer to set
	 */
	public void setLayer(final LayerType layer) {
		this.layer = layer;
	}

	/**
	 * @return the numberInCell
	 */
	public int getNumberInCell() {
		return numberInCell;
	}

	/**
	 * @param numberInCell
	 *            the numberInCell to set
	 */
	public void setNumberInCell(final int numberInCell) {
		this.numberInCell = numberInCell;
	}

	/**
	 * @return the bottomLeft
	 */
	public abstract Point getBottomLeft();

	/**
	 * @return the topRight
	 */
	public abstract Point getTopRight();
}

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

import mm.gen.db.WayClassifier.Graphics;
import mm.io.Streamable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class Carrier implements Streamable {
	protected Graphics graphics = Graphics.POLYLINE;
	protected BaseGraphic baseElement = null;

	public Carrier() {
	}

	public Carrier(final String json) {
		fromJSON(json);
	}

	public Carrier(final PolyLine baseElement) {
		super();
		graphics = Graphics.POLYLINE;
		this.baseElement = baseElement;
	}

	public Carrier(final PolyArea baseElement) {
		super();
		graphics = Graphics.AREA;
		this.baseElement = baseElement;
	}

	@Override
	public JSONObject toJSON() {
		return baseElement.toJSON();
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
			switch (graphics) {
			case AREA:
				baseElement = new PolyArea(json);
				break;
			case LABELS:
				break;
			case POI:
				break;
			case POINT:
				break;
			case POLYLINE:
				baseElement = new PolyLine(json);
				break;
			default:
				break;
			}
		} catch (final JSONException e) {
			System.out.println(json);
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		if (baseElement == null) {
			out.writeBoolean(true);
		} else {
			out.writeBoolean(false);
			out.writeInt(baseElement.getGraphics().ordinal());
			baseElement.writeExternal(out);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		if (in.readBoolean()) {
			baseElement = null;
		} else {
			switch (Graphics.values()[in.readInt()]) {
			case AREA:
				baseElement = new PolyArea();
				break;
			case LABELS:
				break;
			case POI:
				break;
			case POINT:
				break;
			case POLYLINE:
				baseElement = new PolyLine();
				break;
			default:
				break;
			}
			if (baseElement != null) {
				baseElement.readExternal(in);
			}
		}
	}

	/**
	 * @return the baseElement
	 */
	public BaseGraphic getBaseElement() {
		return baseElement;
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getCellId()
	 */
	public int getCellId() {
		return baseElement.getCellId();
	}

	/**
	 * @param cellId
	 * @see mm.lay.types.BaseGraphic#setCellId(int)
	 */
	public void setCellId(final int cellId) {
		baseElement.setCellId(cellId);
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getTextOffset()
	 */
	public byte getTextOffset() {
		return baseElement.getTextOffset();
	}

	/**
	 * @param textOffset
	 * @see mm.lay.types.BaseGraphic#setTextOffset(byte)
	 */
	public void setTextOffset(final byte textOffset) {
		baseElement.setTextOffset(textOffset);
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getTextIndex()
	 */
	public short getTextIndex() {
		return baseElement.getTextIndex();
	}

	/**
	 * @param textIndex
	 * @see mm.lay.types.BaseGraphic#setTextIndex(short)
	 */
	public void setTextIndex(final short textIndex) {
		baseElement.setTextIndex(textIndex);
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getObjectType()
	 */
	public byte getObjectType() {
		return baseElement.getObjectType();
	}

	/**
	 * @param objectType
	 * @see mm.lay.types.BaseGraphic#setObjectType(byte)
	 */
	public void setObjectType(final byte objectType) {
		baseElement.setObjectType(objectType);
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getLineId()
	 */
	public int getLineId() {
		return baseElement.getLineId();
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getGraphics()
	 */
	public Graphics getGraphics() {
		return baseElement.getGraphics();
	}

	/**
	 * @param graphics
	 * @see mm.lay.types.BaseGraphic#setGraphics(mm.gen.db.WayClassifier.Graphics)
	 */
	public void setGraphics(final Graphics graphics) {
		baseElement.setGraphics(graphics);
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getName()
	 */
	public String getName() {
		return baseElement.getName();
	}

	/**
	 * @param name
	 * @see mm.lay.types.BaseGraphic#setName(java.lang.String)
	 */
	public void setName(final String name) {
		baseElement.setName(name);
	}

	/**
	 * @return
	 * @see mm.lay.types.BaseGraphic#getNumberInCell()
	 */
	public int getNumberInCell() {
		return baseElement.getNumberInCell();
	}

	/**
	 * @param numberInCell
	 * @see mm.lay.types.BaseGraphic#setNumberInCell(int)
	 */
	public void setNumberInCell(final int numberInCell) {
		baseElement.setNumberInCell(numberInCell);
	}
}

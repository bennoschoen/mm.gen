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
 * @since 20.03.2014
 * @version 1.0
 * @author benno
 */
package mm.lay.types;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import mm.gen.db.WayClassifier.Graphics;
import mm.lay.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class PolyArea extends BaseGraphic {

	List<SubPolyArea> innerAreas = new ArrayList<>();
	List<SubPolyArea> outerAreas = new ArrayList<>();

	public PolyArea() {
	}

	public PolyArea(final String json) {
		fromJSON(json);
		graphics = Graphics.AREA;
	}

	public PolyArea(final JSONObject json) {
		fromJSON(json);
		graphics = Graphics.AREA;
	}

	public PolyArea(final String name, final List<SubPolyArea> outerAreas,
			final List<SubPolyArea> innerAreas) {
		super();

		this.name = name;
		this.innerAreas.clear();
		this.outerAreas.clear();
		this.innerAreas.addAll(innerAreas);
		this.outerAreas.addAll(outerAreas);
	}

	public void addInnerArea(final SubPolyArea subArea) {
		innerAreas.add(subArea);
	}

	public void addOuterArea(final SubPolyArea subArea) {
		outerAreas.add(subArea);
	}

	@Override
	public JSONObject toJSON() {
		final JSONObject ret = super.toJSON();
		try {
			final JSONArray innerArray = new JSONArray();
			final JSONArray outerArray = new JSONArray();
			for (final SubPolyArea innerArea : innerAreas) {
				innerArray.put(innerArea.toJSON());
			}
			for (final SubPolyArea outerArea : outerAreas) {
				outerArray.put(outerArea.toJSON());
			}
			ret.put("innerArea", innerArray);
			ret.put("outerArea", outerArray);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void fromJSON(final JSONObject json) {
		innerAreas.clear();
		outerAreas.clear();
		try {
			super.fromJSON(json);

			final JSONArray innerArray = json.getJSONArray("innerArea");
			final JSONArray outerArray = json.getJSONArray("outerArea");
			for (int i = 0; i < innerArray.length(); ++i) {
				final JSONObject p = innerArray.getJSONObject(i);
				final SubPolyArea subPolyArea = new SubPolyArea();
				subPolyArea.fromJSON(p);
				innerAreas.add(subPolyArea);
			}
			for (int i = 0; i < outerArray.length(); ++i) {
				final JSONObject p = outerArray.getJSONObject(i);
				final SubPolyArea subPolyArea = new SubPolyArea();
				subPolyArea.fromJSON(p);
				outerAreas.add(subPolyArea);
			}
		} catch (final JSONException e) {
			System.out.println(json.toString());
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mm.lay.types.BaseGraphic#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeInt(outerAreas.size());
		for (final SubPolyArea outerArea : outerAreas) {
			outerArea.writeExternal(out);
		}
		out.writeInt(innerAreas.size());
		for (final SubPolyArea innerArea : innerAreas) {
			innerArea.writeExternal(out);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mm.lay.types.BaseGraphic#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		innerAreas.clear();
		outerAreas.clear();
		super.readExternal(in);
		final int outerSize = in.readInt();
		for (int i = 0; i < outerSize; ++i) {
			final SubPolyArea subPolyArea = new SubPolyArea();
			subPolyArea.readExternal(in);
			outerAreas.add(subPolyArea);
		}
		final int innerSize = in.readInt();
		for (int i = 0; i < innerSize; ++i) {
			final SubPolyArea subPolyArea = new SubPolyArea();
			subPolyArea.readExternal(in);
			innerAreas.add(subPolyArea);
		}
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the bottomLeft
	 */
	@Override
	public Point getBottomLeft() {
		long minY = Long.MAX_VALUE;
		long minX = Long.MAX_VALUE;
		boolean first = true;
		for (final SubPolyArea way : outerAreas) {
			if (first) {
				minY = way.getBottomLeft().getY();
				minX = way.getBottomLeft().getX();
				first = false;
			} else {
				minY = Math.min(minY, way.getBottomLeft().getY());
				minX = Math.min(minX, way.getBottomLeft().getX());
			}
		}
		for (final SubPolyArea way : innerAreas) {
			if (first) {
				minY = way.getBottomLeft().getY();
				minX = way.getBottomLeft().getX();
				first = false;
			} else {
				minY = Math.min(minY, way.getBottomLeft().getY());
				minX = Math.min(minX, way.getBottomLeft().getX());
			}
		}
		return new Point(minX, minY);
	}

	/**
	 * @return the topRight
	 */
	@Override
	public Point getTopRight() {
		long minX = Long.MIN_VALUE;
		long minY = Long.MIN_VALUE;
		boolean first = true;
		for (final SubPolyArea way : outerAreas) {
			if (first) {
				minX = way.getTopRight().getX();
				minY = way.getTopRight().getY();
				first = false;
			} else {
				minX = Math.max(minX, way.getBottomLeft().getX());
				minY = Math.max(minY, way.getBottomLeft().getY());
			}
		}
		for (final SubPolyArea way : innerAreas) {
			if (first) {
				minX = way.getTopRight().getX();
				minY = way.getTopRight().getY();
				first = false;
			} else {
				minX = Math.max(minX, way.getBottomLeft().getX());
				minY = Math.max(minY, way.getBottomLeft().getY());
			}
		}
		return new Point(minX, minY);
	}

	@Override
	public byte[] getAsStream() {
		final ByteBuffer buffer;
		final byte ret[];
		int size = 6;
		final short sizeOuterArea = (short) outerAreas.size();
		final short sizeInnerArea = (short) innerAreas.size();
		final Point bottomLeft = getBottomLeft();
		short vertexCount = 0;
		short vertex = 0;
		int intVertexCount = 0;
		final short areaPartInfo[] = new short[sizeOuterArea + sizeInnerArea];
		boolean first = true;
		size += (sizeOuterArea + sizeInnerArea) * 2;
		for (final SubPolyArea outerArea : outerAreas) {
			final int polyType = outerArea.getPolyType(bottomLeft);
			if (first) {
				first = false;
				areaPartInfo[vertex] = (short) ((polyType << 13) + sizeOuterArea + sizeInnerArea);
			} else {
				areaPartInfo[vertex] = (short) ((polyType << 13) + vertexCount + vertex);
			}

			vertexCount += outerArea.getPoints().size();
			intVertexCount += outerArea.getPoints().size();
			vertex++;
			switch (polyType) {
			case 2:
				size += 4;
				break;
			case 4:
				size += 2;
				break;
			default:
				System.out.println("Unsupported polytype for area: " + polyType);
			}
		}
		for (final SubPolyArea innerArea : innerAreas) {
			final int polyType = innerArea.getPolyType(bottomLeft);
			areaPartInfo[vertex] = (short) ((polyType << 13) + vertexCount + vertex);
			vertexCount += innerArea.getPoints().size();
			intVertexCount += innerArea.getPoints().size();
			vertex++;
			switch (polyType) {
			case 2:
				size += 4;
				break;
			case 4:
				size += 2;
				break;
			default:
				System.out.println("Unsupported polytype for area: " + polyType);
			}
		}
		size += (vertexCount - vertex) * 2;
		if (textOffset != (byte) 0xff) {
			size += 2;
		}

		if (size < 0) {
			final StringBuilder builder = new StringBuilder();
			builder.append("Outer Areas \r\n");
			for (final SubPolyArea outerArea : outerAreas) {
				builder.append(outerArea.toJSON() + "\r\n");
			}
			builder.append("Inner Areas \r\n");
			for (final SubPolyArea innerArea : innerAreas) {
				builder.append(innerArea.toJSON() + "\r\n");
			}
			try {
				final FileWriter writer = new FileWriter("PolyArea.txt");
				writer.write(builder.toString());
				writer.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			System.out.println("VertexCount = " + intVertexCount);
		}
		ret = new byte[size];
		buffer = ByteBuffer.wrap(ret);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(textOffset); // TextIndex
		buffer.put(objectType); // Object Type
		buffer.putShort(vertexCount);
		for (final short s : areaPartInfo) {
			buffer.putShort(s);
		}

		for (final SubPolyArea outerArea : outerAreas) {
			buffer.put(outerArea.getAsStream(bottomLeft));
		}
		for (final SubPolyArea innerArea : innerAreas) {
			try {
				buffer.put(innerArea.getAsStream(bottomLeft));
			} catch (final java.nio.BufferOverflowException e) {
				System.out.println("Size = " + size + " pos = " + buffer.position() + " add "
						+ innerArea.getAsStream().length);
				throw e;
			}
		}

		buffer.put((byte) 0xff); // No contour Data present
		buffer.put((byte) 0); // Alignement
		if (textOffset != (byte) 0xff) {
			try {
				buffer.putShort(textIndex);
			} catch (final Throwable ex) {
				ex.printStackTrace();
			}
		}

		return ret;
	}

	public int getPointsCount() {
		int ret = 0;
		for (final SubPolyArea outerArea : outerAreas) {
			ret += outerArea.getPoints().size();
		}
		for (final SubPolyArea innerArea : innerAreas) {
			ret += innerArea.getPoints().size();
		}
		return ret;
	}
}

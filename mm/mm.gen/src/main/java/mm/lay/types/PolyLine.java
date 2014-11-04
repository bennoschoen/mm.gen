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
public class PolyLine extends BaseGraphic {
	protected final List<Point> points = new ArrayList<>();
	protected Point bottomLeft;
	protected Point topRight;

	public PolyLine() {
		bottomLeft = new Point(0, 0);
		topRight = new Point(0, 0);
	}

	public PolyLine(final String json) {
		fromJSON(json);
		graphics = Graphics.POLYLINE;
	}

	public PolyLine(final JSONObject json) {
		fromJSON(json);
		graphics = Graphics.POLYLINE;
	}

	public PolyLine(final String name, final List<Point> points) {
		super();
		long minX = Long.MAX_VALUE;
		long maxX = Long.MIN_VALUE;
		long minY = Long.MAX_VALUE;
		long maxY = Long.MIN_VALUE;

		this.name = name;
		this.points.clear();
		this.points.addAll(points);
		for (final Point point : points) {
			minX = Math.min(minX, point.getX());
			maxX = Math.max(maxX, point.getX());
			minY = Math.min(minY, point.getY());
			maxY = Math.max(maxY, point.getY());
		}
		bottomLeft = new Point(minX, minY);
		topRight = new Point(maxX, maxY);
	}

	@Override
	public JSONObject toJSON() {
		final JSONObject ret = super.toJSON();
		try {
			ret.put("minX", bottomLeft.getX());
			ret.put("minY", bottomLeft.getY());
			ret.put("maxX", topRight.getX());
			ret.put("maxY", topRight.getY());
			final JSONArray pa = new JSONArray();
			for (final Point point : points) {
				final JSONObject p = new JSONObject();
				p.put("x", point.getX());
				p.put("y", point.getY());
				pa.put(p);
			}
			ret.put("points", pa);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void fromJSON(final JSONObject json) {
		long x;
		long y;
		points.clear();
		try {
			super.fromJSON(json);
			x = json.getLong("minX");
			y = json.getLong("minY");
			bottomLeft = new Point(x, y);
			x = json.getLong("maxX");
			y = json.getLong("maxY");
			topRight = new Point(x, y);

			final JSONArray pa = json.getJSONArray("points");
			for (int i = 0; i < pa.length(); ++i) {
				final JSONObject p = pa.getJSONObject(i);
				x = p.getLong("x");
				y = p.getLong("y");
				points.add(new Point(x, y));
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
		out.writeLong(bottomLeft.getX());
		out.writeLong(bottomLeft.getY());
		out.writeLong(topRight.getX());
		out.writeLong(topRight.getY());
		out.writeInt(points.size());
		for (final Point point : points) {
			out.writeLong(point.getX());
			out.writeLong(point.getY());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mm.lay.types.BaseGraphic#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		long x;
		long y;
		points.clear();
		x = in.readLong();
		y = in.readLong();
		bottomLeft = new Point(x, y);
		x = in.readLong();
		y = in.readLong();
		topRight = new Point(x, y);
		final int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			x = in.readLong();
			y = in.readLong();
			points.add(new Point(x, y));
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
		return bottomLeft;
	}

	/**
	 * @return the topRight
	 */
	@Override
	public Point getTopRight() {
		return topRight;
	}

	/**
	 * @return the points
	 */
	public List<Point> getPoints() {
		return points;
	}

	public int getPolyType() {
		final Point firstPoint = points.get(0);
		final Point lastPoint = points.get(points.size() - 1);
		int ret = -1;

		if (firstPoint.equals(bottomLeft)) {
			if (lastPoint.equals(topRight)) {
				ret = 4;
			} else {
				ret = 3;
			}
		} else if (firstPoint.equals(topRight) && lastPoint.equals(bottomLeft)) {
			ret = 5;
		} else if (firstPoint.x == bottomLeft.x && firstPoint.y == topRight.y
				&& lastPoint.x == topRight.x && lastPoint.y == bottomLeft.y) {
			ret = 6;
		} else if (lastPoint.x == bottomLeft.x && lastPoint.y == topRight.y
				&& firstPoint.x == topRight.x && firstPoint.y == bottomLeft.y) {
			ret = 7;
		} else if (Math.max(Math.abs(firstPoint.x - bottomLeft.x),
				Math.abs(firstPoint.y - bottomLeft.y)) <= 255) {
			ret = 2;
		} else if (Math.max(Math.abs(firstPoint.x - bottomLeft.x),
				Math.abs(firstPoint.y - bottomLeft.y)) < 32767) {
			ret = 1;
		} else {
			ret = 0;
		}
		return ret;
	}

	@Override
	public byte[] getAsStream() {
		final ByteBuffer buffer;
		final byte ret[];
		final int polytype = getPolyType();
		final Point firstPoint = points.get(0);
		int size = 4;
		int count = points.size();
		switch (polytype) {
		case 0:
			size += 10;
			break;
		case 1:
			size += 6;
			break;
		case 2:
			size += 4;
			break;
		case 3:
			size += 2;
			break;
		default:
			break;
		}
		size += (points.size() - 2) * 2;
		if (textOffset != (byte) 0xff) {
			size += 2;
		}
		ret = new byte[size];
		buffer = ByteBuffer.wrap(ret);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(textOffset); // TextIndex
		buffer.put(objectType); // Object Type
		final int nvp = (polytype << 13) | points.size();
		buffer.putShort((short) nvp);
		switch (polytype) {
		case 0:
			buffer.putInt((int) (firstPoint.x - bottomLeft.x));
			buffer.putInt((int) (firstPoint.y - bottomLeft.y));
			break;
		case 1:
			buffer.putShort((short) (firstPoint.x - bottomLeft.x));
			buffer.putShort((short) (firstPoint.y - bottomLeft.y));
			break;
		case 2:
			buffer.put((byte) (firstPoint.x - bottomLeft.x));
			buffer.put((byte) (firstPoint.y - bottomLeft.y));
			break;
		case 3:
			break;
		default:
			count--;
			break;
		}
		Point lastPoint = firstPoint;
		for (int i = 1; i < count; ++i) {
			final Point point = points.get(i);
			buffer.put((byte) (point.x - lastPoint.x));
			buffer.put((byte) (point.y - lastPoint.y));
			lastPoint = point;
		}
		if (textOffset != (byte) 0xff) {
			try {
				buffer.putShort(textIndex);
			} catch (final Throwable ex) {
				ex.printStackTrace();
			}
		}
		// System.out.println("ret size = " + ret.length + "  writePos = " +
		// buffer.position());

		return ret;
	}
}

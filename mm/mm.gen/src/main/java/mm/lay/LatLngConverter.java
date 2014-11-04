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
 * @since 27.02.2014
 * @version 1.0
 * @author benno
 */
package mm.lay;

/**
 * @author benno
 * 
 */
public class LatLngConverter {
	protected long minX = 0;
	protected long maxX = 0;
	protected long minY = 0;
	protected long maxY = 0;
	protected double minLon = 0;
	protected double maxLon = 0;
	protected double minLat = 0;
	protected double maxLat = 0;

	// minX * scaleX = minLon
	protected double scaleX = 0;
	// minY * scaleY = minLat
	protected double scaleY = 0;

	public LatLngConverter(final long minX, final long maxX, final long minY, final long maxY,
			final double minLon, final double maxLon, final double minLat, final double maxLat) {
		super();
		double div = 0;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minLat = minLat;
		this.maxLat = maxLat;

		scaleX = 0;
		scaleY = 0;

		if (minX != 0) {
			scaleX = minLon / minX;
			div += 1;
		}
		if (maxX != 0) {
			scaleX = maxLon / maxX;
			div += 1;
		}
		if (div > 1) {
			scaleX /= div;
		}
		div = 0;
		if (minY != 0) {
			scaleY = minLat / minY;
			div += 1;
		}
		if (maxY != 0) {
			scaleY = maxLat / maxY;
			div += 1;
		}
		if (div > 1) {
			scaleY /= div;
		}
	}

	public LatLngConverter(final long minX, final long maxX, final long minY, final long maxY,
			final double scaleX, final double scaleY) {
		super();
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		minLon = minX * scaleX;
		maxLon = maxX * scaleX;
		minLat = minY * scaleY;
		maxLat = maxY * scaleY;
	}

	public LatLngConverter(final double minLon, final double maxLon, final double minLat,
			final double maxLat, final double scaleX, final double scaleY) {
		super();
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		if (scaleX != 0 && scaleY != 0) {
			minX = (long) (minLon / scaleX);
			maxX = (long) (maxLon / scaleX);
			minY = (long) (minLat / scaleY);
			maxY = (long) (maxLat / scaleY);
		}
	}

	public LatLngConverter(final double centerLon, final double centerLat, final long cellWidth,
			final long cellHeight, final double scaleX, final double scaleY, final int zoomLevel) {
		final long cells = (long) Math.pow(2, zoomLevel);
		long xCenter = (long) (centerLon / scaleX);
		long yCenter = (long) (centerLat / scaleY);

		xCenter -= xCenter % cellWidth;
		yCenter -= yCenter % cellHeight;

		minX = xCenter - cellWidth * cells / 2;
		maxX = xCenter + cellWidth * cells / 2;
		minY = yCenter - cellHeight * cells / 2;
		maxY = yCenter + cellHeight * cells / 2;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		minLon = minX * scaleX;
		maxLon = maxX * scaleX;
		minLat = minY * scaleY;
		maxLat = maxY * scaleY;
	}

	public long convertLatToLong(final double lat) {
		return (long) (lat / scaleY - 0.5);
	}

	public long convertLonToLong(final double lon) {
		return (long) (lon / scaleX + 0.5);
	}

	public double convertLatToDouble(final long lat) {
		return lat * scaleY;
	}

	public double convertLonToDouble(final long lon) {
		return lon * scaleX;
	}

	public double getRelLat(final long lat) {
		return convertLatToDouble(minY + lat);
	}

	public double getRelLon(final long lon) {
		return convertLonToDouble(minX + lon);
	}

	public Bounds getCellBounds(final int cellId) {
		LatLng bottomLeft = null;
		LatLng topRight = null;
		double bMinLat = minLat;
		double bMinLon = minLon;
		double bMaxLat = maxLat;
		double bMaxLon = maxLon;
		if (cellId > 1) {
			int count = 2;
			int side = 2;
			int cells2 = side * side;
			int cells2s = (side + 1) * (side + 1);
			while (cellId > count + cells2 + cells2s - 1) {
				side *= 2;
				count += cells2 + cells2s;
				cells2 = side * side;
				cells2s = (side + 1) * (side + 1);
			}
			final double cellWidth = (maxLon - minLon) / side;
			final double cellHeight = (maxLat - minLat) / side;
			double cellXOffset = 0;
			double cellYOffset = 0;
			double x = 0;
			double y = 0;
			if (cellId < count + cells2) {
				final int relCellId = cellId - count;
				x = relCellId % side;
				y = relCellId / side;
			} else {
				final int relCellId = cellId - count - cells2;
				x = relCellId % (side + 1);
				y = relCellId / (side + 1);
				cellXOffset = -cellWidth / 2;
				cellYOffset = -cellHeight / 2;
			}
			bMinLat = minLat + y * cellHeight + cellYOffset;
			bMinLon = minLon + x * cellWidth + cellXOffset;
			bMaxLat = bMinLat + cellHeight;
			bMaxLon = bMinLon + cellWidth;
		}
		bottomLeft = new LatLng(Math.max(bMinLat, minLat), Math.max(bMinLon, minLon));
		topRight = new LatLng(Math.min(bMaxLat, maxLat), Math.min(bMaxLon, maxLon));
		return new Bounds(bottomLeft, topRight);
	}
}

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
 * @since 22.03.2014
 * @version 1.0
 * @author benno
 */
package mm.lay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mm.gen.db.BoundingBox;

/**
 * @author benno
 * 
 */
public class RelCoordConverter {
	protected long minX = 0;
	protected long maxX = 0;
	protected long minY = 0;
	protected long maxY = 0;
	protected final double minLon;
	protected final double maxLon;
	protected final double minLat;
	protected final double maxLat;

	// minX * scaleX = minLon
	protected double scaleX = 0;
	// minY * scaleY = minLat
	protected double scaleY = 0;
	protected int zoomLevel;
	protected List<CellLayer> cellLayers = new ArrayList<>();

	public RelCoordConverter(final long minX, final long maxX, final long minY, final long maxY,
			final double minLon, final double maxLon, final double minLat, final double maxLat,
			final double scaleX, final double scaleY, final int zoomLevel) {
		super();
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		this.zoomLevel = zoomLevel;
		int minCell = 1;
		long width = maxX - minX;
		long height = maxY - minY;
		final long left = minX;
		final long bottom = minY;
		long lCells = 1;
		for (int i = 0; i < zoomLevel; ++i) {
			cellLayers.add(new CellLayer(width, height, minCell, lCells, left, bottom, true));
			minCell += lCells * lCells;
			if (i >= 1) {
				cellLayers.add(new CellLayer(width, height, minCell, lCells + 1, left - width / 2,
						bottom - height / 2, false));
				minCell += (lCells + 1) * (lCells + 1);
			}

			lCells *= 2;
			width /= 2;
			height /= 2;
			System.out.println(String.format("layer %d, width %d, height %d", i, width, height));
		}

	}

	public RelCoordConverter(final double centerLon, final double centerLat, final long cellWidth,
			final long cellHeight, final double scaleX, final double scaleY, final int zoomLevel) {
		long cells = 1;
		final long xCenter = (long) (centerLon / scaleX + 0.5);
		final long yCenter = (long) (centerLat / scaleY + 0.5);
		this.zoomLevel = zoomLevel;
		for (int i = 0; i < zoomLevel; ++i) {
			cells *= 2;
		}

		if (cellWidth % 2 != 0 || cellHeight % 2 != 0) {
			throw new IllegalArgumentException("Cell Width and CellHeight must be devidable by 2");
		}

		// xCenter -= xCenter % cellWidth;
		// yCenter -= yCenter % cellHeight;

		minX = xCenter - cellWidth * cells / 2;
		maxX = minX + cellWidth * cells;
		minY = yCenter - cellHeight * cells / 2;
		maxY = minY + cellHeight * cells;
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		minLon = minX * scaleX;
		maxLon = maxX * scaleX;
		minLat = minY * scaleY;
		maxLat = maxY * scaleY;

		int minCell = 1;
		long width = cellWidth * cells;
		long height = cellHeight * cells;
		final long left = minX;
		final long bottom = minY;
		long lCells = 1;
		for (int i = 0; i < zoomLevel; ++i) {
			cellLayers.add(new CellLayer(width, height, minCell, lCells, left, bottom, true));
			minCell += lCells * lCells;
			if (i >= 1) {
				cellLayers.add(new CellLayer(width, height, minCell, lCells + 1, left - width / 2,
						bottom - height / 2, false));
				minCell += (lCells + 1) * (lCells + 1);
			}

			lCells *= 2;
			width /= 2;
			height /= 2;
			System.out.println(String.format("layer %d, width %d, height %d", i, width, height));
		}
	}

	public long getLatRel(final double lat) {
		return (long) (lat / scaleY + 0.5);
	}

	public long getLonRel(final double lon) {
		return (long) (lon / scaleX + 0.5);
	}

	public int getCellId(final long minX, final long minY, final long maxX, final long maxY) {
		int ret = -1;
		for (final CellLayer cellLayer : cellLayers) {
			ret = Math.max(ret, cellLayer.getCellId(minX, minY, maxX, maxY));
		}
		// if (ret == -1) {
		// System.out.println(String.format("Line  %d,%d   %d,%d", minX, minY,
		// maxX, maxY));
		// System.out.println(String.format("Cell1 %d,%d   %d,%d", this.minX,
		// this.minY,
		// this.maxX, this.maxY));
		// }
		return ret;
	}

	public BoundingBox getBoundingBox() {
		return new BoundingBox(minLat, minLon, maxLat, maxLon);
	}

	public CellBounds getCellBounds(final int cellId) {
		CellBounds ret = null;
		for (final Iterator<CellLayer> iter = cellLayers.iterator(); iter.hasNext() && ret == null;) {
			final CellLayer cellLayer = iter.next();
			final long firstCell = cellLayer.minCellNumber;
			final long dim = cellLayer.cells;
			if (firstCell <= cellId && firstCell + dim * dim > cellId) {
				final long xLayer = (cellId - firstCell) % dim;
				final long yLayer = (cellId - firstCell) / dim;
				final int lx = (int) (cellLayer.left + xLayer * cellLayer.cellWidth);
				final int ly = (int) (cellLayer.bottom + yLayer * cellLayer.cellHeight);
				ret = new CellBounds(lx, ly, (int) cellLayer.cellWidth, (int) cellLayer.cellHeight);
			}
		}
		return ret;
	}

	private class CellLayer {
		protected long cellWidth;
		protected long cellHeight;
		protected long minCellNumber;
		protected long cells;
		protected long bottom;
		protected long left;
		protected long first;
		protected long last;

		public CellLayer(final long cellWidth, final long cellHeight, final long minCellNumber,
				final long cells, final long left, final long bottom,
				final boolean firstAndLastAllowed) {
			super();
			this.cellWidth = cellWidth;
			this.cellHeight = cellHeight;
			this.minCellNumber = minCellNumber;
			this.cells = cells;
			this.bottom = bottom;
			this.left = left;
			if (firstAndLastAllowed) {
				first = 0;
				last = cells - 1;
			} else {
				first = 1;
				last = cells - 2;
			}
		}

		public int getCellId(final long minX, final long minY, final long maxX, final long maxY) {
			int ret = -1;
			final long bWidth = Math.max(0, maxX - minX - 1);
			final long bHeight = Math.max(0, maxY - minY - 1);

			if (bWidth > cellWidth || bHeight > cellHeight) {
				ret = -1;
			} else {
				final long relX = minX - left;
				final long relY = minY - bottom;
				final long x1 = relX / cellWidth;
				final long y1 = relY / cellHeight;
				final long x2 = (relX + bWidth) / cellWidth;
				final long y2 = (relY + bHeight) / cellHeight;
				if (x1 == x2 && y1 == y2 && x1 >= first && x1 <= last && y1 >= first && y1 <= last) {
					ret = (int) (minCellNumber + y1 * cells + x1);
				}
			}
			return ret;
		}
	}

	/**
	 * @return the minX
	 */
	public long getMinX() {
		return minX;
	}

	/**
	 * @param minX
	 *            the minX to set
	 */
	public void setMinX(final long minX) {
		this.minX = minX;
	}

	/**
	 * @return the maxX
	 */
	public long getMaxX() {
		return maxX;
	}

	/**
	 * @param maxX
	 *            the maxX to set
	 */
	public void setMaxX(final long maxX) {
		this.maxX = maxX;
	}

	/**
	 * @return the minY
	 */
	public long getMinY() {
		return minY;
	}

	/**
	 * @param minY
	 *            the minY to set
	 */
	public void setMinY(final long minY) {
		this.minY = minY;
	}

	/**
	 * @return the maxY
	 */
	public long getMaxY() {
		return maxY;
	}

	/**
	 * @param maxY
	 *            the maxY to set
	 */
	public void setMaxY(final long maxY) {
		this.maxY = maxY;
	}

	/**
	 * @return the minLon
	 */
	public double getMinLon() {
		return minLon;
	}

	/**
	 * @return the maxLon
	 */
	public double getMaxLon() {
		return maxLon;
	}

	/**
	 * @return the minLat
	 */
	public double getMinLat() {
		return minLat;
	}

	/**
	 * @return the maxLat
	 */
	public double getMaxLat() {
		return maxLat;
	}

	/**
	 * @return the scaleX
	 */
	public double getScaleX() {
		return scaleX;
	}

	/**
	 * @param scaleX
	 *            the scaleX to set
	 */
	public void setScaleX(final double scaleX) {
		this.scaleX = scaleX;
	}

	/**
	 * @return the scaleY
	 */
	public double getScaleY() {
		return scaleY;
	}

	/**
	 * @param scaleY
	 *            the scaleY to set
	 */
	public void setScaleY(final double scaleY) {
		this.scaleY = scaleY;
	}

	/**
	 * @return the zoomLevel
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * @param zoomLevel
	 *            the zoomLevel to set
	 */
	public void setZoomLevel(final int zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
}

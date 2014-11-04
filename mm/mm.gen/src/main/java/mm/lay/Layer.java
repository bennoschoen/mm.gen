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
 * @since 04.04.2014
 * @version 1.0
 * @author benno
 */
package mm.lay;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import mm.gen.db.WayClassifier.Graphics;
import mm.lay.types.Carrier;

/**
 * @author benno
 * 
 */
public class Layer {

	protected final RandomAccessFile rao;
	protected final CellTable cellTable;
	protected final byte objType;
	protected final Graphics graphics;
	protected final byte layerType;

	protected byte header[] = new byte[4];
	protected int category = CategoryType.NORMAL_LAYER.getType();
	protected short fileIdentifier = (short) 0xC000;
	protected float lonLeft = 0;
	protected float lonRight = 0;
	protected float latTop = 0;
	protected float latBottom = 0;
	protected short numberOfLevels = 0;
	protected int numberOfObjects = 0;
	protected double scaleLon = 0;
	protected double scaleLat = 0;
	protected float refLon = 0;
	protected float refLat = 0;
	protected int left = 0;
	protected int top = 0;
	protected int right = 0;
	protected int bottom = 0;
	protected int largestCellSize = 0;
	protected int firstCellId = 0;
	protected Cell currentCell = null;

	protected boolean firstCellWait = true;
	protected int lastCellId = 0;
	protected final static int headerSize = 128;

	protected final RelCoordConverter coordConverter;

	protected final static int CELL_PER_TURN_AROUND = 1000;

	public static enum CategoryType {
		NORMAL_LAYER(0), ARTIFICIAL_LAYER(1);

		protected final int type;

		private CategoryType(final int type) {
			this.type = type;
		}

		/**
		 * @return the id
		 */
		public int getType() {
			return type;
		}
	};

	public Layer(final File f, final RelCoordConverter coordConverter, final Graphics graphics,
			final byte objType) throws IOException {
		String path = f.getAbsolutePath();
		path = path.substring(0, path.lastIndexOf('.')) + ".clt";
		if (f.exists()) {
			f.delete();
		}
		rao = new RandomAccessFile(f, "rw");
		cellTable = new CellTable(new File(path));
		lonLeft = (float) coordConverter.getMinLon();
		lonRight = (float) coordConverter.getMaxLon();
		latTop = (float) coordConverter.getMaxLat();
		latBottom = (float) coordConverter.getMinLat();
		numberOfLevels = (short) (coordConverter.getZoomLevel() - 1);
		scaleLon = coordConverter.getScaleX();
		scaleLat = coordConverter.getScaleY();
		refLon = 0;
		refLat = 0;
		left = (int) coordConverter.getMinX();
		top = (int) coordConverter.getMaxY();
		right = (int) coordConverter.getMaxX();
		bottom = (int) coordConverter.getMinY();
		this.objType = objType;
		this.coordConverter = coordConverter;
		this.graphics = graphics;
		layerType = graphics.getType();
		rao.seek(512);
	}

	public void writeHeader() {
		final byte hb[] = new byte[headerSize];
		final ByteBuffer headerBuffer = ByteBuffer.wrap(hb);
		headerBuffer.order(ByteOrder.LITTLE_ENDIAN);
		try {
			headerBuffer.put("MHGO".getBytes());
			headerBuffer.putInt(category);
			headerBuffer.putShort(fileIdentifier);
			headerBuffer.putFloat(lonLeft);
			headerBuffer.putFloat(lonRight);
			headerBuffer.putFloat(latBottom);
			headerBuffer.putFloat(latTop);
			if (numberOfObjects > 0) {
				headerBuffer.putShort(numberOfLevels);
			} else {
				headerBuffer.putShort((short) 0);
			}

			headerBuffer.putInt(numberOfObjects);
			headerBuffer.putDouble(scaleLon);
			headerBuffer.putDouble(scaleLat);
			headerBuffer.putFloat(refLon);
			headerBuffer.putFloat(refLat);
			headerBuffer.putInt(left);
			headerBuffer.putInt(bottom);
			headerBuffer.putInt(right);
			headerBuffer.putInt(top);
			headerBuffer.put(layerType);
			headerBuffer.put((byte) 0);
			headerBuffer.putInt(largestCellSize);
			headerBuffer.putInt(firstCellId);
			headerBuffer.putInt(lastCellId);
			rao.seek(0);
			rao.write(hb);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	protected Cell[] getCellArray(final int firstCell, final int amount, final Graphics graphics,
			final byte objectType) {
		final Cell ret[] = new Cell[amount];
		for (int i = 0; i < amount; ++i) {
			ret[i] = new Cell(graphics, i + firstCell, objectType);
		}
		return ret;
	}

	protected void finishCurrentCell() {
		try {
			final byte buffer[];
			final int numberOfElements;
			switch (graphics) {
			case AREA:
			case POLYLINE:
				buffer = currentCell.getCellStream(coordConverter.getCellBounds(currentCell
						.getCellId()));
				numberOfElements = currentCell.getNumberOfBaseGraphics();
				break;
			case LABELS:
			case POI:
			case POINT:
			default:
				numberOfElements = 0;
				buffer = null;
				break;
			}

			if (buffer != null && buffer.length > 0) {
				final int cellId = currentCell.getCellId();
				cellTable.addCellPoint(cellId, (int) rao.getFilePointer(), buffer.length);
				rao.write(buffer);
				if (firstCellWait) {
					firstCellId = cellId;
					firstCellWait = false;
				}
				lastCellId = cellId;
				numberOfObjects += numberOfElements;
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void addCarrier(final Carrier carrier) {
		if (!graphics.equals(carrier.getGraphics())) {
			throw new IllegalArgumentException("Graphic of Carrier does not match "
					+ carrier.getGraphics().name());
		}
		if (currentCell == null) {
			currentCell = new Cell(graphics, carrier.getCellId(), carrier.getObjectType());
		} else if (currentCell.getCellId() != carrier.getCellId()) {
			finishCurrentCell();
			currentCell = new Cell(graphics, carrier.getCellId(), carrier.getObjectType());
		}
		currentCell.addBaseGraphics(carrier.getBaseElement());
	}

	public void finish() {
		try {
			if (currentCell != null) {
				finishCurrentCell();
			}
			largestCellSize = cellTable.getMaxCellLength();
			writeHeader();
			rao.close();
			cellTable.writeTable();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the layerType
	 */
	public byte getLayerType() {
		return layerType;
	}

	/**
	 * @return the largestCellSize
	 */
	public int getLargestCellSize() {
		return largestCellSize;
	}

	/**
	 * @param largestCellSize
	 *            the largestCellSize to set
	 */
	public void setLargestCellSize(final int largestCellSize) {
		this.largestCellSize = largestCellSize;
	}

	/**
	 * @return the firstCellId
	 */
	public int getFirstCellId() {
		return firstCellId;
	}

	/**
	 * @param firstCellId
	 *            the firstCellId to set
	 */
	public void setFirstCellId(final int firstCellId) {
		this.firstCellId = firstCellId;
	}

	/**
	 * @return the lastCellId
	 */
	public int getLastCellId() {
		return lastCellId;
	}

	/**
	 * @param lastCellId
	 *            the lastCellId to set
	 */
	public void setLastCellId(final int lastCellId) {
		this.lastCellId = lastCellId;
	}

	/**
	 * @return the fileIdentifier
	 */
	public short getFileIdentifier() {
		return fileIdentifier;
	}

	/**
	 * @param fileIdentifier
	 *            the fileIdentifier to set
	 */
	public void setFileIdentifier(final short fileIdentifier) {
		this.fileIdentifier = fileIdentifier;
	}
}

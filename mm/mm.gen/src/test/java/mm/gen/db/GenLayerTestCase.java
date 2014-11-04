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
 * @since 21.04.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.util.Locale;

import junit.framework.TestCase;
import mm.lay.Bounds;
import mm.lay.CellBounds;
import mm.lay.LatLngConverter;
import mm.lay.RelCoordConverter;

/**
 * @author benno
 * 
 */
public class GenLayerTestCase extends TestCase {

	final int zoomLevel = 4;
	final long cellHeight = 1150;
	final long cellWidth = 2250;
	final double scale = 9.0e-6;

	double lonLeft = 9.78075;
	double lonRight = 10.104750000000001;
	double latTop = -52.05015;
	double latBottom = -52.21575;
	double scaleLon = scale;
	double scaleLat = scale;

	String jsonWay = "{\"id\":25966644,\"nodes\":[{\"id\":75404288,\"lon\":9.9807707,\"props\":[],\"lat\":-52.1150184},{\"id\":75404290,\"lon\":9.9805955,\"props\":[{\"v\":\"JOSM\",\"k\":\"created_by\"}],\"lat\":-52.1144857},{\"id\":261711208,\"lon\":9.9804724,\"props\":[{\"v\":\"JOSM\",\"k\":\"created_by\"}],\"lat\":-52.1142351}],\"props\":[{\"v\":\"residential\",\"k\":\"highway\"},{\"v\":\"Domänenstraße\",\"k\":\"name\"}]}";

	protected LatLngConverter converter = new LatLngConverter(lonLeft, lonRight, latBottom, latTop,
			scaleLon, scaleLat);
	final RelCoordConverter relConverter = new RelCoordConverter((lonLeft + lonRight) / 2,
			(latBottom + latTop) / 2, cellWidth, cellHeight, scale, scale, zoomLevel);

	public void testCellConverter() {
		final int cell = 7;
		final CellBounds cellBounds = relConverter.getCellBounds(cell);
		final Bounds bounds = converter.getCellBounds(cell);

		final double cBottom = converter.convertLatToDouble(cellBounds.getY());
		final double cLeft = converter.convertLonToDouble(cellBounds.getX());
		final double cTop = converter
				.convertLatToDouble(cellBounds.getY() + cellBounds.getHeight());
		final double cRight = converter.convertLonToDouble(cellBounds.getX()
				+ cellBounds.getWidth());

		System.out.println(String.format(Locale.US, "%.5f, %.5f  %.5f, %.5f", -cBottom, cLeft,
				-cTop, cRight));
		System.out.println(String.format(Locale.US, "%.5f, %.5f  %.5f, %.5f", -bounds
				.getBottomLeft().getLat(), bounds.getBottomLeft().getLon(), -bounds.getTopRight()
				.getLat(), bounds.getTopRight().getLon()));
		System.out.println(String.format(Locale.US, "%d, %d",
				converter.convertLatToLong(bounds.getBottomLeft().getLat()),
				converter.convertLonToLong(bounds.getBottomLeft().getLon())));
	}
}

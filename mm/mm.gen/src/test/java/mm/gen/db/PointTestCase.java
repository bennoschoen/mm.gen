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
package mm.gen.db;

import java.util.List;

import junit.framework.TestCase;
import mm.lay.Point;

/**
 * @author benno
 * 
 */
public class PointTestCase extends TestCase {

	public void testInterMediate() {
		final List<Point> points = Point.interMediatePoint(new Point(0, 0), new Point(15, 0), 4);
		for (final Point point : points) {
			System.out.println(point);
		}
	}
}

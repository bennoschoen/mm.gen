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
package mm.lay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author benno
 * 
 */
public class Point {
	public final long x;
	public final long y;

	public Point(final long x, final long y) {
		super();
		this.x = x;
		this.y = y;
	}

	/**
	 * @return the x
	 */
	public long getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public long getY() {
		return y;
	}

	public static List<Point> interMediatePoint(final Point p1, final Point p2, final long maxDist) {
		final List<Point> ret = new ArrayList<>();
		final long max = Math.max(Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
		if (max > maxDist) {
			final long number = (max + maxDist - 1) / maxDist;
			final double deltaX = (double) (p2.x - p1.x) / (double) number;
			final double deltaY = (double) (p2.y - p1.y) / (double) number;
			for (int i = 1; i < number; ++i) {
				ret.add(new Point((long) (p1.x + i * deltaX + 0.5),
						(long) (p1.y + i * deltaY + 0.5)));
			}
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Point [x=" + x + ", y=" + y + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (x ^ (x >>> 32));
		result = prime * result + (int) (y ^ (y >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Point other = (Point) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}

}

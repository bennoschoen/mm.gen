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
 * @since 01.03.2014
 * @version 1.0
 * @author benno
 */
package mm.lay;

/**
 * @author benno
 * 
 */
public class Bounds {
	protected final LatLng bottomLeft;
	protected final LatLng topRight;

	public Bounds(final LatLng bottomLeft, final LatLng topRight) {
		super();
		this.bottomLeft = bottomLeft;
		this.topRight = topRight;
	}

	/**
	 * @return the bottomLeft
	 */
	public LatLng getBottomLeft() {
		return bottomLeft;
	}

	/**
	 * @return the topRight
	 */
	public LatLng getTopRight() {
		return topRight;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bounds [bottomLeft=" + bottomLeft + ", topRight=" + topRight + "]";
	}

}

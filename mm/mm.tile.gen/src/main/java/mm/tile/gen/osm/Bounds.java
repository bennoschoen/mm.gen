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
 * @since 28.09.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen.osm;

/**
 * @author benno
 * 
 */
public class Bounds {
	int top = Integer.MIN_VALUE;
	int right = Integer.MIN_VALUE;
	int bottom = Integer.MAX_VALUE;
	int left = Integer.MAX_VALUE;

	public Bounds() {

	}

	public Bounds(final Bounds bounds) {
		super();
		bottom = bounds.bottom;
		left = bounds.left;
		top = bounds.top;
		right = bounds.right;
	}

	public Bounds(final int bottom, final int left, final int top, final int right) {
		super();
		this.bottom = bottom;
		this.left = left;
		this.top = top;
		this.right = right;
	}

	public void increaseBounds(final int vert, final int horiz) {
		if (top < vert) {
			top = vert;
		}
		if (right < horiz) {
			right = horiz;
		}
		if (bottom > vert) {
			bottom = vert;
		}
		if (left > horiz) {
			left = horiz;
		}
	}

	public void increaseBounds(final Bounds bounds) {
		if (top < bounds.top) {
			top = bounds.top;
		}
		if (right < bounds.right) {
			right = bounds.right;
		}
		if (bottom > bounds.bottom) {
			bottom = bounds.bottom;
		}
		if (left > bounds.left) {
			left = bounds.left;
		}
	}

	/**
	 * @return the left
	 */
	public int getLeft() {
		return left;
	}

	/**
	 * @param left
	 *            the left to set
	 */
	public void setLeft(final int left) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public int getRight() {
		return right;
	}

	/**
	 * @param right
	 *            the right to set
	 */
	public void setRight(final int right) {
		this.right = right;
	}

	/**
	 * @return the top
	 */
	public int getTop() {
		return top;
	}

	/**
	 * @param top
	 *            the top to set
	 */
	public void setTop(final int top) {
		this.top = top;
	}

	/**
	 * @return the bottom
	 */
	public int getBottom() {
		return bottom;
	}

	/**
	 * @param bottom
	 *            the bottom to set
	 */
	public void setBottom(final int bottom) {
		this.bottom = bottom;
	}
}

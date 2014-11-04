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
 * @since 29.06.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.util.ArrayList;
import java.util.List;

import mm.gen.osm.MultiWay;
import mm.gen.osm.Way;
import mm.lay.LatLng;

/**
 * @author benno
 * 
 */
public class WayShredder {
	private final BoundingBox boundingBox;
	private final double maxWidth;
	private final double maxHeight;
	private final boolean area;

	public WayShredder(final boolean area, final BoundingBox boundingBox, final double maxWidth,
			final double maxHeight) {
		super();
		this.area = area;
		this.boundingBox = boundingBox;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}

	protected List<Way> splitAtLongitude(final Way way, final double longitude) {
		final List<Way> ret = new ArrayList<>();
		final BoundingBox leftBox = new BoundingBox(-1000, -1000, 1000, longitude);
		final BoundingBox rightBox = new BoundingBox(-1000, longitude, 1000, 1000);
		if (area) {
			ret.addAll(leftBox.cutArea(way));
			ret.addAll(rightBox.cutArea(way));
		} else {
			ret.addAll(leftBox.cutWay(way));
			ret.addAll(rightBox.cutWay(way));
		}
		return ret;
	}

	protected List<Way> splitAtLatitude(final Way way, final double latitude) {
		final List<Way> ret = new ArrayList<>();
		final BoundingBox leftBox = new BoundingBox(-1000, -1000, latitude, 1000);
		final BoundingBox rightBox = new BoundingBox(latitude, -1000, 1000, 1000);
		if (area) {
			ret.addAll(leftBox.cutArea(way));
			ret.addAll(rightBox.cutArea(way));
		} else {
			ret.addAll(leftBox.cutWay(way));
			ret.addAll(rightBox.cutWay(way));
		}
		return ret;
	}

	public List<Way> shredderWay(final MultiWay way) {
		List<Way> ret = new ArrayList<>();
		if (way.isEmpty()) {
			return ret;
		}
		ret.addAll(way.getOuterWays());
		final LatLng wayTR = way.getTopRight();
		final LatLng wayBL = way.getBottomLeft();

		// shredder longitude
		if (Math.abs(wayBL.getLon() - wayTR.getLon()) > maxWidth) {
			for (double lon = wayBL.getLon(); lon < wayTR.getLon(); lon += maxWidth * 0.9) {
				final List<Way> temp = new ArrayList<>();
				for (final Way way2 : ret) {
					final LatLng topRight = way2.getTopRight();
					final LatLng bottomLeft = way2.getBottomLeft();
					if (topRight.getLon() >= boundingBox.getMinLon()
							&& bottomLeft.getLon() <= boundingBox.getMaxLon()) {
						temp.addAll(splitAtLongitude(way2, lon));
					}
				}
				ret = temp;
			}
		}

		// shredder latitude
		if (Math.abs(wayBL.getLat() - wayTR.getLat()) > maxHeight) {
			for (double lat = wayBL.getLat(); lat < wayTR.getLat(); lat += maxHeight * 0.9) {
				final List<Way> temp = new ArrayList<>();
				for (final Way way2 : ret) {
					final LatLng topRight = way2.getTopRight();
					final LatLng bottomLeft = way2.getBottomLeft();
					if (topRight.getLat() >= boundingBox.getMinLat()
							&& bottomLeft.getLat() <= boundingBox.getMaxLat()) {
						temp.addAll(splitAtLatitude(way2, lat));
					}
				}
				ret = temp;
			}
		}
		final List<Way> temp = new ArrayList<>();
		for (final Way way2 : ret) {
			if (area) {
				temp.addAll(boundingBox.cutArea(way2));
			} else {
				temp.addAll(boundingBox.cutWay(way2));
			}

		}
		boolean wayIsDamaged = false;
		for (final Way way2 : temp) {
			final LatLng bottomLeft = way2.getBottomLeft();
			final LatLng topRight = way2.getTopRight();
			final double width = Math.abs(bottomLeft.getLon() - topRight.getLon());
			final double height = Math.abs(bottomLeft.getLat() - topRight.getLat());
			if (width > maxWidth || height > maxHeight) {
				wayIsDamaged = true;
			}
		}
		if (wayIsDamaged) {
			System.out.println("Damaged Way : " + way.toJSON().toString());
			ret.clear();
		} else {
			ret = temp;
		}
		return ret;
	}
}

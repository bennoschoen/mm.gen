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
public class MultiWayShredder {
	private final BoundingBox boundingBox;
	private final double maxWidth;
	private final double maxHeight;

	public MultiWayShredder(final BoundingBox boundingBox, final double maxWidth,
			final double maxHeight) {
		super();
		this.boundingBox = boundingBox;
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}

	protected List<MultiWay> splitAtLongitude(final MultiWay way, final double longitude) {
		final BoundingBox leftBox = new BoundingBox(-1000, -1000, 1000, longitude);
		final BoundingBox rightBox = new BoundingBox(-1000, longitude, 1000, 1000);
		final List<MultiWay> ret = new ArrayList<>();
		final MultiWay leftWay = new MultiWay();
		final MultiWay rightWay = new MultiWay();
		final List<Way> leftInnerWays = new ArrayList<>();
		final List<Way> rightInnerWays = new ArrayList<>();
		final List<Way> leftOuterWays = new ArrayList<>();
		final List<Way> rightOuterWays = new ArrayList<>();
		leftWay.setProps(way.getProps());
		rightWay.setProps(way.getProps());
		for (final Way innerWay : way.getInnerWays()) {
			leftInnerWays.addAll(leftBox.cutArea(innerWay));
			rightInnerWays.addAll(rightBox.cutArea(innerWay));
		}
		for (final Way outerWay : way.getOuterWays()) {
			leftOuterWays.addAll(leftBox.cutArea(outerWay));
			rightOuterWays.addAll(rightBox.cutArea(outerWay));
		}
		if (!leftOuterWays.isEmpty()) {
			leftWay.setInnerWays(leftInnerWays);
			leftWay.setOuterWays(leftOuterWays);
			ret.add(leftWay);
		}
		if (!rightOuterWays.isEmpty()) {
			rightWay.setInnerWays(rightInnerWays);
			rightWay.setOuterWays(rightOuterWays);
			ret.add(rightWay);
		}
		return ret;
	}

	protected List<MultiWay> splitAtLatitude(final MultiWay way, final double latitude) {
		final BoundingBox lowerBox = new BoundingBox(-1000, -1000, latitude, 1000);
		final BoundingBox upperBox = new BoundingBox(latitude, -1000, 1000, 1000);
		final List<MultiWay> ret = new ArrayList<>();
		final MultiWay leftWay = new MultiWay();
		final MultiWay rightWay = new MultiWay();
		final List<Way> leftInnerWays = new ArrayList<>();
		final List<Way> rightInnerWays = new ArrayList<>();
		final List<Way> leftOuterWays = new ArrayList<>();
		final List<Way> rightOuterWays = new ArrayList<>();
		leftWay.setProps(way.getProps());
		rightWay.setProps(way.getProps());
		for (final Way innerWay : way.getInnerWays()) {
			leftInnerWays.addAll(lowerBox.cutArea(innerWay));
			rightInnerWays.addAll(upperBox.cutArea(innerWay));
		}
		for (final Way outerWay : way.getOuterWays()) {
			leftOuterWays.addAll(lowerBox.cutArea(outerWay));
			rightOuterWays.addAll(upperBox.cutArea(outerWay));
		}
		if (!leftOuterWays.isEmpty()) {
			leftWay.setInnerWays(leftInnerWays);
			leftWay.setOuterWays(leftOuterWays);
			ret.add(leftWay);
		}
		if (!rightOuterWays.isEmpty()) {
			rightWay.setInnerWays(rightInnerWays);
			rightWay.setOuterWays(rightOuterWays);
			ret.add(rightWay);
		}
		return ret;
	}

	public List<MultiWay> shredderWay(final MultiWay way) {
		List<MultiWay> ret = new ArrayList<>();
		if (way.isEmpty()) {
			return ret;
		}
		ret.add(way);
		final LatLng wayTR = way.getTopRight();
		final LatLng wayBL = way.getBottomLeft();

		// shredder longitude
		if (Math.abs(wayBL.getLon() - wayTR.getLon()) > maxWidth) {
			for (double lon = wayBL.getLon(); lon < wayTR.getLon(); lon += maxWidth * 0.9) {
				final List<MultiWay> temp = new ArrayList<>();
				for (final MultiWay way2 : ret) {
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
				final List<MultiWay> temp = new ArrayList<>();
				for (final MultiWay way2 : ret) {
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
		final List<MultiWay> temp = new ArrayList<>();
		for (final MultiWay way2 : ret) {
			final List<Way> outerWays = new ArrayList<>();
			final List<Way> innerWays = new ArrayList<>();
			for (final Way way3 : way2.getInnerWays()) {
				innerWays.addAll(boundingBox.cutArea(way3));
			}
			for (final Way way3 : way2.getOuterWays()) {
				outerWays.addAll(boundingBox.cutArea(way3));
			}
			way2.setInnerWays(innerWays);
			way2.setOuterWays(outerWays);
			if (!way2.isEmpty()) {
				temp.add(way2);
			}
		}
		boolean wayIsDamaged = false;
		for (final MultiWay way2 : temp) {
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

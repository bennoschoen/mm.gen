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
 * @since 15.03.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mm.gen.osm.Node;
import mm.gen.osm.Way;
import mm.lay.LatLng;

/**
 * @author benno
 * 
 */
public class BoundingBox {

	protected final double minLat;
	protected final double minLon;
	protected final double maxLat;
	protected final double maxLon;

	public BoundingBox(final double minLat, final double minLon, final double maxLat,
			final double maxLon) {
		super();
		this.minLat = minLat;
		this.minLon = minLon;
		this.maxLat = maxLat;
		this.maxLon = maxLon;
	}

	/**
	 * Returns the quadrant relative to the bounding box, where the point lays
	 * 
	 * @param lat
	 * @param lon
	 * @return
	 */
	public int quadrant(final double lat, final double lon) {
		int xRet = 0;
		int yRet = 0;
		if (lon <= minLon) {
			xRet = 0;
		} else if (lon < maxLon) {
			xRet = 1;
		} else {
			xRet = 2;
		}
		if (lat <= minLat) {
			yRet = 0;
		} else if (lat < maxLat) {
			yRet = 3;
		} else {
			yRet = 6;
		}
		return yRet + xRet;
	}

	/**
	 * a + lambda * ab = c
	 * 
	 * @param a
	 *            start point of the vector
	 * @param b
	 *            end point of the vector
	 * @param c
	 *            intermediate point
	 * @return scale factor for the vector ab to reach c
	 */
	protected double getLambda(final double a, final double b, final double c) {
		final double ab = b - a;
		double ret = 0;
		if (ab != 0) {
			ret = (c - a) / ab;
		}
		return ret;
	}

	protected double squareDistance(final double a1, final double b1, final double a2,
			final double b2) {
		return Math.pow(a2 - a1, 2) + Math.pow(b2 - b1, 2);
	}

	protected LatLng getNearerPoint(final LatLng base, final LatLng a, final LatLng b) {
		final double distA = squareDistance(base.getLat(), base.getLon(), a.getLat(), a.getLon());
		final double distB = squareDistance(base.getLat(), base.getLon(), b.getLat(), b.getLon());
		return distA < distB ? a : b;
	}

	protected LatLng getBorder1Point(final LatLng in, final LatLng out) {
		if (quadrant(in.getLat(), in.getLon()) != 4) {
			throw new InvalidParameterException("Point in should be inside of bounding box");
		}
		final double lambda = getLambda(in.getLat(), out.getLat(), minLat);
		return new LatLng(minLat, in.getLon() + lambda * (out.getLon() - in.getLon()));
	}

	protected LatLng getBorder7Point(final LatLng in, final LatLng out) {
		if (quadrant(in.getLat(), in.getLon()) != 4) {
			throw new InvalidParameterException("Point in should be inside of bounding box");
		}
		final double lambda = getLambda(in.getLat(), out.getLat(), maxLat);
		return new LatLng(maxLat, in.getLon() + lambda * (out.getLon() - in.getLon()));
	}

	protected LatLng getBorder3Point(final LatLng in, final LatLng out) {
		if (quadrant(in.getLat(), in.getLon()) != 4) {
			throw new InvalidParameterException("Point in should be inside of bounding box");
		}
		final double lambda = getLambda(in.getLon(), out.getLon(), minLon);
		return new LatLng(in.getLat() + lambda * (out.getLat() - in.getLat()), minLon);
	}

	protected LatLng getBorder5Point(final LatLng in, final LatLng out) {
		if (quadrant(in.getLat(), in.getLon()) != 4) {
			throw new InvalidParameterException("Point in should be inside of bounding box");
		}
		final double lambda = getLambda(in.getLon(), out.getLon(), maxLon);
		return new LatLng(in.getLat() + lambda * (out.getLat() - in.getLat()), maxLon);
	}

	public LatLng getBorderPoint(final LatLng in, final LatLng out) {
		LatLng ret = null;
		if (quadrant(in.getLat(), in.getLon()) != 4) {
			throw new InvalidParameterException("Point in should be inside of bounding box");
		}
		switch (quadrant(out.getLat(), out.getLon())) {
		case 0:
			ret = getNearerPoint(in, getBorder1Point(in, out), getBorder3Point(in, out));
			break;
		case 1:
			ret = getBorder1Point(in, out);
			break;
		case 2:
			ret = getNearerPoint(in, getBorder1Point(in, out), getBorder5Point(in, out));
			break;
		case 5:
			ret = getBorder5Point(in, out);
			break;
		case 8:
			ret = getNearerPoint(in, getBorder7Point(in, out), getBorder5Point(in, out));
			break;
		case 7:
			ret = getBorder7Point(in, out);
			break;
		case 6:
			ret = getNearerPoint(in, getBorder7Point(in, out), getBorder3Point(in, out));
			break;
		case 3:
			ret = getBorder3Point(in, out);
			break;
		default:
			ret = out;
		}
		return ret;
	}

	protected LatLng nodeToLatLng(final Node node) {
		return new LatLng(node.getLat(), node.getLon());
	}

	public List<Way> cutWay(final Way way) {
		final List<Way> ret = new ArrayList<>();
		final List<Node> nodes = way.getNodes();
		int i = 0;
		boolean outSide = false;
		boolean lastOutside = false;
		Node lastNode = null;
		Way currentWay = null;
		for (final Node node : nodes) {
			outSide = quadrant(node.getLat(), node.getLon()) != 4;
			if (i == 0 && (!outSide)) {
				currentWay = new Way();
				currentWay.setId(way.getId());
				currentWay.setProps(way.getProps());
				currentWay.addNode(node);
				ret.add(currentWay);
			} else if (i > 0) {
				if (lastOutside) {
					if (!outSide) {
						final LatLng intermediate = getBorderPoint(nodeToLatLng(node),
								nodeToLatLng(lastNode));
						currentWay = new Way();
						currentWay.setId(way.getId());
						currentWay.setProps(way.getProps());
						final Node newNode = new Node();
						newNode.setId(lastNode.getId());
						newNode.setLat(intermediate.getLat());
						newNode.setLon(intermediate.getLon());
						newNode.setProps(lastNode.getProps());
						currentWay.addNode(newNode);
						currentWay.addNode(node);
						ret.add(currentWay);
					}
				} else {
					if (outSide) {
						final LatLng intermediate = getBorderPoint(nodeToLatLng(lastNode),
								nodeToLatLng(node));
						final Node newNode = new Node();
						newNode.setId(node.getId());
						newNode.setLat(intermediate.getLat());
						newNode.setLon(intermediate.getLon());
						newNode.setProps(node.getProps());
						currentWay.addNode(newNode);
					} else {
						currentWay.addNode(node);
					}
				}
			}
			lastNode = node;
			lastOutside = outSide;
			++i;
		}

		return ret;
	}

	/**
	 * Checks whether the nodes inside the way are orientated in clockwise
	 * direction
	 * 
	 * @param way
	 * @return true when points are clockwise sorted
	 */
	public static boolean rightRound(final Way way) {
		double sum = 0;
		Node lastNode = null;

		final List<Node> nodes = way.getNodes();
		for (int i = 0; nodes.size() > 0 && i <= nodes.size(); ++i) {
			final Node node = nodes.get(i % nodes.size());
			if (lastNode != null) {
				sum += (node.getLat() - lastNode.getLat()) * (node.getLon() + lastNode.getLon());
			}
			lastNode = node;
		}
		return sum < 0;
	}

	public static Way turnWayArround(final Way way) {
		final Way leftWay = new Way();
		final List<Node> nodes = way.getNodes();
		leftWay.setId(way.getId());
		leftWay.setProps(way.getProps());

		for (int i = nodes.size(); i > 0; --i) {
			final Node node = nodes.get(i - 1);
			final Node newNode = new Node();
			newNode.copy(node);
			leftWay.addNode(newNode);
		}
		return leftWay;
	}

	public List<Way> cutArea(final Way way) {
		final AreaClipper areaClipper = new AreaClipper(this);
		return areaClipper.clipArea(way);
	}

	/**
	 * @return the minLat
	 */
	public double getMinLat() {
		return minLat;
	}

	/**
	 * @return the minLon
	 */
	public double getMinLon() {
		return minLon;
	}

	/**
	 * @return the maxLat
	 */
	public double getMaxLat() {
		return maxLat;
	}

	/**
	 * @return the maxLon
	 */
	public double getMaxLon() {
		return maxLon;
	}
}

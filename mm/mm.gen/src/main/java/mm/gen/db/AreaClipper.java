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
 * @since 17.03.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.util.ArrayList;
import java.util.List;

import mm.gen.osm.Node;
import mm.gen.osm.Way;
import mm.lay.LatLng;

/**
 * @author benno
 * 
 */
public class AreaClipper {
	protected final BoundingBox box;
	protected final double xLength;
	protected final double yLength;
	protected final double extend;

	protected enum NodeState {
		INSIDE, OUTSIDE, LEAVING, ENTERING;
	}

	public AreaClipper(final BoundingBox box) {
		super();
		this.box = box;
		xLength = box.getMaxLon() - box.getMinLon();
		yLength = box.getMaxLat() - box.getMinLat();
		extend = 2 * xLength + yLength;
	}

	protected class AreaNode {
		protected final Node node;
		protected final double roundDistance;
		protected final NodeState state;

		public AreaNode(final Node node, final NodeState state) {
			super();
			this.node = node;

			this.state = state;
			if (NodeState.ENTERING.equals(state) || NodeState.LEAVING.equals(state)) {
				roundDistance = calcRoundDistance(node.getLat(), node.getLon());
			} else {
				roundDistance = -1;
			}
		}

		/**
		 * @return
		 * @see mm.gen.osm.Node#getLat()
		 */
		public double getLat() {
			return node.getLat();
		}

		/**
		 * @return
		 * @see mm.gen.osm.Node#getLon()
		 */
		public double getLon() {
			return node.getLon();
		}

		/**
		 * @return the state
		 */
		public NodeState getState() {
			return state;
		}

		/**
		 * @return the roundDistance
		 */
		public double getRoundDistance() {
			return roundDistance;
		}

		/**
		 * @return the node
		 */
		public Node getNode() {
			return node;
		}
	}

	protected class AreaNodesList {
		protected List<AreaNode> nodes = new ArrayList<>();

		public AreaNodesList(final List<Node> wayNodes) {
			super();
			Node lastNode = null;
			int lastQuadrant = -1;
			final int size = wayNodes.size();
			int firstIn = -1;
			// Find first inside Node
			for (int i = 0; firstIn < 0 && i < size; ++i) {
				final Node node = wayNodes.get(i);
				if (box.quadrant(node.getLat(), node.getLon()) == 4) {
					firstIn = i;
				}
			}

			for (int i = firstIn; firstIn >= 0 && size > 0 && i <= firstIn + size; ++i) {
				final Node node = wayNodes.get(i % size);
				final int quadrant = box.quadrant(node.getLat(), node.getLon());
				final NodeState state = quadrant == 4 ? NodeState.INSIDE : NodeState.OUTSIDE;
				if (lastNode != null) {
					if (lastQuadrant != 4 && quadrant == 4) {
						final LatLng intermediate = box.getBorderPoint(box.nodeToLatLng(node),
								box.nodeToLatLng(lastNode));
						final Node enteringNode = new Node();
						enteringNode.copy(lastNode);
						enteringNode.setLat(intermediate.getLat());
						enteringNode.setLon(intermediate.getLon());
						nodes.add(new AreaNode(enteringNode, NodeState.ENTERING));
					} else if (lastQuadrant == 4 && quadrant != 4) {
						final LatLng intermediate = box.getBorderPoint(box.nodeToLatLng(lastNode),
								box.nodeToLatLng(node));
						final Node leavingNode = new Node();
						leavingNode.copy(lastNode);
						leavingNode.setLat(intermediate.getLat());
						leavingNode.setLon(intermediate.getLon());
						nodes.add(new AreaNode(leavingNode, NodeState.LEAVING));
					}
				}
				if (NodeState.INSIDE.equals(state) && i < firstIn + size) {
					nodes.add(new AreaNode(node, state));
				}
				lastQuadrant = quadrant;
				lastNode = node;
			}
		}

		protected AreaNode getNextEnteringNode(final AreaNode node) {
			AreaNode ret = null;
			double bestDistance = Double.MAX_VALUE;
			final int index = nodes.indexOf(node);
			final int size = nodes.size();
			final double distance = node.getRoundDistance();
			for (int i = index + 1; index > 0 && size > 0 && i < index + size; ++i) {
				final AreaNode temp = nodes.get(i % size);
				if (NodeState.ENTERING.equals(temp.getState())) {
					double tempDistance = temp.getRoundDistance();
					if (tempDistance < distance) {
						tempDistance = extend - distance + tempDistance;
					} else {
						tempDistance = tempDistance - distance;
					}
					if (ret == null || (tempDistance < bestDistance)) {
						ret = temp;
						bestDistance = tempDistance;
					}
				}
			}
			return ret;
		}

		public List<AreaNode> getWayFromList() {
			final List<AreaNode> ret = new ArrayList<>();
			final int size = nodes.size();
			int addSize = 0;
			int index = -1;
			AreaNode startNode = null;
			// Find first in way
			for (int i = 0; index < 0 && i < size; ++i) {
				final AreaNode node = nodes.get(i);
				if (NodeState.INSIDE.equals(node.getState())) {
					index = i;
					startNode = node;
				}
			}

			while (index >= 0 && ret.size() < size + addSize) {
				AreaNode node = nodes.get(index % size);
				ret.add(node);
				if (NodeState.LEAVING.equals(node.getState())) {
					final AreaNode enterNode = getNextEnteringNode(node);
					final List<AreaNode> addNodes = interMediateNodes(node, enterNode);
					ret.addAll(addNodes);
					addSize += addNodes.size();
					node = enterNode;
					index = nodes.indexOf(node);
				} else {
					index++;
				}
				if (startNode == nodes.get(index % size)) {
					index = -1;
				}
			}
			return ret;
		}

		protected AreaNode produceAreaNode(final double lat, final double lon) {
			final Node node = new Node();
			node.setId(0);
			node.setLat(lat);
			node.setLon(lon);
			final int quadrant = box.quadrant(lat, lon);
			final NodeState state = quadrant == 4 ? NodeState.INSIDE : NodeState.OUTSIDE;
			return new AreaNode(node, state);
		}

		protected List<AreaNode> interMediateNodes(final AreaNode leavingNode,
				final AreaNode enteringNode) {
			final List<AreaNode> ret = new ArrayList<>();
			final double leaveDist = leavingNode.getRoundDistance();
			double enterDist = enteringNode.getRoundDistance();
			if (enterDist < leaveDist) {
				enterDist += extend;
			}
			double nextCorner = 0;
			for (int i = 0; i < 2; ++i) {
				nextCorner += xLength;
				if (enterDist > nextCorner && leaveDist < nextCorner) {
					ret.add(produceAreaNode(box.getMaxLat(), box.getMaxLon()));
				}
				nextCorner += yLength;
				if (enterDist > nextCorner && leaveDist < nextCorner) {
					ret.add(produceAreaNode(box.getMinLat(), box.getMaxLon()));
				}
				nextCorner += xLength;
				if (enterDist > nextCorner && leaveDist < nextCorner) {
					ret.add(produceAreaNode(box.getMinLat(), box.getMinLon()));
				}
				nextCorner += yLength;
				if (enterDist > nextCorner && leaveDist < nextCorner) {
					ret.add(produceAreaNode(box.getMaxLat(), box.getMinLon()));
				}
			}

			return ret;
		}

		public void dropNodes(final List<AreaNode> drop) {
			nodes.removeAll(drop);
		}
	}

	protected double calcRoundDistance(final double lat, final double lon) {
		double ret = 0;
		final int quadrant = box.quadrant(lat, lon);
		switch (quadrant) {
		case 7:
		case 8:
			ret = lon - box.getMinLon();
			break;
		case 5:
		case 2:
			ret = xLength + box.getMaxLat() - lat;
			break;
		case 1:
		case 0:
			ret = xLength + yLength + box.getMaxLon() - lon;
			break;
		case 3:
		case 6:
			ret = 2 * xLength + yLength + lat - box.getMinLat();
			break;
		default:
			throw new IllegalArgumentException("point should be outside");
		}
		return ret;
	}

	public List<Way> clipArea(final Way way) {
		final List<Way> ret = new ArrayList<>();
		final boolean rightRound = BoundingBox.rightRound(way);
		final Way tempWay;
		if (rightRound) {
			tempWay = way;
		} else {
			tempWay = BoundingBox.turnWayArround(way);
		}
		final AreaNodesList list = new AreaNodesList(tempWay.getNodes());
		List<AreaNode> wayNodes = list.getWayFromList();
		while (wayNodes.size() > 0) {
			final Way add = new Way();
			add.setId(way.getId());
			add.setProps(way.getProps());
			for (final AreaNode areaNode : wayNodes) {
				add.addNode(areaNode.getNode());
			}
			ret.add(add);
			list.dropNodes(wayNodes);
			wayNodes = list.getWayFromList();
		}
		if (!rightRound) {
			final List<Way> tempRet = new ArrayList<>();
			for (final Way way2 : ret) {
				tempRet.add(BoundingBox.turnWayArround(way2));
			}
			ret.clear();
			ret.addAll(tempRet);
		}
		return ret;
	}
}

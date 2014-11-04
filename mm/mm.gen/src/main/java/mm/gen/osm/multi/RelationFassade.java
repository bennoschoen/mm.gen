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
 * @since 20.07.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.osm.multi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mm.gen.db.WayClassifier;
import mm.gen.db.WayClassifier.WayType;
import mm.gen.osm.MultiWay;
import mm.gen.osm.Node;
import mm.gen.osm.Way;
import mm.tile.gen.osm.Relation;

/**
 * @author benno
 * 
 */
public class RelationFassade {
	private final Relation relation;
	private final List<WayFassade> outerWays = new ArrayList<>();
	private final List<WayFassade> innerWays = new ArrayList<>();
	private static final WayClassifier classifier = new WayClassifier();

	public RelationFassade(final Relation relation) {
		super();
		this.relation = relation;
	}

	public void addInnerWay(final WayFassade way) {
		innerWays.add(way);
	}

	public void addOuterWay(final WayFassade way) {
		outerWays.add(way);
	}

	public boolean complete() {
		boolean ret = true;
		for (final WayFassade way : outerWays) {
			ret &= way.getWay() != null;
		}
		for (final WayFassade way : innerWays) {
			ret &= way.getWay() != null;
		}
		return ret;
	}

	public MultiWay concatenateWays() {
		final MultiWay ret;
		final WayType wayType = classifier.getWayType(relation.getProps());
		WayType ringType = null;
		final Map<String, String> props = new HashMap<>();
		final WayConcatenator innerConatenator = new WayConcatenator();
		final WayConcatenator outerConatenator = new WayConcatenator();
		final List<WayFassade> innerResult = innerConatenator.process(innerWays);
		final List<WayFassade> outerResult = outerConatenator.process(outerWays);

		if (innerConatenator.isIllegalMultiPolygon() || outerConatenator.isIllegalMultiPolygon()) {
			ret = null;
		} else {
			ret = new MultiWay();
			for (final WayFassade wayFassade : outerResult) {
				final Way way = wayFassade.getWay();
				ret.addOuterWay(way);
				if (ringType == null) {
					props.putAll(way.getProps());
					ringType = classifier.getWayType(props);
				}
			}

			for (final WayFassade wayFassade : innerResult) {
				final Way way = wayFassade.getWay();
				ret.addInnerWay(way);
			}
			if (wayType == null) {
				ret.setProps(props);
			} else {
				ret.setProps(relation.getProps());
			}
		}
		return ret;
	}

	private class WayConcatenator {
		private final Map<Long, List<WayFassade>> wayMap = new HashMap<>();
		boolean illegalMultiPolygon = false;

		private void insert(final Long id, final WayFassade way) {
			List<WayFassade> list = wayMap.get(id);
			if (list == null) {
				list = new ArrayList<>();
				wayMap.put(id, list);
			}
			list.add(way);
		}

		private void remove(final Long id, final WayFassade way) {
			final List<WayFassade> list = wayMap.get(id);
			if (list != null) {
				list.remove(way);
				if (list.isEmpty()) {
					wayMap.remove(id);
				}
			}
		}

		private WayFassade concatenate(final Long nodeId) {
			final List<WayFassade> list = wayMap.remove(nodeId);
			final List<Node> resultingNodes = new ArrayList<>();
			final HashMap<String, String> props = new HashMap<String, String>();
			for (final WayFassade wayFassade : list) {
				remove(wayFassade.getStartNode(), wayFassade);
				remove(wayFassade.getEndNode(), wayFassade);
			}

			final WayFassade firstWay = list.get(0);
			final WayFassade lastWay = list.get(1);

			// Concatenate nodes
			resultingNodes.addAll(firstWay.getNodesendigWith(nodeId));
			resultingNodes.addAll(lastWay.getNodesStartingWith(nodeId));

			// Mixing props
			props.putAll(firstWay.getWay().getProps());
			props.putAll(firstWay.getWay().getProps());
			// build new Way
			final Way newWay = new Way();
			newWay.setProps(props);
			newWay.setNodes(resultingNodes);
			newWay.setId(list.get(0).getId());
			final WayFassade ret = new WayFassade(newWay.getId());
			ret.setWay(newWay);
			return ret;
		}

		public List<WayFassade> process(final List<WayFassade> ways) {
			final List<WayFassade> ret = new ArrayList<>();
			// Push ways into map
			for (final WayFassade wayFassade : ways) {
				if (wayFassade.getWay() == null) {
					illegalMultiPolygon = true;
				}
				if (wayFassade.getWay() != null) {
					if (wayFassade.isClosed()) {
						ret.add(wayFassade);
					} else {
						insert(wayFassade.getStartNode(), wayFassade);
						insert(wayFassade.getEndNode(), wayFassade);
					}
				}
			}
			// check for nodes which do not have 2 ways
			while (!(wayMap.isEmpty() || illegalMultiPolygon)) {
				Long lastKey = null;
				for (final Long key : wayMap.keySet()) {
					lastKey = key;
					final List<WayFassade> nodesList = wayMap.get(key);
					if (nodesList.size() != 2) {
						illegalMultiPolygon = true;
					}
				}
				if (!(illegalMultiPolygon || lastKey == null)) {
					final WayFassade result = concatenate(lastKey);
					if (result.isClosed()) {
						ret.add(result);
					} else {
						insert(result.getStartNode(), result);
						insert(result.getEndNode(), result);
					}
				}
			}
			if (illegalMultiPolygon) {
				ret.clear();
			}
			return ret;
		}

		/**
		 * @return the illegalMultiPolygon
		 */
		public boolean isIllegalMultiPolygon() {
			return illegalMultiPolygon;
		}
	}
}

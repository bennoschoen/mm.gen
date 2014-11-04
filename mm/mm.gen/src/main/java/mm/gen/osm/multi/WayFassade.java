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
import java.util.Collections;
import java.util.List;

import mm.gen.osm.Node;
import mm.gen.osm.Way;

/**
 * @author benno
 * 
 */
public class WayFassade {
	private Way way = null;
	private long id = 0;
	private long startNode = -1;
	private long endNode = -2;

	public WayFassade(final long id) {
		this.id = id;
	}

	public void setWay(final Way way) {
		this.way = way;
		if (way != null) {
			id = way.getId();
			final List<Node> nodes = way.getNodes();
			startNode = nodes.get(0).getId();
			endNode = nodes.get(nodes.size() - 1).getId();
		}
	}

	public boolean isClosed() {
		return startNode == endNode;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the startNode
	 */
	public long getStartNode() {
		return startNode;
	}

	/**
	 * @return the endNode
	 */
	public long getEndNode() {
		return endNode;
	}

	/**
	 * @return the way
	 */
	public Way getWay() {
		return way;
	}

	public List<Node> getNodesStartingWith(final Long nodeId) {
		final List<Node> ret = new ArrayList<>(way.getNodes());
		if (nodeId != startNode) {
			Collections.reverse(ret);
		}
		return ret;
	}

	public List<Node> getNodesendigWith(final Long nodeId) {
		final List<Node> ret = new ArrayList<>(way.getNodes());
		if (nodeId != endNode) {
			Collections.reverse(ret);
		}
		return ret;
	}
}

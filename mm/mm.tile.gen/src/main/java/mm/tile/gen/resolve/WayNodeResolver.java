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
 * @since 14.10.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen.resolve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mm.io.ReferenceResolver;
import mm.io.ReferenceResolver.ReferedHandler;
import mm.io.ReferenceResolver.RefererHandler;
import mm.tile.gen.osm.Node;
import mm.tile.gen.osm.Way;

/**
 * @author benno
 * 
 */
public class WayNodeResolver {

	private final int MAX_SIZE = 800000;

	private final ReferedHandler<Node> manyHandler = new ReferedHandler<Node>() {

		@Override
		public long getId(final Node many) {
			return many.getId();
		}

		@Override
		public void setId(final Node many, final long id) {
			many.setId(id);
		}
	};

	private final RefererHandler<Way, Node> oneHandler = new RefererHandler<Way, Node>() {

		@Override
		public long getId(final Way one) {
			return one.getId();
		}

		@Override
		public List<Long> getRefs(final Way one) {
			final List<Long> refs = new ArrayList<>();
			for (final Node node : one.getNodes()) {
				refs.add(Long.valueOf(node.getId()));
			}
			return refs;
		}

		@Override
		public void setResolvedRefs(final Way one, final List<Node> manies) {
			final HashMap<Long, Node> resolvedMap = new HashMap<>();
			for (final Node node : manies) {
				resolvedMap.put(Long.valueOf(node.getId()), node);
			}

			for (final Node node : one.getNodes()) {
				final Node resolvedNode = resolvedMap.get(Long.valueOf(node.getId()));
				if (resolvedNode == null) {
					System.out.println("Node not resolved : " + node.getId());
				} else {
					node.setLat(resolvedNode.getLat());
					node.setLon(resolvedNode.getLon());
				}
			}
		}
	};

	private WayNodeResolver() {

	}

	private void resolveNodes1(final File destFile, final File wayFile, final File nodeFile,
			final File tempDir) throws IOException {
		ReferenceResolver.resolveReferences(destFile, wayFile, nodeFile, tempDir,
				new Way.WayFactory(), new Node.NodeFactory(), manyHandler, oneHandler, MAX_SIZE);
	}

	public static void resolveNodes(final File destFile, final File wayFile, final File nodeFile,
			final File tempDir) throws IOException {
		final WayNodeResolver resolver = new WayNodeResolver();
		resolver.resolveNodes1(destFile, wayFile, nodeFile, tempDir);
	}

}

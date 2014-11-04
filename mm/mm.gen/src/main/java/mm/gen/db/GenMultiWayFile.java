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
 * @since 14.03.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.io.File;
import java.io.IOException;

import mm.gen.osm.MultiWay;
import mm.gen.osm.Node;
import mm.gen.osm.Way;
import mm.gen.osm.multi.RelationFassade;
import mm.gen.osm.multi.WayFassade;
import mm.io.StreamableIterator;
import mm.io.StreamableWriter;
import mm.tile.gen.osm.Member;

/**
 * @author benno
 * 
 */
public class GenMultiWayFile {

	private static double scale = 0.0000001;

	private GenMultiWayFile() {
	}

	public static Way getWay(final mm.tile.gen.osm.Way rawWay) {
		final Way way = new Way();
		way.setId(rawWay.getId());
		way.setProps(rawWay.getProps());
		for (final mm.tile.gen.osm.Node rawNode : rawWay.getNodes()) {
			final Node node = new Node();
			node.setId(rawNode.getId());
			node.setLat(-scale * rawNode.getLat());
			node.setLon(scale * rawNode.getLon());
			way.addNode(node);
		}
		return way;
	}

	public static void genFiles(final File rawWayFile, final File rawRelationFile,
			final File destFile) throws IOException {
		final StreamableWriter<MultiWay> wayWriter = new StreamableWriter<>(destFile);
		genRelationFile(rawRelationFile, wayWriter);
		genWayFile(rawWayFile, wayWriter);
		wayWriter.close();
	}

	public static void genWayFile(final File rawWayFile, final StreamableWriter<MultiWay> wayWriter) {
		try {

			final StreamableIterator<mm.tile.gen.osm.Way> wayIter = new StreamableIterator<>(
					rawWayFile, new mm.tile.gen.osm.Way.WayFactory());
			while (wayIter.hasNext()) {
				final mm.tile.gen.osm.Way rawWay = wayIter.next();
				if (!rawWay.getNodes().isEmpty()) {
					final Way way = getWay(rawWay);
					final MultiWay multiWay = new MultiWay();
					multiWay.setProps(way.getProps());
					multiWay.addOuterWay(way);
					wayWriter.writeStreamable(multiWay);
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void genRelationFile(final File rawRelationFile,
			final StreamableWriter<MultiWay> wayWriter) {
		try {

			final StreamableIterator<mm.tile.gen.osm.Relation> rawRelIter = new StreamableIterator<>(
					rawRelationFile, new mm.tile.gen.osm.Relation.RelationFactory());

			while (rawRelIter.hasNext()) {
				final mm.tile.gen.osm.Relation rawRel = rawRelIter.next();
				if (!rawRel.getRelations().isEmpty()) {
					System.out.println("Relation contains Relations, that is not supported. Drop :"
							+ rawRel.getId());
				} else {
					final RelationFassade relFassade = new RelationFassade(rawRel);
					for (final Member<mm.tile.gen.osm.Way> wayMember : rawRel.getWays()) {
						final mm.tile.gen.osm.Way rawWay = wayMember.getRef();
						final WayFassade wayFassade = new WayFassade(rawWay.getId());
						if (rawWay.getNodes().isEmpty()) {
							System.out.println("Drop empty Way :" + rawWay.getId());
						} else {
							wayFassade.setWay(getWay(rawWay));
							if ("outer".equals(wayMember.getRole())) {
								relFassade.addOuterWay(wayFassade);
							} else if ("inner".equals(wayMember.getRole())) {
								relFassade.addInnerWay(wayFassade);
							}
						}
					}
					final MultiWay multiWay = relFassade.concatenateWays();
					if (multiWay != null) {
						wayWriter.writeStreamable(multiWay);
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}

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
 * @since 27.09.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import mm.io.StreamableWriter;
import mm.o5m.reader.O5mHandler;
import mm.o5m.reader.O5mNode;
import mm.o5m.reader.O5mReader;
import mm.o5m.reader.O5mRelation;
import mm.o5m.reader.O5mWay;
import mm.o5m.reader.O5mRelation.Reference;
import mm.tile.gen.osm.Member;
import mm.tile.gen.osm.Node;
import mm.tile.gen.osm.Relation;
import mm.tile.gen.osm.Way;
import mm.tile.gen.resolve.RelationRelationResolver;
import mm.tile.gen.resolve.RelationWayResolver;
import mm.tile.gen.resolve.WayNodeResolver;

import org.json.JSONException;

/**
 * @author benno
 * 
 */
public class Generator {

	protected final File dataPath;
	protected final File nodeFile;
	protected final File simpleNodeFile;
	protected final File wayFile;
	protected final File relationFile;
	protected final File resolvedWayFile;
	protected final File resolvedFilteredWayFile;

	protected final File resolvedRelationFile;

	public Generator(File dataPath) throws IOException {
		this.dataPath = dataPath;
		nodeFile = new File(dataPath, "nodes.dat");
		simpleNodeFile = new File(dataPath, "snodes.dat");
		wayFile = new File(dataPath, "ways.dat");
		relationFile = new File(dataPath, "relations.dat");
		resolvedWayFile = new File(dataPath, "resways.dat");
		resolvedFilteredWayFile = new File(dataPath, "resfiltways.dat");
		resolvedRelationFile = new File(dataPath, "resrelation.dat");
	}

	public void readFile(final File f) throws IOException {
		final O5mReader reader = new O5mReader();
		final StreamableWriter<Node> nodeWriter = new StreamableWriter<>(nodeFile);
		final StreamableWriter<Node> sNodeWriter = new StreamableWriter<>(simpleNodeFile);
		final StreamableWriter<Way> wayWriter = new StreamableWriter<>(wayFile);
		final StreamableWriter<Relation> relationWriter = new StreamableWriter<>(relationFile);

		reader.readFile(f, new O5mHandler() {

			@Override
			public void newNode(final O5mNode o5mNode) {
				try {
					final Node node = new Node();
					node.setId(o5mNode.getNodeId());
					node.setLat(o5mNode.getLat());
					node.setLon(o5mNode.getLon());
					sNodeWriter.writeStreamable(node);
					node.setProps(o5mNode.getProps());
					nodeWriter.writeStreamable(node);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void newWay(final O5mWay o5mWay) {
				try {
					final Way way = new Way();
					way.setId(o5mWay.getWayId());
					way.setProps(o5mWay.getProps());
					for (final Long ref : o5mWay.getRefs()) {
						final Node node = new Node();
						node.setId(ref.longValue());
						way.addNode(node);
					}
					wayWriter.writeStreamable(way);
					if (way.getId() == 23492714) {
						try {
							System.out.println(way.toJSON().toString(4));
						} catch (final JSONException e) {
							e.printStackTrace();
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void newRelation(final O5mRelation o5mRelation) {
				try {
					final Relation relation = new Relation();
					relation.setId(o5mRelation.getRelationId());
					relation.setProps(o5mRelation.getProps());
					if ("multipolygon".equals(relation.getProperty("type"))) {
						for (final Reference ref : o5mRelation.getRefs()) {
							switch (ref.getType()) {
							case NODE:
								final Node node = new Node();
								final Member<Node> nodeMember = new Member<>(node);
								node.setId(ref.getId());
								nodeMember.setRole(ref.getRole());
								relation.addNode(nodeMember);
								break;
							case RELATION:
								final Relation childRelation = new Relation();
								final Member<Relation> relationMember = new Member<>(childRelation);
								childRelation.setId(ref.getId());
								relationMember.setRole(ref.getRole());
								relation.addRelation(relationMember);
								break;
							case WAY:
								final Way way = new Way();
								final Member<Way> wayMember = new Member<>(way);
								way.setId(ref.getId());
								wayMember.setRole(ref.getRole());
								relation.addWay(wayMember);
								break;
							case MAX:
							default:
								break;

							}
						}
						relationWriter.writeStreamable(relation);
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}

			}
		});
		nodeWriter.close();
		sNodeWriter.close();
		wayWriter.close();
		relationWriter.close();
		WayNodeResolver.resolveNodes(resolvedWayFile, wayFile, simpleNodeFile, dataPath);
		RelationRelationResolver.resolve(relationFile, dataPath);
		RelationWayResolver.resolveWays(resolvedRelationFile, relationFile, resolvedWayFile,
				dataPath);
		RelationWayResolver.dropRelatedWays(resolvedFilteredWayFile, resolvedRelationFile,
				resolvedWayFile, dataPath);
		simpleNodeFile.delete();
		wayFile.delete();
		relationFile.delete();
		resolvedWayFile.delete();
		System.out.println(new Date());
	}

	public File getNodeFile() {
		return nodeFile;
	}
	
	public File getResolvedFilteredWayFile() {
		return resolvedFilteredWayFile;
	}

	public File getResolvedRelationFile() {
		return resolvedRelationFile;
	}
}

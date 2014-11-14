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
import mm.io.StreamableFilter;
import mm.io.StreamableIterator;
import mm.io.StreamableSorter;
import mm.io.StreamableWriter;
import mm.io.ReferenceResolver.ReferedHandler;
import mm.io.ReferenceResolver.RefererHandler;
import mm.io.StreamableFilter.ID;
import mm.io.StreamableFilter.IDComparator;
import mm.io.StreamableFilter.IDFactory;
import mm.io.StreamableFilter.SourceHandler;
import mm.tile.gen.osm.Member;
import mm.tile.gen.osm.Relation;
import mm.tile.gen.osm.Way;
import mm.tile.gen.osm.Relation.RelationFactory;

/**
 * @author benno
 * 
 */
public class RelationWayResolver {
	private final static int MAX_SIZE = 10000;

	private final ReferedHandler<Way> manyHandler = new ReferedHandler<Way>() {

		@Override
		public long getId(final Way many) {
			return many.getId();
		}

		@Override
		public void setId(final Way many, final long id) {
			many.setId(id);
		}
	};

	private final RefererHandler<Relation, Way> oneHandler = new RefererHandler<Relation, Way>() {

		@Override
		public long getId(final Relation one) {
			return one.getId();
		}

		@Override
		public List<Long> getRefs(final Relation one) {
			final List<Long> refs = new ArrayList<>();
			for (final Member<Way> way : one.getWays()) {
				refs.add(Long.valueOf(way.getRef().getId()));
			}
			return refs;
		}

		@Override
		public void setResolvedRefs(final Relation one, final List<Way> manies) {
			final HashMap<Long, Way> resolvedMap = new HashMap<>();
			for (final Way way : manies) {
				resolvedMap.put(Long.valueOf(way.getId()), way);
			}

			for (final Member<Way> wayMember : one.getWays()) {
				final Way way = wayMember.getRef();
				final Way resolvedWay = resolvedMap.get(Long.valueOf(way.getId()));
				if (resolvedWay == null) {
					System.out.println("Way not resolved : " + way.getId());
				} else {
					way.setProps(resolvedWay.getProps());
					way.setNodes(resolvedWay.getNodes());
				}
			}
		}
	};

	private RelationWayResolver() {

	}

	private int generateIdFile(final File idFile, final File relationFile, final File tempDir)
			throws IOException {
		final StreamableIterator<Relation> relationIter = new StreamableIterator<>(relationFile,
				new RelationFactory());
		final StreamableWriter<ID> idWriter = new StreamableWriter<>(idFile);
		int idCount = 0;
		while (relationIter.hasNext()) {
			final Relation relation = relationIter.next();
			for (final Member<Way> wayMember : relation.getWays()) {
				idCount++;
				final ID id = new ID();
				id.setId(wayMember.getRef().getId());
				idWriter.writeStreamable(id);
			}
		}
		idWriter.close();
		relationIter.close();
		if (idCount > 1) {
			final StreamableSorter<ID> idSorter = new StreamableSorter<>(idFile, tempDir,
					new IDFactory(), new IDComparator());
			idSorter.process();
		}
		return idCount;
	}

	private void removeRelationWays(final File destFile, final File relationFile,
			final File wayFile, final File tempDir) throws IOException {
		final File idFile = File.createTempFile("wayids", "id", tempDir);
		generateIdFile(idFile, relationFile, tempDir);
		StreamableFilter.filter(wayFile, destFile, idFile, new Way.WayFactory(),
				new SourceHandler<Way>() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see
					 * de.yamap.mm.io.StreamableFilter.SourceHandler#getId(de
					 * .yamap.mm.io.Streamable)
					 */
					@Override
					public long getId(final Way instance) {
						return instance.getId();
					}
				}, false);
		idFile.delete();
	}

	private void resolveWays1(final File destFile, final File relationFile, final File wayFile,
			final File tempDir) throws IOException {
		ReferenceResolver.resolveReferences(destFile, relationFile, wayFile, tempDir,
				new Relation.RelationFactory(), new Way.WayFactory(), manyHandler, oneHandler, MAX_SIZE);
	}

	public static void resolveWays(final File destFile, final File relationFile,
			final File wayFile, final File tempDir) throws IOException {
		final RelationWayResolver resolver = new RelationWayResolver();
		resolver.resolveWays1(destFile, relationFile, wayFile, tempDir);
	}

	public static void dropRelatedWays(final File destFile, final File relationFile,
			final File wayFile, final File tempDir) throws IOException {
		final RelationWayResolver resolver = new RelationWayResolver();
		resolver.removeRelationWays(destFile, relationFile, wayFile, tempDir);
	}
}

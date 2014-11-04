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
 * @since 17.10.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen.resolve;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.json.JSONException;

/**
 * @author benno
 * 
 */
public class RelationRelationResolver {
	final File relationFile;
	final File tempPath;

	private final ReferedHandler<Relation> manyHandler = new ReferedHandler<Relation>() {

		@Override
		public long getId(final Relation many) {
			return many.getId();
		}

		@Override
		public void setId(final Relation many, final long id) {
			many.setId(id);
		}
	};

	private final RefererHandler<Relation, Relation> oneHandler = new RefererHandler<Relation, Relation>() {

		@Override
		public long getId(final Relation one) {
			return one.getId();
		}

		@Override
		public List<Long> getRefs(final Relation one) {
			final List<Long> refs = new ArrayList<>();
			for (final Member<Relation> relation : one.getRelations()) {
				refs.add(Long.valueOf(relation.getRef().getId()));
			}
			return refs;
		}

		@Override
		public void setResolvedRefs(final Relation one, final List<Relation> manies) {
			final Map<Long, Member<Relation>> referedMap = new HashMap<>();
			for (final Member<Relation> memRelation : one.getRelations()) {
				referedMap.put(memRelation.getRef().getId(), memRelation);
			}
			one.getRelations().clear();

			for (final Relation relation : manies) {
				final Member<Relation> origRelation = referedMap.get(relation.getId());
				for (final Member<Way> wayMember : relation.getWays()) {
					final Member<Way> newMember = new Member<>(wayMember.getRef());
					newMember.setRole(origRelation.getRole());
					one.addWay(newMember);
				}
			}
		}
	};

	public RelationRelationResolver(final File relationFile, final File tempPath) {
		super();
		this.relationFile = relationFile;
		this.tempPath = tempPath;
	}

	private int generateIdFile(final File idFile, final File relationFile) throws IOException {
		final StreamableIterator<Relation> relationIter = new StreamableIterator<>(relationFile,
				new RelationFactory());
		final StreamableWriter<ID> idWriter = new StreamableWriter<>(idFile);
		int idCount = 0;
		while (relationIter.hasNext()) {
			final Relation relation = relationIter.next();
			for (final Member<Relation> relation2 : relation.getRelations()) {
				idCount++;
				final ID id = new ID();
				id.setId(relation2.getRef().getId());
				idWriter.writeStreamable(id);
			}
		}
		idWriter.close();
		relationIter.close();
		if (idCount > 1) {
			final StreamableSorter<ID> idSorter = new StreamableSorter<>(idFile, tempPath,
					new IDFactory(), new IDComparator());
			idSorter.process();
		}
		return idCount;
	}

	private void filterRelations(final File idFile, final File sourceFile, final File destFile)
			throws IOException {
		StreamableFilter.filter(sourceFile, destFile, idFile, new RelationFactory(),
				new RelationSourceHandler(), true);
	}

	private void dumpRelationFile(final File someReFile) throws IOException {
		final StreamableIterator<Relation> relIter = new StreamableIterator<>(someReFile,
				new RelationFactory());
		while (relIter.hasNext()) {
			try {
				System.out.println(relIter.next().toJSON().toString());
			} catch (final JSONException e) {
			}
		}
	}

	private File mergeRelations(final int count, final File someRelationsFile,
			final File manyRelationsFile) throws IOException {

		final File idFile = File.createTempFile("filter", "id", tempPath);
		final File resultFile = File.createTempFile("refered", "n" + count, tempPath);
		final int nextCount = generateIdFile(idFile, someRelationsFile);
		File tempSomeReleationFile = someRelationsFile;
		if (nextCount >= count) {
			System.out.println("Circular reference between Relations");
			dumpRelationFile(someRelationsFile);
			idFile.delete();
		} else if (nextCount > 0) {
			final File destRelFile = File.createTempFile("refered", "rel", tempPath);
			filterRelations(idFile, someRelationsFile, destRelFile);
			tempSomeReleationFile = mergeRelations(nextCount, destRelFile, someRelationsFile);
			destRelFile.delete();
			idFile.delete();
		} else if (nextCount == 0) {
			idFile.delete();
		}
		ReferenceResolver.resolveReferences(resultFile, manyRelationsFile, tempSomeReleationFile,
				tempPath, new Relation.RelationFactory(), new Relation.RelationFactory(),
				manyHandler, oneHandler);
		tempSomeReleationFile.delete();
		return resultFile;
	}

	private void resolve() throws IOException {
		final File idFile = File.createTempFile("filter", "id", tempPath);
		final File destRelFile = File.createTempFile("refered", "rel", tempPath);
		final int count = generateIdFile(idFile, relationFile);
		if (count > 0) {
			filterRelations(idFile, relationFile, destRelFile);
			final File resultFile = mergeRelations(count, destRelFile, relationFile);
			final StreamableIterator<Relation> relIter = new StreamableIterator<>(resultFile,
					new RelationFactory());
			final StreamableWriter<Relation> relWriter = new StreamableWriter<>(relationFile);
			while (relIter.hasNext()) {
				relWriter.writeStreamable(relIter.next());
			}
			relWriter.close();
			relIter.close();
			resultFile.delete();
		}
		idFile.delete();
		destRelFile.delete();
	}

	public static void resolve(final File relationFile, final File tempPath) throws IOException {
		final RelationRelationResolver resolver = new RelationRelationResolver(relationFile,
				tempPath);
		resolver.resolve();
	}

	private static class RelationSourceHandler implements SourceHandler<Relation> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.yamap.mm.io.StreamableFilter.SoureHandler#getId(de.yamap.mm.io
		 * .Streamable)
		 */
		@Override
		public long getId(final Relation instance) {
			return instance.getId();
		}
	}
}

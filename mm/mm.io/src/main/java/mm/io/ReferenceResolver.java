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
 * @since 11.10.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is able to resolve a one to many reference relation. The two
 * classes that reference each other need not have any special Interfaces
 * implemented, the algorithm is loose coupled to the classes that use it. On
 * the other side it makes things a little bit more complicated because handlers
 * are needed to access the one and the many class.
 * 
 * It is expected that the reference is build by a long id and that both classes
 * can be identified by such an id.
 * 
 * @author benno
 * 
 */
public class ReferenceResolver<One extends Streamable, Many extends Streamable> {

	private final File destFile;
	private final File oneFile;
	private final File manyFile;
	private final File tempDir;

	private final StreamableFactory<One> oneFactory;
	private final StreamableFactory<Many> manyFactory;

	private final ReferedHandler<Many> manyHandler;
	private final RefererHandler<One, Many> oneHandler;
	private final int maxSize;
	private static final int DEFAULT_MAX_SIZE = StreamableSorter.MaxSize;

	/**
	 * Constuctor storing all configuration that are needed to let the resolve
	 * algorithm run
	 * 
	 * @param destFile
	 *            The target file for the one instances containing the resolved
	 *            many instances
	 * @param oneFile
	 *            the file containing the one side instances
	 * @param manyFile
	 *            the file containing the many side instances
	 * @param tempDir
	 *            a directory to store intermediate results
	 * @param oneFactory
	 *            Factory constructing One instances
	 * @param manyFactory
	 *            Factory constructing Many instances
	 * @param manyHandler
	 *            handler that sets and gets all needed values from the one
	 *            class
	 * @param oneHandler
	 *            handler that sets and gets all needed values from the many
	 *            class
	 */
	private ReferenceResolver(final File destFile, final File oneFile, final File manyFile,
			final File tempDir, final StreamableFactory<One> oneFactory,
			final StreamableFactory<Many> manyFactory, final ReferedHandler<Many> manyHandler,
			final RefererHandler<One, Many> oneHandler) {
		this(destFile, oneFile, manyFile, tempDir, oneFactory, manyFactory, manyHandler,
				oneHandler, DEFAULT_MAX_SIZE);
	}

	/**
	 * Constuctor storing all configuration that are needed to let the resolve
	 * algorithm run
	 * 
	 * @param destFile
	 *            The target file for the one instances containing the resolved
	 *            many instances
	 * @param oneFile
	 *            the file containing the one side instances
	 * @param manyFile
	 *            the file containing the many side instances
	 * @param tempDir
	 *            a directory to store intermediate results
	 * @param oneFactory
	 *            Factory constructing One instances
	 * @param manyFactory
	 *            Factory constructing Many instances
	 * @param manyHandler
	 *            handler that sets and gets all needed values from the one
	 *            class
	 * @param oneHandler
	 *            handler that sets and gets all needed values from the many
	 *            class
	 */
	private ReferenceResolver(final File destFile, final File oneFile, final File manyFile,
			final File tempDir, final StreamableFactory<One> oneFactory,
			final StreamableFactory<Many> manyFactory, final ReferedHandler<Many> manyHandler,
			final RefererHandler<One, Many> oneHandler, final int maxSize) {
		super();
		this.destFile = destFile;
		this.oneFile = oneFile;
		this.manyFile = manyFile;
		this.tempDir = tempDir;
		this.oneFactory = oneFactory;
		this.manyFactory = manyFactory;
		this.manyHandler = manyHandler;
		this.oneHandler = oneHandler;
		this.maxSize = maxSize;
	}

	/**
	 * This method lets the whole resolving algorithm run. As a result the
	 * destfile is created containing the result. The intermediate files are
	 * deleted when the resolving is finished.
	 * 
	 * @param destFile
	 *            The target file for the one instances containing the resolved
	 *            many instances
	 * @param oneFile
	 *            the file containing the one side instances
	 * @param manyFile
	 *            the file containing the many side instances
	 * @param tempDir
	 *            a directory to store intermediate results
	 * @param oneFactory
	 *            Factory constructing One instances
	 * @param manyFactory
	 *            Factory constructing Many instances
	 * @param manyHandler
	 *            handler that sets and gets all needed values from the one
	 *            class
	 * @param oneHandler
	 *            handler that sets and gets all needed values from the many
	 *            class
	 * @throws IOException
	 */
	public static <One extends Streamable, Many extends Streamable> void resolveReferences(
			final File destFile, final File oneFile, final File manyFile, final File tempDir,
			final StreamableFactory<One> oneFactory, final StreamableFactory<Many> manyFactory,
			final ReferedHandler<Many> manyHandler, final RefererHandler<One, Many> oneHandler)
			throws IOException {
		resolveReferences(destFile, oneFile, manyFile, tempDir, oneFactory, manyFactory,
				manyHandler, oneHandler, DEFAULT_MAX_SIZE);
	}

	/**
	 * This method lets the whole resolving algorithm run. As a result the
	 * destfile is created containing the result. The intermediate files are
	 * deleted when the resolving is finished.
	 * 
	 * @param destFile
	 *            The target file for the one instances containing the resolved
	 *            many instances
	 * @param oneFile
	 *            the file containing the one side instances
	 * @param manyFile
	 *            the file containing the many side instances
	 * @param tempDir
	 *            a directory to store intermediate results
	 * @param oneFactory
	 *            Factory constructing One instances
	 * @param manyFactory
	 *            Factory constructing Many instances
	 * @param manyHandler
	 *            handler that sets and gets all needed values from the one
	 *            class
	 * @param oneHandler
	 *            handler that sets and gets all needed values from the many
	 *            class
	 * @param maxSize
	 *            maximum size for initial SorterFiles in number of elements.
	 *            Default is 50000, but there is space for rather more.
	 * @throws IOException
	 */
	public static <One extends Streamable, Many extends Streamable> void resolveReferences(
			final File destFile, final File oneFile, final File manyFile, final File tempDir,
			final StreamableFactory<One> oneFactory, final StreamableFactory<Many> manyFactory,
			final ReferedHandler<Many> manyHandler, final RefererHandler<One, Many> oneHandler,
			final int maxSize) throws IOException {
		final ReferenceResolver<One, Many> resolver = new ReferenceResolver<>(destFile, oneFile,
				manyFile, tempDir, oneFactory, manyFactory, manyHandler, oneHandler, maxSize);

		// Create reference File to resolve References

		final File referenceFile = resolver.createReferenceFile();

		// Sort the file for the Many reference

		resolver.sortReferencesForMany(referenceFile);

		// Resolve References

		final File resolvedRefFile = resolver.setManiesToReferences(referenceFile);

		referenceFile.delete();

		// Sort the file for the One reference

		resolver.sortReferencesForOne(resolvedRefFile);

		// Finish Resolving

		resolver.setManiesToOne(resolvedRefFile);

		resolvedRefFile.delete();
	}

	/**
	 * Gives the Many instances that are resolved by there references to the One
	 * class using the setResolvedRefs method from the OneHandler
	 * 
	 * @param referenceFile
	 *            File containing the references with the resolved many
	 *            belonging to the reference.
	 * @throws IOException
	 */
	protected void setManiesToOne(final File referenceFile) throws IOException {
		final StreamableWriter<One> destWriter = new StreamableWriter<>(destFile);
		final ReferenceIterator refIterator = new ReferenceIterator(referenceFile);
		final StreamableIterator<One> oneIter = new StreamableIterator<>(oneFile, oneFactory);
		One oneInstance = oneIter.hasNext() ? oneIter.next() : null;
		long nextOneId = refIterator.getNextOneId();
		List<Many> resolvedMany = refIterator.hasNext() ? refIterator.next()
				: new ArrayList<Many>();
		while (!(oneInstance == null || resolvedMany.isEmpty())) {
			while (!resolvedMany.isEmpty() && nextOneId < oneHandler.getId(oneInstance)) {
				nextOneId = refIterator.getNextOneId();
				resolvedMany = refIterator.hasNext() ? refIterator.next() : new ArrayList<Many>();
			}
			if (nextOneId == oneHandler.getId(oneInstance)) {
				oneHandler.setResolvedRefs(oneInstance, resolvedMany);
			}
			destWriter.writeStreamable(oneInstance);
			oneInstance = oneIter.hasNext() ? oneIter.next() : null;
		}
		while (oneInstance != null) {
			destWriter.writeStreamable(oneInstance);
			oneInstance = oneIter.hasNext() ? oneIter.next() : null;
		}
		oneIter.close();
		refIterator.close();
		destWriter.close();
	}

	/**
	 * Sorts all references for the id of the one objects. This is needed for
	 * the next step, where the One instances are read and the resolved manies
	 * are given to them.
	 * 
	 * @param referenceFile
	 * @throws IOException
	 */
	protected void sortReferencesForOne(final File referenceFile) throws IOException {
		final StreamableSorter<Reference> sorter = new StreamableSorter<>(referenceFile, tempDir,
				new ReferenceFactory(), new SortReferenceForOneComparator(), maxSize);
		sorter.process();
	}

	/**
	 * Reads the many file and puts the many instances to the references that
	 * seem to search for them.
	 * 
	 * @param referenceFile
	 *            containing references to many objects
	 * @return The File containing the resolved references
	 * @throws IOException
	 */
	protected File setManiesToReferences(final File referenceFile) throws IOException {
		final File resolvedRefFile = File.createTempFile("resolvedref", "dat", tempDir);
		final StreamableIterator<Many> manyIterator = new StreamableIterator<>(manyFile,
				manyFactory);
		final StreamableIterator<Reference> refIterator = new StreamableIterator<>(referenceFile,
				new ReferenceFactory());
		final StreamableWriter<Reference> resolvedWriter = new StreamableWriter<>(resolvedRefFile);
		Many manyInstance = manyIterator.hasNext() ? manyIterator.next() : null;
		Reference reference = refIterator.hasNext() ? refIterator.next() : null;
		while (!(manyInstance == null || reference == null)) {
			while (reference != null && reference.getManyId() < manyHandler.getId(manyInstance)) {
				reference = refIterator.hasNext() ? refIterator.next() : null;
			}
			while (reference != null && reference.getManyId() == manyHandler.getId(manyInstance)) {
				reference.setManyInstance(manyInstance);
				resolvedWriter.writeStreamable(reference);
				reference = refIterator.hasNext() ? refIterator.next() : null;
			}
			manyInstance = manyIterator.hasNext() ? manyIterator.next() : null;
		}
		manyIterator.close();
		refIterator.close();
		resolvedWriter.close();
		return resolvedRefFile;
	}

	/**
	 * Sorts the references for the id of the many instances. This is needed for
	 * the next step, where the many file is read and the many instances are
	 * given to the references.
	 * 
	 * @param referenceFile
	 *            File containing unresolved references.
	 * @throws IOException
	 */
	protected void sortReferencesForMany(final File referenceFile) throws IOException {
		final StreamableSorter<Reference> sorter = new StreamableSorter<>(referenceFile, tempDir,
				new ReferenceFactory(), new SortReferenceForManyComparator(), maxSize);
		sorter.process();
	}

	/**
	 * reads the one file and gets all references to many instances from it an
	 * stores them into a reference file.
	 * 
	 * @return File containing unresolved references
	 * @throws IOException
	 */
	protected File createReferenceFile() throws IOException {
		final File referenceFile = File.createTempFile("reference", "dat", tempDir);
		final StreamableWriter<Reference> refWriter = new StreamableWriter<>(referenceFile);
		final StreamableIterator<One> oneIter = new StreamableIterator<>(oneFile, oneFactory);
		while (oneIter.hasNext()) {
			final One one = oneIter.next();
			final List<Long> refs = oneHandler.getRefs(one);
			for (final Long refId : refs) {
				final Many referedMany = manyFactory.construct();
				manyHandler.setId(referedMany, refId.longValue());
				refWriter.writeStreamable(new Reference(oneHandler.getId(one), refId, referedMany));
			}
		}
		refWriter.close();
		return referenceFile;
	}

	/**
	 * Handler that deals with the referenced class (Many). All operations that
	 * needs to be done on that class are covered by this handler.
	 * 
	 * @author benno
	 * 
	 * @param <Many>
	 */
	public interface ReferedHandler<Many> {
		/**
		 * @param many
		 *            Many instance that needs to be inspected
		 * @return the id of the Many instance
		 */
		long getId(Many many);

		/**
		 * 
		 * @param many
		 *            Many instance that needs to be changed
		 * @param id
		 *            id that shall be set to the instance
		 */
		void setId(Many many, long id);
	}

	/**
	 * Handler that deals with the referencing class (One). All operations that
	 * needs to be done on that class are covered by this handler.
	 * 
	 * @author benno
	 * 
	 * @param <One>
	 * @param <Many>
	 */
	public interface RefererHandler<One, Many> {
		/**
		 * @param one
		 *            one instance that needs to be inspected
		 * @return the id of the One instance
		 */
		long getId(One one);

		/**
		 * @param one
		 *            one instance that needs to be inspected
		 * @return a list of all references that One needs
		 */
		List<Long> getRefs(One one);

		/**
		 * @param one
		 *            one instance that needs to be changed
		 * @param manies
		 *            gives the resolved Many instances to One
		 */
		void setResolvedRefs(One one, List<Many> manies);
	}

	/**
	 * Comparator that allows a StreamableSorter to sort the References for the
	 * One id.
	 * 
	 * @author benno
	 */
	private class SortReferenceForOneComparator implements Comparator<Reference> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final Reference o1, final Reference o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			} else if (o2 == null) {
				return 1;
			}

			long compare = o1.getOneId() - o2.getOneId();
			if (compare < 0) {
				return -1;
			} else if (compare > 0) {
				return 1;
			}
			compare = o1.getManyId() - o2.getManyId();
			if (compare < 0) {
				return -1;
			} else if (compare > 0) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * Comparator that allows a StreamableSorter to sort the References for the
	 * Many id.
	 * 
	 * @author benno
	 */
	private class SortReferenceForManyComparator implements Comparator<Reference> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final Reference o1, final Reference o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			} else if (o2 == null) {
				return 1;
			}

			long compare = o1.getManyId() - o2.getManyId();
			if (compare < 0) {
				return -1;
			} else if (compare > 0) {
				return 1;
			}

			compare = o1.getOneId() - o2.getOneId();
			if (compare < 0) {
				return -1;
			} else if (compare > 0) {
				return 1;
			}

			return 0;
		}
	}

	/**
	 * Iterator to iterate over a resolved reference File, that returns all Many
	 * instances belonging to a One instance
	 * 
	 * @author benno
	 * 
	 */
	private class ReferenceIterator {
		private final StreamableIterator<Reference> iterator;
		private Reference next = null;

		/**
		 * @param f
		 *            File containing resolved References
		 * @throws IOException
		 */
		public ReferenceIterator(final File f) throws IOException {
			iterator = new StreamableIterator<>(f, new ReferenceFactory());
			next = iterator.hasNext() ? iterator.next() : null;
		}

		/**
		 * Closes the iterator when it is no longer needed.
		 * 
		 * @throws IOException
		 */
		public void close() throws IOException {
			iterator.close();
		}

		/**
		 * @return true when there are further resolved Many instances available
		 */
		public boolean hasNext() {
			return next != null;
		}

		/**
		 * @return id of the next One instance. This must be called before
		 *         next() is called.
		 */
		public long getNextOneId() {
			return next == null ? -1 : next.getOneId();
		}

		/**
		 * @return a list containing the Many instances that where resolved
		 * @throws IOException
		 */
		public List<Many> next() throws IOException {
			final List<Many> manies = new ArrayList<>();
			final long searchId = next.getOneId();
			while (next != null && searchId == next.getOneId()) {
				manies.add(next.getManyInstance());
				next = iterator.hasNext() ? iterator.next() : null;
			}
			return manies;
		}
	}

	/**
	 * This represents the reference between the Referrer (One) and the Referred
	 * (Many). For each one to many relation one instance of this class will be
	 * created and stored in a file. Later the resolved Many instance will be
	 * stored into it as well.
	 * 
	 * @author benno
	 */
	private class Reference implements Streamable {
		private long oneId;
		private long manyId;
		private Many manyInstance;

		/**
		 * Base constructor for Factory
		 */
		public Reference() {
			super();
			oneId = -1;
			manyId = -1;
			manyInstance = manyFactory.construct();
		}

		/**
		 * Constructor initializing all fields
		 * 
		 * @param oneId
		 *            the Id of the Referrer
		 * @param manyId
		 *            the id of the Referred.
		 * @param manyInstance
		 *            the instance of Many
		 */
		public Reference(final long oneId, final long manyId, final Many manyInstance) {
			super();
			this.oneId = oneId;
			this.manyId = manyId;
			this.manyInstance = manyInstance;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
		 */
		@Override
		public void writeExternal(final ObjectOutput out) throws IOException {
			out.writeLong(oneId);
			out.writeLong(manyId);
			manyInstance.writeExternal(out);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
		 */
		@Override
		public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
			oneId = in.readLong();
			manyId = in.readLong();
			manyInstance.readExternal(in);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.Streamable#toJSON()
		 */
		@Override
		public JSONObject toJSON() throws JSONException {
			final JSONObject json = new JSONObject();
			json.put("one", oneId);
			json.put("many", manyId);
			json.put("ref", manyInstance.toJSON());
			return json;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.Streamable#fromJSON(java.lang.String)
		 */
		@Override
		public void fromJSON(final String json) throws JSONException {
			final JSONObject node = new JSONObject(json);
			fromJSON(node);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.Streamable#fromJSON(org.json.JSONObject)
		 */
		@Override
		public void fromJSON(final JSONObject json) throws JSONException {
			oneId = json.getLong("one");
			manyId = json.getLong("many");
			manyInstance.fromJSON(json.getJSONObject("ref"));
		}

		/**
		 * @return the manyInstance
		 */
		public Many getManyInstance() {
			return manyInstance;
		}

		/**
		 * @param manyInstance
		 *            the manyInstance to set
		 */
		public void setManyInstance(final Many manyInstance) {
			this.manyInstance = manyInstance;
		}

		/**
		 * @return the oneId
		 */
		public long getOneId() {
			return oneId;
		}

		/**
		 * @return the manyId
		 */
		public long getManyId() {
			return manyId;
		}
	}

	/**
	 * Factory creating a reference
	 * 
	 * @author benno
	 */
	private class ReferenceFactory implements StreamableFactory<Reference> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.StreamableFactory#construct()
		 */
		@Override
		public Reference construct() {
			return new Reference();
		}
	}
}

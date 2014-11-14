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
 * @since 24.07.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mm.io.base.MemoryReservoir;

/**
 * MergeSort implementation that is able to sort a complete file of Streamable
 * objects using a Comparator. Hence the objects must not implement Comparable.
 * This sorter works like a TreeSet, there will not be 2 elements in the
 * resulting file where the comparator says that they are equal.
 * 
 * @author benno
 */
public class StreamableSorter<T extends Streamable> {
	protected final Comparator<T> comparator;
	protected final File f;
	protected final File tempDir;
	protected final StreamableFactory<T> factory;
	protected final int maxSize;
	protected LinkedList<File> splittedFiles = new LinkedList<>();
	protected static final int MaxSize = 50000;
	protected static final int reservoireSize = 20 * 1024 * 1024; // 20 MB

	/**
	 * Constructor for a MergeSorter with DefaulMaxSize first level sorted files
	 * 
	 * @param f
	 *            File that shall be sorted. This file will be replaced by a
	 *            sorted version
	 * @param tempDir
	 *            Directory that the sorter can use for its intermediate files
	 * @param factory
	 *            Factory for creating instances of T
	 * @param comparator
	 *            Comparator that shall be used for sorting
	 */
	public StreamableSorter(final File f, final File tempDir, final StreamableFactory<T> factory,
			final Comparator<T> comparator) {
		this(f, tempDir, factory, comparator, MaxSize);
	}

	/**
	 * Constructor setting the size of the first generated files.
	 * 
	 * @param f
	 *            File that shall be sorted. This file will be replaced by a
	 *            sorted version
	 * @param tempDir
	 *            Directory that the sorter can use for its intermediate files
	 * @param factory
	 *            Factory for creating instances of T
	 * @param comparator
	 *            Comparator that shall be used for sorting
	 * @param maxSize
	 *            Size of the first generated files. Note: the bigger this
	 *            number is, the faster the sorter will work.
	 */
	public StreamableSorter(final File f, final File tempDir, final StreamableFactory<T> factory,
			final Comparator<T> comparator, final int maxSize) {
		super();
		this.f = f;
		this.tempDir = tempDir;
		this.factory = factory;
		this.comparator = comparator;
		this.maxSize = maxSize;
	}

	/**
	 * Stores a first level file.
	 * 
	 * @param data
	 *            A presorted Set of elements
	 * @throws IOException
	 */
	protected void storeToTempFile(final Set<T> data) throws IOException {
		final File nextFile = File.createTempFile("merge", "dat", tempDir);
		final StreamableWriter<T> writer = new StreamableWriter<>(nextFile);
		for (final T t : data) {
			writer.writeStreamable(t);
		}
		writer.close();
		splittedFiles.add(nextFile);
	}

	/**
	 * presorts the complete file to the first level files.
	 * 
	 * @throws IOException
	 */
	protected void presortFile() throws IOException {
		final TreeSet<T> preSorter = new TreeSet<>(comparator);
		final StreamableIterator<T> iter = new StreamableIterator<>(f, factory);
		MemoryReservoir reservoir = new MemoryReservoir(reservoireSize);
		while (iter.hasNext()) {
			preSorter.add(iter.next());
			boolean isInReservoir = reservoir.isInReservoir();
			if (preSorter.size() >= maxSize || isInReservoir) {
				storeToTempFile(preSorter);
				preSorter.clear();
				if (isInReservoir || reservoir.isInReservoir()){
					reservoir = new MemoryReservoir(reservoireSize);	
				}
			}
		}
		if (preSorter.size() > 0) {
			storeToTempFile(preSorter);
			preSorter.clear();
		}
	}

	/**
	 * this is the MergeSort implementation. I merges to files together in the
	 * way that the smallest element is at the beginning of the file and the
	 * biggest at the end.
	 * 
	 * @param dest
	 *            Destination file where the merge result is stored in
	 * @param source1
	 *            File 1 that shall be merged
	 * @param source2
	 *            File 2 that shall be merged
	 * @throws IOException
	 */
	protected void mergeTwoFiles(final File dest, final File source1, final File source2)
			throws IOException {
		final StreamableWriter<T> writer = new StreamableWriter<>(dest);
		final StreamableIterator<T> iter1 = new StreamableIterator<>(source1, factory);
		final StreamableIterator<T> iter2 = new StreamableIterator<>(source2, factory);
		T element1 = iter1.hasNext() ? iter1.next() : null;
		T element2 = iter2.hasNext() ? iter2.next() : null;
		while (!(element1 == null || element2 == null)) {
			final int compare = comparator.compare(element1, element2);
			if (compare < 0) {
				writer.writeStreamable(element1);
				element1 = iter1.hasNext() ? iter1.next() : null;
			} else if (compare == 0) {
				element1 = iter1.hasNext() ? iter1.next() : null;
			} else {
				writer.writeStreamable(element2);
				element2 = iter2.hasNext() ? iter2.next() : null;
			}
		}
		if (element1 != null) {
			writer.writeStreamable(element1);
		}
		if (element2 != null) {
			writer.writeStreamable(element2);
		}
		while (iter1.hasNext()) {
			writer.writeStreamable(iter1.next());
		}
		while (iter2.hasNext()) {
			writer.writeStreamable(iter2.next());
		}
		writer.close();
	}

	protected void mergeManyFiles(final File dest, final List<File> source) throws IOException {
		final StreamableWriter<T> writer = new StreamableWriter<>(dest);
		final TreeSet<SortNode> sortedSet = new TreeSet<>();
		int i = 0;
		for (final File file : source) {
			final SortNode set = new SortNode(new StreamableIterator<>(file, factory), i++);
			if (set.hasNext()) {
				sortedSet.add(set);
			}
		}
		while (!sortedSet.isEmpty()) {
			final SortNode smallest = sortedSet.pollFirst();
			final T element = smallest.next();
			writer.writeStreamable(element);
			if (smallest.hasNext()) {
				sortedSet.add(smallest);
			}
		}
		writer.close();
	}

	/**
	 * Processes the complete sorting algorithm
	 * 
	 * @throws IOException
	 */
	public void process() throws IOException {
		presortFile();
		while (splittedFiles.size() > 2) {
			final List<File> files = new ArrayList<>();
			for (int i = 0; i < 32 && !splittedFiles.isEmpty(); ++i) {
				files.add(splittedFiles.pollFirst());
			}
			if (splittedFiles.isEmpty()) {
				mergeManyFiles(f, files);
			} else {
				final File dest = File.createTempFile("merge", "dat", tempDir);
				mergeManyFiles(dest, files);
				splittedFiles.addLast(dest);
			}
			for (final File file : files) {
				file.delete();
			}
		}
		if (splittedFiles.size() == 2) {
			final File source1 = splittedFiles.pollFirst();
			final File source2 = splittedFiles.pollFirst();
			mergeTwoFiles(f, source1, source2);
			source1.delete();
			source2.delete();
		} else if (splittedFiles.size() == 1) {
			final File source1 = splittedFiles.pollFirst();
			final StreamableWriter<T> writer = new StreamableWriter<>(f);
			final StreamableIterator<T> iter = new StreamableIterator<>(source1, factory);
			while (iter.hasNext()) {
				writer.writeStreamable(iter.next());
			}
			writer.close();
			source1.delete();
		}
	}

	private class SortNode implements Comparable<SortNode> {
		private final StreamableIterator<T> iter;
		private T element = null;
		private final int id;

		public SortNode(final StreamableIterator<T> iter, final int id) throws IOException {
			super();
			this.iter = iter;
			element = iter.hasNext() ? iter.next() : null;
			this.id = id;
		}

		public boolean hasNext() {
			return element != null;
		}

		public T next() throws IOException {
			final T current = element;
			element = iter.hasNext() ? iter.next() : null;
			return current;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(final SortNode o) {
			if (o == null) {
				return 1;
			}
			final int ret = comparator.compare(element, o.element);
			if (ret == 0) {
				return id < o.id ? -1 : 1;
			}
			return ret;
		}
	}
}

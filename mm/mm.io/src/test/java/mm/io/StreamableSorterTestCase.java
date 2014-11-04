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
 * @since 10.10.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mm.io.StreamableFactory;
import mm.io.StreamableIterator;
import mm.io.StreamableSorter;
import mm.io.StreamableWriter;
import mm.io.help.TestStreamable;

import org.junit.Test;

/**
 * @author benno
 * 
 */
public class StreamableSorterTestCase {

	/**
	 * Test method for {@link mm.io.StreamableSorter#process()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testProcess() throws IOException {
		final File tempFile = File.createTempFile("sorter", "dat");
		final File tempDir = File.createTempFile("sortertemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		final StreamableWriter<TestStreamable> writer = new StreamableWriter<>(tempFile);
		final StreamableSorter<TestStreamable> sorter = new StreamableSorter<>(tempFile, tempDir,
				new TestFactory(), new TestComparator());
		final List<Integer> values = new ArrayList<>();
		for (int i = 0; i < 500000; ++i) {
			values.add(Integer.valueOf(i));
		}
		Collections.shuffle(values);
		for (final Integer integer : values) {
			writer.writeStreamable(new TestStreamable(integer));
		}
		writer.close();
		sorter.process();
		int lastValue = -1;
		boolean firstTest = true;
		int number = 0;
		for (final StreamableIterator<TestStreamable> iter = new StreamableIterator<>(tempFile,
				new TestFactory()); iter.hasNext();) {
			final TestStreamable test = iter.next();
			if (firstTest) {
				firstTest = false;
			} else {
				assertTrue(lastValue < test.getMyId());
			}
			lastValue = test.getMyId();
			number++;
		}
		System.out.println("Found " + number + " items");
		tempFile.delete();
		tempDir.delete();
	}

	private static class TestFactory implements StreamableFactory<TestStreamable> {

		@Override
		public TestStreamable construct() {
			return new TestStreamable();
		}
	}

	private static class TestComparator implements Comparator<TestStreamable> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final TestStreamable o1, final TestStreamable o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			} else if (o2 == null) {
				return 1;
			}
			return Integer.valueOf(o1.getMyId()).compareTo(Integer.valueOf(o2.getMyId()));
		}

	}
}

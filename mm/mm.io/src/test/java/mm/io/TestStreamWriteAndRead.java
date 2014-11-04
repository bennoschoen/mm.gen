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
 * @since 06.10.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.File;
import java.io.IOException;

import mm.io.StreamableFactory;
import mm.io.StreamableIterator;
import mm.io.StreamableWriter;
import mm.io.help.TestStreamable;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author benno
 * 
 */
public class TestStreamWriteAndRead {

	private static final int NUMBER_OF_TEST_ELEMENTS = 1024;

	@Test
	public void test() throws IOException {
		final File f = File.createTempFile("random", "test");
		final StreamableWriter<TestStreamable> writer = new StreamableWriter<>(f);
		for (int i = 0; i < NUMBER_OF_TEST_ELEMENTS; ++i) {
			writer.writeStreamable(new TestStreamable(i));
		}
		writer.close();

		final StreamableIterator<TestStreamable> iter = new StreamableIterator<>(f,
				new StreamableFactory<TestStreamable>() {
					@Override
					public TestStreamable construct() {
						return new TestStreamable();
					}
				});
		int count = 0;
		while (iter.hasNext()) {
			final TestStreamable test = iter.next();
			Assert.assertEquals(count, test.getMyId());
			count++;
		}
	}
}

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
 * @since 13.10.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mm.io.ReferenceResolver;
import mm.io.StreamableFactory;
import mm.io.StreamableWriter;
import mm.io.ReferenceResolver.ReferedHandler;
import mm.io.ReferenceResolver.RefererHandler;
import mm.io.help.TestManyStreamable;
import mm.io.help.TestOneStreamable;

import org.junit.Before;
import org.junit.Test;

/**
 * @author benno
 * 
 */
public class TestReferenceResolver {

	private File oneFile = null;
	private File manyFile = null;
	private final StreamableFactory<TestOneStreamable> oneFactory = new StreamableFactory<TestOneStreamable>() {

		@Override
		public TestOneStreamable construct() {
			return new TestOneStreamable();
		}
	};

	private final StreamableFactory<TestManyStreamable> manyFactory = new StreamableFactory<TestManyStreamable>() {

		@Override
		public TestManyStreamable construct() {
			return new TestManyStreamable();
		}
	};

	private final RefererHandler<TestOneStreamable, TestManyStreamable> oneHandler = new RefererHandler<TestOneStreamable, TestManyStreamable>() {

		@Override
		public long getId(final TestOneStreamable one) {
			return one.getMyId();
		}

		@Override
		public List<Long> getRefs(final TestOneStreamable one) {
			final List<Long> refs = new ArrayList<>();
			for (final TestManyStreamable many : one.getManies()) {
				refs.add(Long.valueOf(many.getMyId()));
			}
			return refs;
		}

		@Override
		public void setResolvedRefs(final TestOneStreamable one,
				final List<TestManyStreamable> manies) {
			one.setManies(manies);
		}
	};

	private final ReferedHandler<TestManyStreamable> manyHandler = new ReferedHandler<TestManyStreamable>() {

		@Override
		public long getId(final TestManyStreamable many) {
			return many.getMyId();
		}

		@Override
		public void setId(final TestManyStreamable many, final long id) {
			many.setMyId((int) id);
		}
	};

	@Before
	public void setup() throws IOException {
		oneFile = File.createTempFile("onefile", "dat");
		manyFile = File.createTempFile("manyfile", "dat");
		final StreamableWriter<TestOneStreamable> oneWriter = new StreamableWriter<>(oneFile);
		final StreamableWriter<TestManyStreamable> manyWriter = new StreamableWriter<>(manyFile);
		final int manyToOne = 5;
		final int oneAmount = 10;
		final int manyAmount = manyToOne * oneAmount;
		final List<TestManyStreamable> manies = new ArrayList<>();

		for (int i = 0; i < manyAmount; ++i) {
			manies.add(new TestManyStreamable(i, false));
			manyWriter.writeStreamable(new TestManyStreamable(i, true));
		}
		Collections.shuffle(manies);
		for (int i = 0; i < oneAmount; ++i) {
			final List<TestManyStreamable> oneManies = new ArrayList<>();
			for (int j = 0; j < manyToOne; ++j) {
				final TestManyStreamable many = manies.get(i * manyToOne + j);
				oneManies.add(many);
			}
			oneWriter.writeStreamable(new TestOneStreamable(i, oneManies));
		}
		oneWriter.close();
		manyWriter.close();
	}

	/**
	 * Test method for
	 * {@link mm.io.ReferenceResolver#resolveReferences(java.io.File, java.io.File, java.io.File, java.io.File, mm.io.StreamableFactory, mm.io.StreamableFactory, mm.io.ReferenceResolver.ReferedHandler, mm.io.ReferenceResolver.RefererHandler)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testResolveReferences() throws IOException {
		final File tempDir = File.createTempFile("resolvetemp", "");
		tempDir.delete();
		tempDir.mkdirs();
		final File destFile = File.createTempFile("resolved", "dat");

		ReferenceResolver.resolveReferences(destFile, oneFile, manyFile, tempDir, oneFactory,
				manyFactory, manyHandler, oneHandler);

	}
}

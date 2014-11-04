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
 * @since 16.10.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class StreamableFilter<T extends Streamable> {

	private final File destFile;
	private final File sourceFile;
	private final File idFile;
	private final StreamableFactory<T> factory;
	private final SourceHandler<T> sourceHandler;
	private final boolean whiteList;

	private StreamableFilter(final File sourceFile, final File destFile, final File idFile,
			final StreamableFactory<T> factory, final SourceHandler<T> sourceHandler,
			final boolean whiteList) {
		super();
		this.sourceFile = sourceFile;
		this.destFile = destFile;
		this.idFile = idFile;
		this.factory = factory;
		this.sourceHandler = sourceHandler;
		this.whiteList = whiteList;
	}

	private void filter() throws IOException {
		final StreamableIterator<T> sourceIter = new StreamableIterator<>(sourceFile, factory);
		final StreamableIterator<ID> idIter = new StreamableIterator<>(idFile, new IDFactory());
		final StreamableWriter<T> destWriter = new StreamableWriter<>(destFile);
		ID id = idIter.hasNext() ? idIter.next() : null;
		T source = sourceIter.hasNext() ? sourceIter.next() : null;
		while (!(id == null || source == null)) {
			while (source != null && sourceHandler.getId(source) < id.getId()) {
				if (!whiteList) {
					destWriter.writeStreamable(source);
				}
				source = sourceIter.hasNext() ? sourceIter.next() : null;
			}
			if (source != null && sourceHandler.getId(source) == id.getId()) {
				if (whiteList) {
					destWriter.writeStreamable(source);
				}
				source = sourceIter.hasNext() ? sourceIter.next() : null;
			}
			id = idIter.hasNext() ? idIter.next() : null;
		}
		if (!whiteList) {
			while (source != null) {
				destWriter.writeStreamable(source);
				source = sourceIter.hasNext() ? sourceIter.next() : null;
			}
		}
		sourceIter.close();
		idIter.close();
		destWriter.close();
	}

	public static <T extends Streamable> void filter(final File sourceFile, final File destFile,
			final File idFile, final StreamableFactory<T> factory,
			final SourceHandler<T> sourceHandler, final boolean whiteList) throws IOException {
		final StreamableFilter<T> filter = new StreamableFilter<>(sourceFile, destFile, idFile,
				factory, sourceHandler, whiteList);
		filter.filter();
	}

	public static class ID implements Streamable {
		private long id = -1;

		public ID() {
			super();
		}

		public ID(final long id) {
			super();
			this.id = id;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
		 */
		@Override
		public void writeExternal(final ObjectOutput out) throws IOException {
			out.writeLong(id);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
		 */
		@Override
		public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
			id = in.readLong();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.Streamable#toJSON()
		 */
		@Override
		public JSONObject toJSON() throws JSONException {
			final JSONObject json = new JSONObject();
			json.put("id", id);
			return json;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.Streamable#fromJSON(java.lang.String)
		 */
		@Override
		public void fromJSON(final String json) throws JSONException {
			fromJSON(new JSONObject(json));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.Streamable#fromJSON(org.json.JSONObject)
		 */
		@Override
		public void fromJSON(final JSONObject json) throws JSONException {
			id = json.getLong("id");
		}

		/**
		 * @return the id
		 */
		public long getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(final long id) {
			this.id = id;
		}
	}

	public static class IDFactory implements StreamableFactory<ID> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.StreamableFactory#construct()
		 */
		@Override
		public ID construct() {
			return new ID();
		}
	}

	public static class IDComparator implements Comparator<ID> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(final ID o1, final ID o2) {
			if (o1 == null) {
				return o2 == null ? 0 : -1;
			} else if (o2 == null) {
				return 1;
			}

			final long compare = o1.getId() - o2.getId();
			if (compare < 0) {
				return -1;
			} else if (compare > 0) {
				return 1;
			}
			return 0;
		}
	}

	public interface SourceHandler<T extends Streamable> {
		long getId(T instance);
	}
}

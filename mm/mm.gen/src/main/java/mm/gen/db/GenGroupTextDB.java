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
 * @since 17.05.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.io.File;
import java.io.IOException;

import mm.io.StreamableFactory;
import mm.io.StreamableIterator;
import mm.io.StreamableWriter;
import mm.lay.types.Carrier;
import mm.text.TextDbGenerator;
import mm.text.TextDbGenerator.TextLink;

/**
 * @author benno
 * 
 */
public class GenGroupTextDB {
	final TextDbGenerator textDbA;
	final File groupFile;

	public GenGroupTextDB(final TextDbGenerator textDbA, final File groupFile) {
		super();
		this.textDbA = textDbA;
		this.groupFile = groupFile;
	}

	public void genTextDb() {
		try {
			final File temp = new File(groupFile.getCanonicalPath() + ".temp");
			groupFile.renameTo(temp);
			final StreamableWriter<Carrier> writer = new StreamableWriter<>(groupFile);
			final StreamableIterator<Carrier> iter = new StreamableIterator<>(temp,
					new StreamableFactory<Carrier>() {
						@Override
						public Carrier construct() {
							return new Carrier();
						}
					});
			TextLink link = null;
			while (iter.hasNext()) {
				final Carrier carrier = iter.next();
				final String name = carrier.getName();
				if (!(name == null || name.isEmpty())) {
					link = textDbA.addText(name, carrier.getCellId(), carrier.getNumberInCell(),
							carrier.getObjectType());
					carrier.setTextIndex((short) link.getRow());
					carrier.setTextOffset((byte) link.getOffset());
				}
				writer.writeStreamable(carrier);
			}
			writer.close();
			temp.delete();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}

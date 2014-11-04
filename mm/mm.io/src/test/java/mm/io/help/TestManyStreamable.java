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
package mm.io.help;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class TestManyStreamable extends TestStreamable {
	private boolean resolved = false;

	public TestManyStreamable() {
		super();
	}

	public TestManyStreamable(final int myId, final boolean resolved) {
		super(myId);
		this.resolved = resolved;
	}

	/**
	 * @return the resolved
	 */
	public boolean isResolved() {
		return resolved;
	}

	/**
	 * @param resolved
	 *            the resolved to set
	 */
	public void setResolved(final boolean resolved) {
		this.resolved = resolved;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.yamap.mm.io.help.TestStreamable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(resolved);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.help.TestStreamable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		resolved = in.readBoolean();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.help.TestStreamable#toJSON()
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		final JSONObject json = super.toJSON();
		json.put("resolved", resolved);
		return json;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.help.TestStreamable#fromJSON(org.json.JSONObject)
	 */
	@Override
	public void fromJSON(final JSONObject json) throws JSONException {
		super.fromJSON(json);
		resolved = json.getBoolean("resolved");
	}
}

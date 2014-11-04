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
package mm.io.help;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import mm.io.Streamable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class TestStreamable implements Streamable {

	private int myId = 0;

	public TestStreamable() {
		super();
	}

	public TestStreamable(final int myId) {
		super();
		this.myId = myId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeInt(myId);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		myId = in.readInt();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.Streamable#toJSON()
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		final JSONObject obj = new JSONObject();
		obj.put("myid", myId);
		return obj;
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
		myId = json.getInt("myid");
	}

	/**
	 * @return the myId
	 */
	public int getMyId() {
		return myId;
	}

	/**
	 * @param myId
	 *            the myId to set
	 */
	public void setMyId(final int myId) {
		this.myId = myId;
	}
}

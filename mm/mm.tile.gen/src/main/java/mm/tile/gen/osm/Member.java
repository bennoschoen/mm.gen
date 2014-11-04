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
 * @since 19.07.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen.osm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import mm.io.StreamIo;
import mm.io.Streamable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class Member<T extends Streamable> implements Streamable {

	private final T ref;
	private String role = "";

	public Member(final T ref) {
		super();
		this.ref = ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		StreamIo.writeString(out, role);
		ref.writeExternal(out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		role = StreamIo.readString(in);
		ref.readExternal(in);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mm.io.Streamable#toJSON()
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		final JSONObject ret = new JSONObject();
		ret.put("role", role);
		ret.put("ref", ref.toJSON());
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mm.io.Streamable#fromJSON(java.lang.String)
	 */
	@Override
	public void fromJSON(final String json) throws JSONException {
		final JSONObject way = new JSONObject(json);
		fromJSON(way);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see mm.io.Streamable#fromJSON(org.json.JSONObject)
	 */
	@Override
	public void fromJSON(final JSONObject json) throws JSONException {
		try {
			role = json.getString("role");
			ref.fromJSON(json.getJSONObject("ref"));
		} catch (final Throwable e) {
			System.out.println(json.toString(4));
			throw e;
		}
	}

	/**
	 * @return the ref
	 */
	public T getRef() {
		return ref;
	}

	/**
	 * @return the role
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role
	 *            the role to set
	 */
	public void setRole(final String role) {
		this.role = role;
	}
}

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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class TestOneStreamable extends TestStreamable {

	private List<TestManyStreamable> manies = new ArrayList<>();

	public TestOneStreamable() {
		super();
	}

	public TestOneStreamable(final int myId, final List<TestManyStreamable> manies) {
		super(myId);
		this.manies = manies;
	}

	/**
	 * @return the manies
	 */
	public List<TestManyStreamable> getManies() {
		return manies;
	}

	/**
	 * @param manies
	 *            the manies to set
	 */
	public void setManies(final List<TestManyStreamable> manies) {
		this.manies.clear();
		this.manies.addAll(manies);
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
		out.writeInt(manies.size());
		for (final TestManyStreamable many : manies) {
			many.writeExternal(out);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.help.TestStreamable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		manies.clear();
		super.readExternal(in);
		final int amount = in.readInt();
		for (int i = 0; i < amount; ++i) {
			final TestManyStreamable many = new TestManyStreamable();
			many.readExternal(in);
			manies.add(many);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.help.TestStreamable#toJSON()
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		final JSONObject json = super.toJSON();
		final JSONArray jMany = new JSONArray();
		for (final TestManyStreamable many : manies) {
			jMany.put(many.toJSON());
		}
		json.put("many", jMany);
		return json;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.help.TestStreamable#fromJSON(org.json.JSONObject)
	 */
	@Override
	public void fromJSON(final JSONObject json) throws JSONException {
		manies.clear();
		super.fromJSON(json);
		final JSONArray jMany = json.getJSONArray("many");
		for (int i = 0; i < jMany.length(); ++i) {
			final TestManyStreamable many = new TestManyStreamable();
			many.fromJSON(jMany.getJSONObject(i));
			manies.add(many);
		}
	}
}

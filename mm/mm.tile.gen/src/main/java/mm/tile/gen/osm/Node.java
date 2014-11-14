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
 * @since 27.09.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen.osm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Set;

import mm.io.StreamIo;
import mm.io.Streamable;
import mm.io.StreamableFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Node implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6950455165886155779L;
	protected long id = 0;
	protected int lat = 0;
	protected int lon = 0;

	protected final HashMap<String, String> props = new HashMap<String, String>();
	protected final Set<String> keySet = props.keySet();

	public Node() {
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

	/**
	 * @return the lat
	 */
	public int getLat() {
		return lat;
	}

	/**
	 * @param lat
	 *            the lat to set
	 */
	public void setLat(final int lat) {
		this.lat = lat;
	}

	/**
	 * @return the lon
	 */
	public int getLon() {
		return lon;
	}

	/**
	 * @param lon
	 *            the lon to set
	 */
	public void setLon(final int lon) {
		this.lon = lon;
	}

	public void addProperty(final String key, final String value) {
		props.put(key, value);
	}

	public String getProperty(final String key) {
		return props.get(key);
	}

	public String getProperty(final String key, final String defaultValue) {
		String ret = getProperty(key);
		if (ret == null) {
			ret = defaultValue;
		}
		return ret;
	}

	/**
	 * @return the props
	 */
	public HashMap<String, String> getProps() {
		return props;
	}

	/**
	 * @param props
	 *            the props to set
	 */
	public void setProps(final HashMap<String, String> props) {
		this.props.clear();
		this.props.putAll(props);
	}

	public void copy(final Node node) {
		props.clear();
		id = node.getId();
		lat = node.getLat();
		lon = node.getLon();
		props.putAll(node.getProps());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		writeExternal(out, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(final ObjectOutput out, final boolean containProps)
			throws IOException {
		out.writeLong(id);
		out.writeInt(lat);
		out.writeInt(lon);
		if (containProps && !props.isEmpty()) {
			out.writeInt(keySet.size());
			for (final String key : keySet) {
				StreamIo.writeString(out, key);
				StreamIo.writeString(out, props.get(key));
			}
		} else {
			out.writeInt(0);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		props.clear();
		id = in.readLong();
		lat = in.readInt();
		lon = in.readInt();
		final int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.Streamable#toJSON()
	 */
	@Override
	public JSONObject toJSON() throws JSONException {
		final JSONObject ret = new JSONObject();
		ret.put("lat", lat);
		ret.put("lon", lon);
		ret.put("id", id);
		final JSONArray props = new JSONArray();
		for (final String key : keySet) {
			final JSONObject prop = new JSONObject();
			prop.put("k", key);
			prop.put("v", this.props.get(key));
			props.put(prop);
		}
		ret.put("props", props);
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.Streamable#fromJSON(java.lang.String)
	 */
	@Override
	public void fromJSON(final String json) throws JSONException {
		final JSONObject node = new JSONObject(json);
		fromJSON(node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.yamap.mm.io.Streamable#fromJSON(org.json.JSONObject)
	 */
	@Override
	public void fromJSON(final JSONObject json) throws JSONException {
		props.clear();
		id = json.optLong("id", -1);
		lat = json.getInt("lat");
		lon = json.getInt("lon");
		final JSONArray jProps = json.optJSONArray("props");
		for (int i = 0; (jProps != null) && (i < jProps.length()); ++i) {
			final JSONObject prop = jProps.getJSONObject(i);
			final String key = prop.optString("k", "");
			final String value = prop.optString("v", "");
			addProperty(key, value);
		}
	}

	public static class NodeFactory implements StreamableFactory<Node> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.StreamableFactory#construct()
		 */
		@Override
		public Node construct() {
			return new Node();
		}

	}
}

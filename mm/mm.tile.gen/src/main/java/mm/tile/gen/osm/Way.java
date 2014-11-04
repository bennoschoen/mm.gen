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
 * @since 28.09.2014
 * @version 1.0
 * @author benno
 */
package mm.tile.gen.osm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mm.io.StreamIo;
import mm.io.Streamable;
import mm.io.StreamableFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author benno
 * 
 */
public class Way implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3176353537702323119L;
	protected long id = 0;
	protected List<Node> nodes = new ArrayList<>();
	protected HashMap<String, String> props = new HashMap<String, String>();

	public Way() {

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

	public void addNode(final Node ref) {
		nodes.add(ref);
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

	/**
	 * @return the nodes
	 */
	public List<Node> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes
	 *            the nodes to set
	 */
	public void setNodes(final List<Node> nodes) {
		this.nodes.clear();
		this.nodes.addAll(nodes);
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public Bounds getBounds() {
		Bounds bounds = null;
		for (final Node node : nodes) {
			if (bounds == null) {
				bounds = new Bounds(node.getLat(), node.getLon(), node.getLat(), node.getLon());
			} else {
				bounds.increaseBounds(node.getLat(), node.getLon());
			}
		}
		return bounds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeLong(id);
		final Set<String> keys = props.keySet();
		out.writeInt(keys.size());
		for (final String key : keys) {
			StreamIo.writeString(out, key);
			StreamIo.writeString(out, props.get(key));
		}
		out.writeInt(nodes.size());
		for (final Node node : nodes) {
			node.writeExternal(out, false);
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
		nodes.clear();
		id = in.readLong();
		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Node node = new Node();
			node.readExternal(in);
			nodes.add(node);
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
		final JSONArray props = new JSONArray();
		final JSONArray jNodes = new JSONArray();

		ret.put("id", id);
		for (final String key : this.props.keySet()) {
			final JSONObject prop = new JSONObject();
			prop.put("k", key);
			prop.put("v", this.props.get(key));
			props.put(prop);
		}
		ret.put("props", props);
		for (final Node node : nodes) {
			jNodes.put(node.toJSON());
		}
		ret.put("nodes", jNodes);
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
		nodes.clear();
		id = json.optLong("id", -1);
		final JSONArray jProps = json.optJSONArray("props");
		for (int i = 0; i < jProps.length(); ++i) {
			final JSONObject prop = jProps.getJSONObject(i);
			final String key = prop.getString("k");
			final String value = prop.getString("v");
			addProperty(key, value);
		}
		final JSONArray jNodes = json.optJSONArray("nodes");
		for (int i = 0; i < jNodes.length(); ++i) {
			final Node node = new Node();
			node.fromJSON(jNodes.getJSONObject(i));
			nodes.add(node);
		}
	}

	public static class WayFactory implements StreamableFactory<Way> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.StreamableFactory#construct()
		 */
		@Override
		public Way construct() {
			return new Way();
		}

	}
}

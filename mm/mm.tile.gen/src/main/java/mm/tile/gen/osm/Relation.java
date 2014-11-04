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
public class Relation implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3176353537702323119L;
	protected long id = 0;
	protected List<Member<Node>> nodes = new ArrayList<>();
	protected List<Member<Way>> ways = new ArrayList<>();
	protected List<Member<Relation>> relations = new ArrayList<>();
	protected HashMap<String, String> props = new HashMap<String, String>();

	public Relation() {

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

	public void addNode(final Member<Node> ref) {
		nodes.add(ref);
	}

	public void addWay(final Member<Way> ref) {
		ways.add(ref);
	}

	public void addRelation(final Member<Relation> ref) {
		relations.add(ref);
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
	public List<Member<Node>> getNodes() {
		return nodes;
	}

	public List<Member<Way>> getWays() {
		return ways;
	}

	public List<Member<Relation>> getRelations() {
		return relations;
	}

	public Bounds getBounds() {
		Bounds bounds = null;
		for (final Member<Node> member : nodes) {
			final Node node = member.getRef();
			if (bounds == null) {
				bounds = new Bounds(node.getLat(), node.getLon(), node.getLat(), node.getLon());
			} else {
				bounds.increaseBounds(node.getLat(), node.getLon());
			}
		}
		for (final Member<Way> member : ways) {
			final Way way = member.getRef();
			final Bounds wayBounds = way.getBounds();
			if (wayBounds != null) {
				if (bounds == null) {
					bounds = new Bounds();
				} else {
					bounds.increaseBounds(way.getBounds());
				}
			}
		}
		for (final Member<Relation> member : relations) {
			final Relation relation = member.getRef();
			final Bounds relationBounds = relation.getBounds();
			if (relationBounds != null) {
				if (bounds == null) {
					bounds = new Bounds(relationBounds);
				} else {
					bounds.increaseBounds(relation.getBounds());
				}
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
		for (final Member<Node> node : nodes) {
			node.writeExternal(out);
		}
		out.writeInt(ways.size());
		for (final Member<Way> way : ways) {
			way.writeExternal(out);
		}
		out.writeInt(relations.size());
		for (final Member<Relation> relation : relations) {
			relation.writeExternal(out);
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
		ways.clear();
		relations.clear();
		id = in.readLong();
		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Member<Node> node = new Member<>(new Node());
			node.readExternal(in);
			nodes.add(node);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Member<Way> way = new Member<>(new Way());
			way.readExternal(in);
			ways.add(way);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Member<Relation> relation = new Member<>(new Relation());
			relation.readExternal(in);
			relations.add(relation);
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
		final JSONArray jWays = new JSONArray();
		final JSONArray jRelations = new JSONArray();

		ret.put("id", id);
		for (final String key : this.props.keySet()) {
			final JSONObject prop = new JSONObject();
			prop.put("k", key);
			prop.put("v", this.props.get(key));
			props.put(prop);
		}
		ret.put("props", props);
		for (final Member<Node> node : nodes) {
			jNodes.put(node.toJSON());
		}
		ret.put("nodes", jNodes);
		for (final Member<Way> way : ways) {
			jWays.put(way.toJSON());
		}
		ret.put("ways", jWays);
		for (final Member<Relation> relation : relations) {
			jRelations.put(relation.toJSON());
		}
		ret.put("relations", jRelations);
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
		ways.clear();
		relations.clear();
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
			final Member<Node> node = new Member<>(new Node());
			node.fromJSON(jNodes.getJSONObject(i));
			nodes.add(node);
		}
		final JSONArray jWays = json.optJSONArray("ways");
		for (int i = 0; i < jWays.length(); ++i) {
			final Member<Way> way = new Member<>(new Way());
			way.fromJSON(jWays.getJSONObject(i));
			ways.add(way);
		}
		final JSONArray jRelations = json.optJSONArray("relations");
		for (int i = 0; i < jRelations.length(); ++i) {
			final Member<Relation> relation = new Member<>(new Relation());
			relation.fromJSON(jRelations.getJSONObject(i));
			relations.add(relation);
		}
	}

	public static class RelationFactory implements StreamableFactory<Relation> {
		/*
		 * (non-Javadoc)
		 * 
		 * @see de.yamap.mm.io.StreamableFactory#construct()
		 */
		@Override
		public Relation construct() {
			return new Relation();
		}

	}
}

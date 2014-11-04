package mm.gen.osm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import mm.io.StreamIo;
import mm.io.Streamable;
import mm.lay.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Way implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3176353537702323119L;
	protected long id = 0;
	protected List<Node> nodes = new ArrayList<Node>();
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

	public void addNode(final Node node) {
		nodes.add(node);
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

	@Override
	public JSONObject toJSON() {
		final JSONObject ret = new JSONObject();
		try {
			final JSONArray props = new JSONArray();
			final JSONArray nodes = new JSONArray();

			ret.put("id", id);
			for (final String key : this.props.keySet()) {
				final JSONObject prop = new JSONObject();
				prop.put("k", key);
				prop.put("v", this.props.get(key));
				props.put(prop);
			}
			ret.put("props", props);
			for (final Node node : this.nodes) {
				nodes.put(node.toJSON());
			}
			ret.put("nodes", nodes);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void fromJSON(final String json) {
		try {
			final JSONObject way = new JSONObject(json);
			fromJSON(way);
		} catch (final JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fromJSON(final JSONObject json) {
		try {
			props.clear();
			nodes.clear();
			id = json.optLong("id", -1);
			final JSONArray jProps = json.optJSONArray("props");
			for (int i = 0; i < jProps.length(); ++i) {
				final JSONObject prop = jProps.getJSONObject(i);
				final String key = prop.optString("k", "");
				final String value = prop.optString("v", "");
				addProperty(key, value);
			}
			final JSONArray jNodes = json.optJSONArray("nodes");
			for (int i = 0; i < jNodes.length(); ++i) {
				final JSONObject jNode = jNodes.getJSONObject(i);
				final Node node = new Node();
				node.fromJSON(jNode);
				nodes.add(node);
			}
		} catch (final JSONException e) {
			e.printStackTrace();
		}
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

	public LatLng getBottomLeft() {
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		boolean first = true;
		for (final Node node : nodes) {
			if (first) {
				minLat = node.getLat();
				minLon = node.getLon();
				first = false;
			} else {
				minLat = Math.min(minLat, node.getLat());
				minLon = Math.min(minLon, node.getLon());
			}
		}
		return new LatLng(minLat, minLon);
	}

	public LatLng getTopRight() {
		double minLat = Double.MIN_VALUE;
		double minLon = Double.MIN_VALUE;
		boolean first = true;
		for (final Node node : nodes) {
			if (first) {
				minLat = node.getLat();
				minLon = node.getLon();
				first = false;
			} else {
				minLat = Math.max(minLat, node.getLat());
				minLon = Math.max(minLon, node.getLon());
			}
		}
		return new LatLng(minLat, minLon);
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
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
			node.writeExternal(out);
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
}

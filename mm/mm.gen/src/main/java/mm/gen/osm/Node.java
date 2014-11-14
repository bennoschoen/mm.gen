package mm.gen.osm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Set;

import mm.io.StreamIo;
import mm.io.Streamable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Node implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6950455165886155779L;
	protected long id = 0;
	protected double lat = 0;
	protected double lon = 0;

	protected final HashMap<String, String> props = new HashMap<String, String>();
	protected final Set<String> keySet = props.keySet();

	public Node() {
	}

	public Node(final String json) {
		fromJSON(json);
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
	public double getLat() {
		return lat;
	}

	/**
	 * @param lat
	 *            the lat to set
	 */
	public void setLat(final double lat) {
		this.lat = lat;
	}

	/**
	 * @return the lon
	 */
	public double getLon() {
		return lon;
	}

	/**
	 * @param lon
	 *            the lon to set
	 */
	public void setLon(final double lon) {
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

	@Override
	public JSONObject toJSON() {
		final JSONObject ret = new JSONObject();
		try {
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
		} catch (final JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public void copy(final Node node) {
		props.clear();
		id = node.getId();
		lat = node.getLat();
		lon = node.getLon();
		props.putAll(node.getProps());
	}

	@Override
	public void fromJSON(final String json) {
		try {
			final JSONObject node = new JSONObject(json);
			fromJSON(node);
		} catch (final JSONException e) {
			System.out.println(json);
			e.printStackTrace();
		}
	}

	@Override
	public void fromJSON(final JSONObject json) {
		try {
			props.clear();
			id = json.optLong("id", -1);
			lat = json.optDouble("lat", -90);
			lon = json.optDouble("lon", -180);
			final JSONArray jProps = json.optJSONArray("props");
			for (int i = 0; (jProps != null) && (i < jProps.length()); ++i) {
				final JSONObject prop = jProps.getJSONObject(i);
				final String key = prop.optString("k", "");
				final String value = prop.optString("v", "");
				addProperty(key, value);
			}
		} catch (final JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(final ObjectOutput out) throws IOException {
		out.writeLong(id);
		out.writeDouble(lat);
		out.writeDouble(lon);
		out.writeInt(keySet.size());
		for (final String key : keySet) {
			StreamIo.writeString(out, key);
			StreamIo.writeString(out, props.get(key));
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
		lat = in.readDouble();
		lon = in.readDouble();
		final int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
	}
}

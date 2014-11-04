package mm.gen.osm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mm.gen.db.BoundingBox;
import mm.io.StreamIo;
import mm.io.Streamable;
import mm.lay.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MultiWay implements Streamable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3176353537702323119L;
	protected long id = 0;
	protected HashMap<String, String> props = new HashMap<String, String>();
	protected List<Way> innerWays = new ArrayList<>();
	protected List<Way> outerWays = new ArrayList<>();

	public MultiWay() {

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
	public void setProps(final Map<String, String> props) {
		this.props.clear();
		this.props.putAll(props);
	}

	public void addOuterWay(final Way way) {
		if (BoundingBox.rightRound(way)) {
			outerWays.add(way);
		} else {
			outerWays.add(BoundingBox.turnWayArround(way));
		}
	}

	public void addInnerWay(final Way way) {
		if (BoundingBox.rightRound(way)) {
			innerWays.add(BoundingBox.turnWayArround(way));
		} else {
			innerWays.add(way);
		}
	}

	public List<Way> getInnerWays() {
		return innerWays;
	}

	public List<Way> getOuterWays() {
		return outerWays;
	}

	public void setInnerWays(final List<Way> innerWays) {
		this.innerWays.clear();
		this.innerWays.addAll(innerWays);
	}

	public void setOuterWays(final List<Way> outerWays) {
		this.outerWays.clear();
		this.outerWays.addAll(outerWays);
	}

	@Override
	public JSONObject toJSON() {
		final JSONObject ret = new JSONObject();
		try {
			final JSONArray props = new JSONArray();
			final JSONArray outerWays = new JSONArray();
			final JSONArray innerWays = new JSONArray();

			ret.put("id", id);
			for (final String key : this.props.keySet()) {
				final JSONObject prop = new JSONObject();
				prop.put("k", key);
				prop.put("v", this.props.get(key));
				props.put(prop);
			}
			ret.put("props", props);
			for (final Way way : this.outerWays) {
				outerWays.put(way.toJSON());
			}
			ret.put("outerWays", outerWays);
			for (final Way way : this.innerWays) {
				innerWays.put(way.toJSON());
			}
			ret.put("innerWays", innerWays);
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
			innerWays.clear();
			outerWays.clear();
			id = json.optLong("id", -1);
			final JSONArray jProps = json.optJSONArray("props");
			for (int i = 0; i < jProps.length(); ++i) {
				final JSONObject prop = jProps.getJSONObject(i);
				final String key = prop.optString("k", "");
				final String value = prop.optString("v", "");
				addProperty(key, value);
			}
			final JSONArray jOuterWays = json.optJSONArray("outerWays");
			for (int i = 0; i < jOuterWays.length(); ++i) {
				final JSONObject jWay = jOuterWays.getJSONObject(i);
				final Way way = new Way();
				way.fromJSON(jWay);
				outerWays.add(way);
			}
			final JSONArray jInnerWays = json.optJSONArray("innerWays");
			for (int i = 0; i < jInnerWays.length(); ++i) {
				final JSONObject jWay = jInnerWays.getJSONObject(i);
				final Way way = new Way();
				way.fromJSON(jWay);
				innerWays.add(way);
			}
		} catch (final JSONException e) {
			e.printStackTrace();
		}
	}

	public LatLng getBottomLeft() {
		double minLat = Double.MAX_VALUE;
		double minLon = Double.MAX_VALUE;
		boolean first = true;
		for (final Way way : outerWays) {
			if (first) {
				minLat = way.getBottomLeft().getLat();
				minLon = way.getBottomLeft().getLon();
				first = false;
			} else {
				minLat = Math.min(minLat, way.getBottomLeft().getLat());
				minLon = Math.min(minLon, way.getBottomLeft().getLon());
			}
		}
		return new LatLng(minLat, minLon);
	}

	public LatLng getTopRight() {
		double maxLat = Double.MIN_VALUE;
		double maxLon = Double.MIN_VALUE;
		boolean first = true;
		for (final Way way : outerWays) {
			if (first) {
				maxLat = way.getTopRight().getLat();
				maxLon = way.getTopRight().getLon();
				first = false;
			} else {
				maxLat = Math.max(maxLat, way.getTopRight().getLat());
				maxLon = Math.max(maxLon, way.getTopRight().getLon());
			}
		}
		return new LatLng(maxLat, maxLon);
	}

	public boolean isEmpty() {
		boolean notEmpty = false;
		for (final Way outerWay : outerWays) {
			if (!outerWay.getNodes().isEmpty()) {
				notEmpty = true;
			}
		}
		return !notEmpty;
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
		out.writeInt(outerWays.size());
		for (final Way way : outerWays) {
			way.writeExternal(out);
		}
		out.writeInt(innerWays.size());
		for (final Way way : innerWays) {
			way.writeExternal(out);
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
		innerWays.clear();
		outerWays.clear();
		id = in.readLong();
		int size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final String key = StreamIo.readString(in);
			final String value = StreamIo.readString(in);
			props.put(key, value);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Way way = new Way();
			way.readExternal(in);
			outerWays.add(way);
		}
		size = in.readInt();
		for (int i = 0; i < size; ++i) {
			final Way way = new Way();
			way.readExternal(in);
			innerWays.add(way);
		}
	}
}

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
 * @since 15.03.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.util.Map;

/**
 * @author benno
 * 
 */
public class WayClassifier {

	public enum Graphics {
		POINT((byte) 0x0B), POLYLINE((byte) 0x0D), AREA((byte) 0x0C), LABELS((byte) 0x0F), POI(
				(byte) 0x10);
		final byte type;

		private Graphics(final byte type) {
			this.type = type;
		}

		/**
		 * @return the type
		 */
		public byte getType() {
			return type;
		}

	};

	public enum OsmType {
		HIGHWAY(Graphics.POLYLINE), NATURAL(Graphics.AREA), LANDUSE(Graphics.AREA), RAILWAY(
				Graphics.POLYLINE), BUILDING(Graphics.AREA), LEISURE(Graphics.AREA), WATERWAY(
				Graphics.POLYLINE), AMENITY(Graphics.AREA);
		protected final Graphics graphics;

		private OsmType(final Graphics graphics) {
			this.graphics = graphics;
		}

		/**
		 * @return the graphics
		 */
		public Graphics getGraphics() {
			return graphics;
		}

	};

	public static enum LayerType {
		HIGHWAY(Graphics.POLYLINE), PRIMARY(Graphics.POLYLINE), SECNDARY(Graphics.POLYLINE), TERTIARY(
				Graphics.POLYLINE), RESIDENTIAL(Graphics.POLYLINE), TRACK(Graphics.POLYLINE), FOOTWAY(
				Graphics.POLYLINE), PATH(Graphics.POLYLINE), CYCLEWAY(Graphics.POLYLINE), RESIDAREA(
				Graphics.AREA), SCRUB(Graphics.AREA), WATER(Graphics.AREA), RAILWAY(
				Graphics.POLYLINE), FOREST(Graphics.AREA), PITCH(Graphics.AREA), WATERWAY(
				Graphics.POLYLINE), PARKING(Graphics.AREA), BUILDING(Graphics.AREA), POINTS(
				Graphics.POINT), UNKNOWN(Graphics.AREA);
		protected final Graphics graphics;

		private LayerType(final Graphics graphics) {
			this.graphics = graphics;
		}

		/**
		 * @return the graphics
		 */
		public Graphics getGraphics() {
			return graphics;
		}
	};

	// 0 HIGHWAY
	// 1 PRIMARY
	// 2 SECONDARY,
	// 3 RESIDENTIAL
	// 4 TRACK
	// 5 Footway
	// 6 PATH
	// 7 cycleway
	// 8 scrub / weide
	// 9 Water
	// 10 RAILWAY
	// 11 FOREST
	// 12 Pitch
	// Layer.WATERWAY Waterway
	// 14 parking

	public enum WayType {
		MOTORWAY(
		// HIGHWAY
				OsmType.HIGHWAY, LayerType.HIGHWAY, "motorway"), TRUNK(OsmType.HIGHWAY,
				LayerType.HIGHWAY, "trunk"), PRIMARY(OsmType.HIGHWAY, LayerType.PRIMARY, "primary"), SECONDARY(
				OsmType.HIGHWAY, LayerType.SECNDARY, "secondary"), TERTIARY(OsmType.HIGHWAY,
				LayerType.TERTIARY, "tertiary"), UNCLASSIFIED(OsmType.HIGHWAY,
				LayerType.RESIDENTIAL, "unclassified"), RESIDENTIAL(OsmType.HIGHWAY,
				LayerType.RESIDENTIAL, "residential"), SERVICE(OsmType.HIGHWAY,
				LayerType.RESIDENTIAL, "service"), MOTORWAY_LINK(OsmType.HIGHWAY,
				LayerType.HIGHWAY, "motorway_link"), TRUNC_LINK(OsmType.HIGHWAY, LayerType.HIGHWAY,
				"trunk_link"), PRIMARY_LINK(OsmType.HIGHWAY, LayerType.PRIMARY, "primary_link"), SECONDARY_LINK(
				OsmType.HIGHWAY, LayerType.SECNDARY, "secondary_link"), TERTIARY_LINK(
				OsmType.HIGHWAY, LayerType.SECNDARY, "tertriary_link"), LIVING_STREET(
				OsmType.HIGHWAY, LayerType.RESIDENTIAL, "living_street"), PEDESTRIAN(
				OsmType.HIGHWAY, LayerType.RESIDENTIAL, "pedestrian"), TRACK(OsmType.HIGHWAY,
				LayerType.TRACK, "track"), BUS_GUIDEWAY(OsmType.HIGHWAY, LayerType.UNKNOWN,
				"bus_guideway"), RACEWAY(OsmType.HIGHWAY, LayerType.HIGHWAY, "raceway"), ROAD(
				OsmType.HIGHWAY, LayerType.RESIDENTIAL, "road"), FOOTWAY(OsmType.HIGHWAY,
				LayerType.FOOTWAY, "footway"), BRIDLEWAY(OsmType.HIGHWAY, LayerType.UNKNOWN,
				"bridleway"), STEPS(OsmType.HIGHWAY, LayerType.FOOTWAY, "steps"), PATH(
				OsmType.HIGHWAY, LayerType.PATH, "path"), CYCLEWAY(OsmType.HIGHWAY,
				LayerType.CYCLEWAY, "cycleway"), PROPOSED(OsmType.HIGHWAY, LayerType.UNKNOWN,
				"proposed"), CONSTRUCTION(OsmType.HIGHWAY, LayerType.UNKNOWN, "construction"),
		// RAILWAY
		RAILWAY(OsmType.RAILWAY, LayerType.RAILWAY, "rail"),
		// NATURAL
		SCRUB(OsmType.NATURAL, LayerType.SCRUB, "scrub"), GRASSLAND(OsmType.NATURAL,
				LayerType.SCRUB, "grassland"), WATER(OsmType.NATURAL, LayerType.WATER, "water"), WATERCOAST(
				OsmType.NATURAL, LayerType.WATERWAY, "coastline"), WOOD2(OsmType.NATURAL,
				LayerType.FOREST, "wood"),
		// Building
		BUILDING(OsmType.BUILDING, LayerType.BUILDING, "yes"), APARTMENTS(OsmType.BUILDING,
				LayerType.BUILDING, "apartments"), HOUSE(OsmType.BUILDING, LayerType.BUILDING,
				"house"), GARAGE(OsmType.BUILDING, LayerType.BUILDING, "garage"), GARAGES(
				OsmType.BUILDING, LayerType.BUILDING, "garages"), ROOF(OsmType.BUILDING,
				LayerType.BUILDING, "roof"), SHED(OsmType.BUILDING, LayerType.BUILDING, "shed"), HANGAR(
				OsmType.BUILDING, LayerType.BUILDING, "hangar"), INDUSTRIAL(OsmType.BUILDING,
				LayerType.BUILDING, "industrial"), PUBLIC(OsmType.BUILDING, LayerType.BUILDING,
				"public"), CHURCH(OsmType.BUILDING, LayerType.BUILDING, "church"), CHAPEL(
				OsmType.BUILDING, LayerType.BUILDING, "chapel"), SCHOOL_BUILDING(OsmType.BUILDING,
				LayerType.BUILDING, "school"), GREENHOUSE(OsmType.BUILDING, LayerType.BUILDING,
				"greenhouse"), RESIDENTIAL_BUILDING(OsmType.BUILDING, LayerType.BUILDING,
				"residential"), COMMERCIAL(OsmType.BUILDING, LayerType.BUILDING, "commercial"), TERRACE(
				OsmType.BUILDING, LayerType.BUILDING, "terrace"),
		// Landuse
		FOREST(OsmType.LANDUSE, LayerType.FOREST, "forest"), WOOD(OsmType.LANDUSE,
				LayerType.FOREST, "WOOD"), FARMLAND(OsmType.LANDUSE, LayerType.SCRUB, "farmland"), GRASS(
				OsmType.LANDUSE, LayerType.SCRUB, "grass"), CEMETERY(OsmType.LANDUSE,
				LayerType.SCRUB, "cemetery"), MEADOW(OsmType.LANDUSE, LayerType.SCRUB, "meadow"), RECREATION_GROUD(
				OsmType.LANDUSE, LayerType.SCRUB, "recreation_ground"), RESIDENTIAL_LANDUSE(
				OsmType.LANDUSE, LayerType.RESIDAREA, "residential"), VILLAGE_GREEN(
				OsmType.LANDUSE, LayerType.SCRUB, "village_green"), GARAGES_AREA(OsmType.LANDUSE,
				LayerType.PARKING, "garages"), RESERVOIR(OsmType.LANDUSE, LayerType.WATER,
				"reservoir"), BASIN(OsmType.LANDUSE, LayerType.WATER, "basin"), ALLOTMENTS(
				OsmType.LANDUSE, LayerType.PITCH, "allotments"), ORCHARD(OsmType.LANDUSE,
				LayerType.SCRUB, "orchard"), INDUSTRIAL_AREA(OsmType.LANDUSE, LayerType.RESIDAREA,
				"industrial"), COMMERCIAL_AREA(OsmType.LANDUSE, LayerType.RESIDAREA, "commercial"),
		// Leisure
		PITCH(OsmType.LEISURE, LayerType.PITCH, "pitch"), STADIUM(OsmType.LEISURE, LayerType.PITCH,
				"stadium"), SWIMMING_POOL(OsmType.LEISURE, LayerType.WATER, "swimming_pool"), PLAY_GROUND(
				OsmType.LEISURE, LayerType.PITCH, "playground"), GARDEN(OsmType.LEISURE,
				LayerType.PITCH, "garden"), PARK(OsmType.LEISURE, LayerType.PITCH, "park"),
		// Waterway
		STREAM(OsmType.WATERWAY, LayerType.WATERWAY, "stream"), RIVER(OsmType.WATERWAY,
				LayerType.WATERWAY, "river"), CANAl(OsmType.WATERWAY, LayerType.WATERWAY, "canal"), RIVERBANK(
				OsmType.WATERWAY, LayerType.WATER, "riverbank"), DRAIN(OsmType.WATERWAY,
				LayerType.WATERWAY, "drain"), DITCH(OsmType.WATERWAY, LayerType.WATERWAY, "ditch"),
		// Amenity
		PARKING(OsmType.AMENITY, LayerType.PARKING, "parking"), SCHOOL(OsmType.AMENITY,
				LayerType.UNKNOWN, "school"), UNIVERSITY(OsmType.AMENITY, LayerType.UNKNOWN,
				"university"), COLLEGE(OsmType.AMENITY, LayerType.UNKNOWN, "college");

		protected final LayerType layer;
		protected final String value;
		protected final OsmType osmType;

		private WayType(final OsmType osmType, final LayerType layer, final String value) {
			this.osmType = osmType;
			this.layer = layer;
			this.value = value;
		}

		/**
		 * @return the layer
		 */
		public LayerType getLayer() {
			return layer;
		}

		/**
		 * @return the highway
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @return the osmType
		 */
		public OsmType getOsmType() {
			return osmType;
		}
	}

	public WayType getWayType(final Map<String, String> props) {
		WayType ret = null;
		final String highway = props.get("highway");
		final String natural = props.get("natural");
		final String railway = props.get("railway");
		final String building = props.get("building");
		final String landuse = props.get("landuse");
		final String leisure = props.get("leisure");
		final String waterway = props.get("waterway");
		final String amenity = props.get("amenity");
		final WayType types[] = WayType.values();
		for (int i = 0; i < types.length; i++) {
			final WayType wayType = types[i];
			if (OsmType.HIGHWAY.equals(wayType.getOsmType()) && wayType.getValue().equals(highway)) {
				ret = wayType;
			} else if (OsmType.NATURAL.equals(wayType.getOsmType())
					&& wayType.getValue().equals(natural)) {
				ret = wayType;
			} else if (OsmType.RAILWAY.equals(wayType.getOsmType())
					&& wayType.getValue().equals(railway)) {
				ret = wayType;
			} else if (OsmType.BUILDING.equals(wayType.getOsmType())
					&& wayType.getValue().equals(building)) {
				ret = wayType;
			} else if (OsmType.LANDUSE.equals(wayType.getOsmType())
					&& wayType.getValue().equals(landuse)) {
				ret = wayType;
			} else if (OsmType.LEISURE.equals(wayType.getOsmType())
					&& wayType.getValue().equals(leisure)) {
				ret = wayType;
			} else if (OsmType.WATERWAY.equals(wayType.getOsmType())
					&& wayType.getValue().equals(waterway)) {
				ret = wayType;
			} else if (OsmType.AMENITY.equals(wayType.getOsmType())
					&& wayType.getValue().equals(amenity)) {
				ret = wayType;
			}
		}
		// if (ret == null && way.getProperty("barrier") == null) {
		// System.out.println("Unclassified way : " + way.getId() + " props : "
		// + way.toJSON().optJSONArray("props").toString());
		// }
		return ret;
	}
}

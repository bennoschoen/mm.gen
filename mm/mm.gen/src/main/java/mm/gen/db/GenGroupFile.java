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
 * @since 16.05.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.db;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mm.db.DataBase;
import mm.db.Table;
import mm.db.Table.FieldType;
import mm.gen.config.Group;
import mm.gen.config.LayerConf;
import mm.gen.config.MapConfig;
import mm.gen.db.WayClassifier.LayerType;
import mm.gen.db.WayClassifier.WayType;
import mm.gen.osm.MultiWay;
import mm.gen.osm.Node;
import mm.gen.osm.Way;
import mm.io.StreamableFactory;
import mm.io.StreamableIterator;
import mm.io.StreamableSorter;
import mm.io.StreamableWriter;
import mm.lay.LatLng;
import mm.lay.Layer;
import mm.lay.Point;
import mm.lay.RelCoordConverter;
import mm.lay.types.Carrier;
import mm.lay.types.PolyArea;
import mm.lay.types.PolyLine;
import mm.lay.types.SubPolyArea;
import mm.text.TextDbGenerator;

/**
 * @author benno
 * 
 */
public class GenGroupFile {
	protected final MapConfig mapConfig;
	protected final File wayFile;
	protected final File groupFolder;
	protected final RelCoordConverter coordConverter;
	protected final BoundingBox boundingBox;
	protected final File targetFolder;
	protected final File groupFiles[];
	protected final MultiWayShredder areaShredder;
	protected final WayShredder wayShredder;

	public GenGroupFile(final MapConfig mapConfig, final File wayFile, final File groupFolder,
			final File targetFolder, final RelCoordConverter coordConverter) {
		super();
		final double maxWidth = getMaxAllowedExtend(Math.abs(coordConverter.getMaxX()
				- coordConverter.getMinX()))
				* coordConverter.getScaleX();
		final double maxHeight = getMaxAllowedExtend(Math.abs(coordConverter.getMaxY()
				- coordConverter.getMinY()))
				* coordConverter.getScaleY();
		System.out
				.println("MaxX extend "
						+ getMaxAllowedExtend(Math.abs(coordConverter.getMaxX()
								- coordConverter.getMinX())));
		System.out
				.println("MaxY extend "
						+ getMaxAllowedExtend(Math.abs(coordConverter.getMaxY()
								- coordConverter.getMinY())));
		final Group groups[] = mapConfig.getGroups();
		this.mapConfig = mapConfig;
		this.wayFile = wayFile;
		this.groupFolder = groupFolder;
		this.targetFolder = targetFolder;
		this.coordConverter = coordConverter;
		boundingBox = coordConverter.getBoundingBox();
		groupFiles = new File[groups.length];
		areaShredder = new MultiWayShredder(boundingBox, maxWidth, maxHeight);
		wayShredder = new WayShredder(false, boundingBox, maxWidth, maxHeight);

		for (int i = 0; i < groups.length; i++) {
			groupFiles[i] = new File(groupFolder, groups[i].getName() + ".ser");
		}
	}

	protected long getMaxAllowedExtend(final long totalExtend) {
		long ret = totalExtend;
		while (ret > 65535) {
			ret /= 2;
		}
		return ret / 2 - 1;
	}

	protected List<Point> getPoints(final List<Node> node) {
		final List<Point> ret = new ArrayList<>();
		Point lastPoint = null;
		for (final Node co : node) {
			final long nextLon = Math.min(coordConverter.getLonRel(co.getLon()),
					coordConverter.getMaxX() - 1);
			final long nextLat = Math.min(coordConverter.getLatRel(co.getLat()),
					coordConverter.getMaxY() - 1);
			final Point nextPoint = new Point(nextLon, nextLat);
			if (lastPoint != null) {
				ret.addAll(Point.interMediatePoint(lastPoint, nextPoint, 126));
			}
			if (!nextPoint.equals(lastPoint)) {
				ret.add(nextPoint);
				lastPoint = nextPoint;
			}
		}
		return ret;
	}

	protected void writePolyLine(final StreamableWriter<Carrier> os, final MultiWay way,
			final LayerType layerType) throws UnsupportedEncodingException, IOException {
		final List<Way> ways = wayShredder.shredderWay(way);
		final String name = way.getProperty("name", "");

		for (final Way way2 : ways) {
			final List<Point> points = getPoints(way2.getNodes());
			if (points.size() > 1) {
				final PolyLine line = new PolyLine(name, points);
				final Point topRight = line.getTopRight();
				final Point bottomLeft = line.getBottomLeft();
				final int cell = coordConverter.getCellId(bottomLeft.getX(), bottomLeft.getY(),
						topRight.getX(), topRight.getY());
				line.setCellId(cell);
				line.setGraphics(layerType.getGraphics());
				line.setObjectType((byte) mapConfig.getLayerNrOfLayer(layerType));
				line.setLayer(layerType);
				if (!points.isEmpty() && (cell > 0)) {
					final Carrier carrier = new Carrier(line);
					os.writeStreamable(carrier);
				}
			}
		}
	}

	protected PolyArea createPolyArea(final MultiWay way2, final String name,
			final LayerType layerType) {
		final PolyArea polyArea = new PolyArea();
		polyArea.setName(name);
		for (final Way outerWay : way2.getOuterWays()) {
			final List<Point> points = getPoints(outerWay.getNodes());
			if (points.size() > 1) {
				final SubPolyArea subArea = new SubPolyArea(name, points);
				polyArea.addOuterArea(subArea);
			}
		}
		for (final Way innerWay : way2.getInnerWays()) {
			final List<Point> points = getPoints(innerWay.getNodes());
			if (points.size() > 1) {
				final SubPolyArea subArea = new SubPolyArea(name, points);
				polyArea.addInnerArea(subArea);
			}
		}
		final Point topRight = polyArea.getTopRight();
		final Point bottomLeft = polyArea.getBottomLeft();
		final int cell = coordConverter.getCellId(bottomLeft.getX(), bottomLeft.getY(),
				topRight.getX(), topRight.getY());
		polyArea.setCellId(cell);
		polyArea.setGraphics(layerType.getGraphics());
		polyArea.setObjectType((byte) mapConfig.getLayerNrOfLayer(layerType));
		polyArea.setLayer(layerType);
		return polyArea;
	}

	protected void writePolyArea(final StreamableWriter<Carrier> os, final MultiWay way,
			final LayerType layerType) throws UnsupportedEncodingException, IOException {
		final List<MultiWay> ways = areaShredder.shredderWay(way);
		final String name = way.getProperty("name", "");

		while (ways.size() > 0 && ways.size() < 200) {
			final MultiWay multiWay = ways.remove(0);
			final PolyArea polyArea = createPolyArea(multiWay, name, layerType);
			if (polyArea.getCellId() > 0) {
				if (polyArea.getPointsCount() <= 16200) {
					final Carrier carrier = new Carrier(polyArea);
					os.writeStreamable(carrier);
				} else {
					final LatLng bottomLeft = multiWay.getBottomLeft();
					final LatLng topRight = multiWay.getTopRight();
					final double width = topRight.getLon() - bottomLeft.getLon();
					final double height = topRight.getLat() - bottomLeft.getLat();
					if (width > height) {
						ways.addAll(areaShredder.splitAtLongitude(multiWay, bottomLeft.getLon()
								+ width / 2));
					} else {
						ways.addAll(areaShredder.splitAtLatitude(multiWay, bottomLeft.getLat()
								+ height / 2));
					}
				}
			}
		}
	}

	public void genGroup() {
		final WayClassifier classifier = new WayClassifier();
		try {
			final StreamableIterator<MultiWay> iterator = new StreamableIterator<>(wayFile,
					new StreamableFactory<MultiWay>() {
						@Override
						public MultiWay construct() {
							return new MultiWay();
						}
					});
			final List<StreamableWriter<Carrier>> writers = new ArrayList<StreamableWriter<Carrier>>();
			for (int i = 0; i < groupFiles.length; ++i) {
				writers.add(new StreamableWriter<Carrier>(groupFiles[i]));
			}

			while (iterator.hasNext()) {
				final MultiWay way = iterator.next();
				final WayType type = classifier.getWayType(way.getProps());
				int group = -1;
				LayerType layerType = LayerType.UNKNOWN;
				if (type != null) {
					layerType = type.getLayer();
					group = mapConfig.getGroupOfLayer(layerType);
				}
				if (group >= 0) {
					switch (layerType.getGraphics()) {
					case AREA:
						writePolyArea(writers.get(group), way, layerType);
						break;
					case POINT:
						break;
					case POLYLINE:
						writePolyLine(writers.get(group), way, layerType);
						break;
					default:
						break;
					}
				}
			}
			for (final StreamableWriter<Carrier> streamableWriter : writers) {
				streamableWriter.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void sortCells() {

		for (final File file : groupFiles) {
			try {
				final StreamableSorter<Carrier> sorter = new StreamableSorter<>(file, groupFolder,
						new StreamableFactory<Carrier>() {

							@Override
							public Carrier construct() {
								return new Carrier();
							}
						}, new Comparator<Carrier>() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see
							 * java.util.Comparator#compare(java.lang.Object,
							 * java.lang.Object)
							 */
							@Override
							public final int compare(final Carrier o1, final Carrier o2) {
								int ret;
								if (o1 == null && o2 == null) {
									return 0;
								} else if (o1 == null) {
									return -1;
								} else if (o2 == null) {
									return 1;
								}
								ret = Integer.valueOf(o1.getCellId()).compareTo(
										Integer.valueOf(o2.getCellId()));
								if (ret == 0) {
									ret = Integer.valueOf(o1.getObjectType()).compareTo(
											Integer.valueOf(o2.getObjectType()));
									if (ret == 0) {
										ret = Integer.valueOf(o1.getLineId()).compareTo(
												Integer.valueOf(o2.getLineId()));

									}
								}
								return ret;
							}
						});
				sorter.process();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setNumberInCell() {
		for (final File file : groupFiles) {
			try {
				final File temp = new File(file.getCanonicalPath() + ".temp");
				file.renameTo(temp);
				final StreamableWriter<Carrier> writer = new StreamableWriter<>(file);
				final StreamableIterator<Carrier> iter = new StreamableIterator<>(temp,
						new StreamableFactory<Carrier>() {
							@Override
							public Carrier construct() {
								return new Carrier();
							}
						});
				int lastCell = -1;
				int lastObjectType = -1;
				int numberInCell = 0;
				while (iter.hasNext()) {
					final Carrier carrier = iter.next();
					if (!(carrier.getObjectType() == lastObjectType && carrier.getCellId() == lastCell)) {
						numberInCell = 0;
						lastObjectType = carrier.getObjectType();
						lastCell = carrier.getCellId();
					}
					carrier.setNumberInCell(numberInCell);
					numberInCell++;
					if (numberInCell < 65534) {
						writer.writeStreamable(carrier);
					} else {
						System.out.println("Drop Element : " + carrier.toJSON().toString());
					}
				}
				writer.close();
				temp.delete();
			} catch (final IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void sort() {
		for (final File file : groupFiles) {
			try {
				final StreamableSorter<Carrier> sorter = new StreamableSorter<>(file, groupFolder,
						new StreamableFactory<Carrier>() {

							@Override
							public Carrier construct() {
								return new Carrier();
							}

						}, new Comparator<Carrier>() {
							/*
							 * (non-Javadoc)
							 * 
							 * @see
							 * java.util.Comparator#compare(java.lang.Object,
							 * java.lang.Object)
							 */
							@Override
							public final int compare(final Carrier o1, final Carrier o2) {
								int ret;
								if (o1 == null && o2 == null) {
									return 0;
								} else if (o1 == null) {
									return -1;
								} else if (o2 == null) {
									return 1;
								}

								ret = o1.getName().compareTo(o2.getName());
								if (ret == 0) {
									if (o1.getLineId() < o2.getLineId()) {
										ret = -1;
									} else if (o1.getLineId() > o2.getLineId()) {
										ret = 1;
									} else {
										ret = 0;
									}
								}
								return ret;
							}
						});
				sorter.process();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void genTextDb() throws IOException {
		final int nrOfGroups = mapConfig.getGroups().length;
		final DataBase db = new DataBase(new File(targetFolder, "db00.dbd"));
		final File cityTableRef = new File(targetFolder, "00z.dat");
		final File cityNameRef = new File(targetFolder, "00cn.dat");

		final Table cityTable = new Table(cityTableRef, "Z_R", new FieldType[] { FieldType.SHORT,
				FieldType.INTEGER, FieldType.INTEGER, FieldType.INTEGER }, new String[] {
				"ZIP_CODE", "C_REF" }, 4);
		final Table cityName = new Table(cityNameRef, "C_R", new FieldType[] { FieldType.SHORT,
				FieldType.INTEGER, FieldType.STRING }, new String[] { "CITY_BUF" }, 4);
		db.addTable(cityTable);
		db.addTable(cityName);

		final TextDbGenerator textDbA[] = new TextDbGenerator[nrOfGroups];
		for (int i = 0; i < nrOfGroups; ++i) {
			textDbA[i] = new TextDbGenerator(targetFolder, db, i);
		}
		for (int i = 0; i < nrOfGroups; ++i) {
			final GenGroupTextDB genGroupTextDB = new GenGroupTextDB(textDbA[i], groupFiles[i]);
			genGroupTextDB.genTextDb();
			textDbA[i].finish();
		}
		cityTable.close();
		cityName.close();
		db.close();
	}

	public void genLayers() {
		try {
			final Group groups[] = mapConfig.getGroups();
			for (int i = 0; i < groups.length; i++) {
				final Group group = groups[i];
				final LayerConf[] layerConfs = group.getLayers();
				final Layer layers[] = new Layer[layerConfs.length];
				for (int j = 0; j < layers.length; j++) {
					final LayerConf layerConf = layerConfs[j];
					final File layerFile = new File(targetFolder, layerConfs[j].getFileName()
							+ ".lay");
					layers[j] = new Layer(layerFile, coordConverter, layerConf.getLayerType()
							.getGraphics(), (byte) layerConf.getLayerNr());
				}
				final StreamableIterator<Carrier> iter = new StreamableIterator<>(groupFiles[i],
						new StreamableFactory<Carrier>() {
							@Override
							public Carrier construct() {
								return new Carrier();
							}

						});
				while (iter.hasNext()) {
					final Carrier carrier = iter.next();
					if (carrier.getObjectType() < layers.length) {
						layers[carrier.getObjectType()].addCarrier(carrier);
					}
				}
				for (int j = 0; j < layers.length; j++) {
					layers[j].finish();
				}

			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}

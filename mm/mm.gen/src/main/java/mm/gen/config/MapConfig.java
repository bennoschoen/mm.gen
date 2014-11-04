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
package mm.gen.config;

import java.util.HashMap;

import mm.gen.db.WayClassifier.LayerType;

/**
 * @author benno
 * 
 */
public class MapConfig {
	protected final String name;
	protected final Group groups[];
	protected HashMap<LayerType, LayerConf> layerMap = new HashMap<>();

	public MapConfig(final String name, final Group[] groups) {
		super();
		this.name = name;
		this.groups = groups;
		for (int i = 0; i < groups.length; i++) {
			groups[i].setGroupNr(i);
			for (final LayerConf layer : groups[i].getLayers()) {
				layerMap.put(layer.getLayerType(), layer);
			}
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the layers
	 */
	public Group[] getGroups() {
		return groups;
	}

	public LayerConf getLayerConf(final LayerType layerType) {
		return layerMap.get(layerType);
	}

	public int getGroupOfLayer(final LayerType layerType) {
		int ret = -1;
		final LayerConf conf = getLayerConf(layerType);
		if (conf != null) {
			ret = conf.getGroupNr();
		}
		return ret;
	}

	public int getLayerNrOfLayer(final LayerType layerType) {
		int ret = -1;
		final LayerConf conf = getLayerConf(layerType);
		if (conf != null) {
			ret = conf.getLayerNr();
		}
		return ret;
	}

	public String getFileNameOfLayer(final LayerType layerType) {
		String ret = null;
		final LayerConf conf = getLayerConf(layerType);
		if (conf != null) {
			ret = conf.getFileName();
		}
		return ret;
	}
}

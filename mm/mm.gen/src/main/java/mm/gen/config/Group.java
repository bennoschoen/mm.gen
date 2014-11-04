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
 * @since 06.04.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.config;

/**
 * @author benno
 * 
 */
public class Group {
	protected int groupNr = -1;
	protected final String name;
	protected final LayerConf layers[];

	public Group(final String name, final LayerConf[] layers) {
		super();
		this.name = name;
		this.layers = layers;
		for (int i = 0; i < layers.length; i++) {
			layers[i].setLayerNr(i);
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
	public LayerConf[] getLayers() {
		return layers;
	}

	/**
	 * @return the groupNr
	 */
	public int getGroupNr() {
		return groupNr;
	}

	/**
	 * @param groupNr
	 *            the groupNr to set
	 */
	void setGroupNr(final int groupNr) {
		this.groupNr = groupNr;
		for (final LayerConf layerConf : layers) {
			layerConf.setGroupNr(groupNr);
		}
	}
}

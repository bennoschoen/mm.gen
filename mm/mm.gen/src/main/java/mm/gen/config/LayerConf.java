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

import mm.gen.db.WayClassifier.LayerType;

public class LayerConf {
	protected int groupNr = -1;
	protected int layerNr = -1;
	protected final LayerType layerType;
	protected final String fileName;

	public LayerConf(final LayerType layerType, final String fileName) {
		super();
		this.layerType = layerType;
		this.fileName = fileName;
	}

	/**
	 * @return the groupNr
	 */
	public int getGroupNr() {
		return groupNr;
	}

	/**
	 * @return the layerType
	 */
	public LayerType getLayerType() {
		return layerType;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the groupNr
	 * 
	 * @param groupNr
	 */
	void setGroupNr(final int groupNr) {
		this.groupNr = groupNr;
	}

	/**
	 * @return the layerNr
	 */
	public int getLayerNr() {
		return layerNr;
	}

	/**
	 * @param layerNr
	 *            the layerNr to set
	 */
	void setLayerNr(final int layerNr) {
		this.layerNr = layerNr;
	}
}
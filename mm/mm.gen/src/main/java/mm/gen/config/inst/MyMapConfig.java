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
package mm.gen.config.inst;

import mm.gen.config.Group;
import mm.gen.config.LayerConf;
import mm.gen.config.MapConfig;
import mm.gen.db.WayClassifier.LayerType;

/**
 * @author benno
 * 
 */
public class MyMapConfig extends MapConfig {

	/**
	 * @param name
	 * @param groups
	 */
	public MyMapConfig() {
		super("niedersachsen", getGroupConfs());
	}

	private static Group[] getGroupConfs() {
		return new Group[] {
		/**
		 * Group 0
		 */
		new Group("gr0", new LayerConf[] { new LayerConf(LayerType.HIGHWAY, "00fwy"), // 0
				new LayerConf(LayerType.PRIMARY, "00mj_hwy"), // 1
				new LayerConf(LayerType.TERTIARY, "00mn_hwy"), // 2
				new LayerConf(LayerType.SECNDARY, "00maj"), // 3
				new LayerConf(LayerType.RESIDENTIAL, "00str"), // 4
		}),
		/**
		 * Group 1
		 */
		new Group("gr1", new LayerConf[] { new LayerConf(LayerType.RAILWAY, "00railrd"), // 13
		}),
		/**
		 * Group 2
		 */
		new Group("gr2", new LayerConf[] { new LayerConf(LayerType.WATER, "00hydp"), // 10
				new LayerConf(LayerType.WATERWAY, "00hydl"), // 11
		}),
		/**
		 * Group 3
		 */
		new Group("gr3", new LayerConf[] { new LayerConf(LayerType.PITCH, "00park"), // 7
				new LayerConf(LayerType.FOREST, "00frst"), // 12
		}),
		/**
		 * Group 4
		 */
		new Group("gr4", new LayerConf[] { new LayerConf(LayerType.PATH, "00trail"), // 6
				new LayerConf(LayerType.TRACK, "00trail4"), // 5
				new LayerConf(LayerType.FOOTWAY, "00foot"), // 6
				new LayerConf(LayerType.CYCLEWAY, "00cycle") // 8
				}),
		/**
		 * Group 5
		 */
		new Group("gr5", new LayerConf[] { new LayerConf(LayerType.BUILDING, "00build"), // 8
				new LayerConf(LayerType.PARKING, "00prkp"), // 15
		}),
		/**
		 * Group 6
		 */
		new Group("gr6", new LayerConf[] { new LayerConf(LayerType.SCRUB, "00farm"), // 9
				new LayerConf(LayerType.RESIDAREA, "00rsare"), // 9
		}),
		/**
		 * Group 7
		 */
		new Group("gr7", new LayerConf[] { new LayerConf(LayerType.POINTS, "00points"), // 12
		}),

		};
	}

}

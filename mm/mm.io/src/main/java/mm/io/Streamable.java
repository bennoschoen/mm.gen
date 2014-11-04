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
 * @since 01.07.2014
 * @version 1.0
 * @author benno
 */
package mm.io;

import java.io.Externalizable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that can be either used to store Objects as single line json Strings or
 * in serialized form
 * 
 * @author benno
 */
public interface Streamable extends Externalizable {

	/**
	 * This flag is checked by any of the streaming class from this package.
	 * When flag is set to true, it is expected that the json methods shall be
	 * called and that all files where Streamables are read from are in json
	 * format
	 */
	public static boolean jsonSupport = false;

	/**
	 * Converts this Streamable to a JSON form
	 * 
	 * @return
	 * @throws JSONException
	 */
	JSONObject toJSON() throws JSONException;

	/**
	 * reads the object state from the json in the given string
	 * 
	 * @param json
	 *            JSON representation of the object state
	 * @throws JSONException
	 */
	void fromJSON(final String json) throws JSONException;

	/**
	 * reads the object state from the json in the given JSONObject
	 * 
	 * @param json
	 *            JSON representation of the object state
	 * @throws JSONException
	 */
	void fromJSON(final JSONObject json) throws JSONException;
}

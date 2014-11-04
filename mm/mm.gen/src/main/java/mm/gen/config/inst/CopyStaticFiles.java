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
 * @since 21.06.2014
 * @version 1.0
 * @author benno
 */
package mm.gen.config.inst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mm.lay.RelCoordConverter;

/**
 * @author benno
 * 
 */
public class CopyStaticFiles {

	protected void copyMapIni(final File destFolder, final RelCoordConverter converter,
			final String name) throws IOException {
		final StringBuilder builder = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				CopyStaticFiles.class.getResourceAsStream("00map.ini"), "ISO-8859-1"));
		String line = reader.readLine();
		while (line != null) {
			builder.append(line);
			builder.append("\r\n");
			line = reader.readLine();
		}
		reader.close();
		String iniContent = builder.toString();
		iniContent = iniContent.replace("[LON_MIN]",
				String.format(Locale.US, "%.5f", converter.getMinLon()));
		iniContent = iniContent.replace("[LON_MAX]",
				String.format(Locale.US, "%.5f", converter.getMaxLon()));
		iniContent = iniContent.replace("[LAT_MIN]",
				String.format(Locale.US, "%.5f", converter.getMinLat()));
		iniContent = iniContent.replace("[LAT_MAX]",
				String.format(Locale.US, "%.5f", converter.getMaxLat()));
		iniContent = iniContent.replace("[NAME]", name);
		final FileOutputStream fos = new FileOutputStream(new File(destFolder, "00map.ini"));
		fos.write(iniContent.getBytes("ISO-8859-1"));
		fos.close();
	}

	protected void copyFile(final InputStream fis, final File dest) throws IOException {
		final FileOutputStream fos = new FileOutputStream(dest);
		final byte b[] = new byte[1000];
		int got = fis.read(b);
		while (got > 0) {
			fos.write(b, 0, got);
			got = fis.read(b);
		}
		fis.close();
		fos.close();
	}

	protected void genCVG_MAP_MSF(final File file, final String name) throws IOException {
		final StringBuilder builder = new StringBuilder();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		final SimpleDateFormat versionFormat = new SimpleDateFormat("yy.MM.dd");
		builder.append("IMG_NAME = " + name);
		builder.append("\r\n");
		builder.append("PRODUCT = yamap");
		builder.append("\r\n");
		builder.append("PROVIDER = mm.gen");
		builder.append("\r\n");
		builder.append("IMG_DATE = " + dateFormat.format(new Date()));
		builder.append("\r\n");
		builder.append("IMG_VERSION = " + versionFormat.format(new Date()));
		builder.append("\r\n");
		builder.append("Version = " + versionFormat.format(new Date()));
		builder.append("\r\n");
		builder.append("VENDOR_ID = -1");
		builder.append("\r\n");
		builder.append("REGION_ID = -1");
		builder.append("\r\n");
		builder.append("ADDITIONAL_COMMENTS = Contains information from OpenStreetMap, which is made available here under the Open Database Licence (ODbL). The map is under CC-BY-SA 3.0. The map comes without warranty.");
		builder.append("\r\n");
		final FileWriter fos = new FileWriter(file);
		fos.write(builder.toString());
		fos.close();
	}

	public void copyStaticFiles(final File destFolder, final RelCoordConverter converter,
			final String name) {
		try {
			copyMapIni(destFolder, converter, name);
			copyFile(CopyStaticFiles.class.getResourceAsStream("add_maps.cfg"), new File(
					destFolder, "add_maps.cfg"));
			copyFile(CopyStaticFiles.class.getResourceAsStream("bmp2bit.ics"), new File(destFolder,
					"bmp2bit.ics"));
			copyFile(CopyStaticFiles.class.getResourceAsStream("bmp4bit.ics"), new File(destFolder,
					"bmp4bit.ics"));
			genCVG_MAP_MSF(new File(destFolder, "cvg_map.msf"), name);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}

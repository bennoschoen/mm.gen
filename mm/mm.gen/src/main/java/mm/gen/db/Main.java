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

import java.io.File;
import java.io.IOException;

import mm.gen.config.MapConfig;
import mm.gen.config.inst.CopyStaticFiles;
import mm.gen.config.inst.MyMapConfig;
import mm.gen.imi.ImiGenerator;
import mm.lay.RelCoordConverter;
import mm.tile.gen.Generator;

/**
 * @author benno
 * 
 */
public class Main {

	final static double scale = 9.0e-6;
	static double minLat = 0.0;
	static double minLon = 0.0;
	static double maxLat = 0.0;
	static double maxLon = 0.0;		
	static File outputFile = null;
	static File inputFile = null;
	static File tempFile = null;

	
	protected static RelCoordConverter getConverter(){
		final RelCoordConverter converter;
		long cellWidth = 1;
		long cellHeight = 1;
		int zoom = 1;
		long extent = 3800;
		final double centerLat = (minLat + maxLat) / 2;
		final double centerLon = (minLon + maxLon) / 2;
		
		while (centerLat + extent * scale < maxLat || centerLon + extent * scale < maxLon){
			zoom++;
			extent *= 2;
		}
		cellWidth = (long)(0.5 + (maxLon - centerLon) / (scale * (1 << (zoom - 1))));
		cellHeight = (long)(0.5 + (maxLat - centerLat) / (scale * (1 << (zoom - 1))));
		cellWidth += cellWidth % 2;
		cellHeight += cellHeight % 2;
		converter = new RelCoordConverter(centerLon, -centerLat,
				cellWidth, cellHeight, scale, scale, zoom);
		return converter;
	}
	
	public static Generator generateRawFiles() throws IOException{
		Generator generator = new Generator(tempFile);
		generator.readFile(inputFile);
		return generator;
	}
	
	
	protected static void genMap(final RelCoordConverter converter, final File rawWayFile, final File rawRelationFile) {
		final long startTime = System.nanoTime();
		try {
			final MapConfig mapConfig = new MyMapConfig();
			final File targetfolder = new File(tempFile, "map");
			final File multiWayFile = new File(tempFile, "multiways.dat");
			targetfolder.mkdirs();

			final File mapFile = outputFile;

			double minLat = converter.getMinLat();
			double minLon = converter.getMinLon();
			double maxLat = converter.getMaxLat();
			double maxLon = converter.getMaxLon();
			System.out.println("Bottomleft " + (-maxLat) + ", " + minLon);
			System.out.println("TopRight" + (-minLat) + ", " + maxLon);
			System.out.println("Download Map");

			final GenGroupFile genGroupFile = new GenGroupFile(mapConfig, multiWayFile, tempFile,
					targetfolder, converter);
			final CopyStaticFiles copyStaticFiles = new CopyStaticFiles();

			System.out.println("Generate Way File: "
					+ ((System.nanoTime() - startTime) / 1000000000) + " sec");
			GenMultiWayFile.genFiles(rawWayFile, rawRelationFile, multiWayFile);

			System.out.println("Generate Groups: " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
			genGroupFile.genGroup();
			System.out.println("Sort for Cells: " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
			genGroupFile.sortCells();
			System.out.println("Set Number in Cell: "
					+ ((System.nanoTime() - startTime) / 1000000000) + " sec");
			genGroupFile.setNumberInCell();
			System.out.println("Sort for Text: " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
			genGroupFile.sort();
			System.out.println("Gen Text DB: " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
			genGroupFile.genTextDb();
			System.out.println("Sort for Cells: " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
			genGroupFile.sortCells();
			System.out.println("Generate Layers: " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
			genGroupFile.genLayers();
			copyStaticFiles.copyStaticFiles(targetfolder, converter, outputFile.getName());
			final ImiGenerator imiGenerator = new ImiGenerator(targetfolder, mapFile);
			imiGenerator.genFile();
			System.out.println("Duration : " + ((System.nanoTime() - startTime) / 1000000000)
					+ " sec");
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) throws IOException {
		boolean expectInputFile = false;
		boolean expectOutputFile = false;
		boolean expectTempFolder = false;
		boolean expectBoundingBox = false;
		boolean noBoundingBox = true;
		boolean printUsage = false;
		String error = null;
		for (String arg : args) {
			if ("-i".equals(arg)){
				expectInputFile = true;
			} else if ("-o".equals(arg)){
				expectOutputFile = true;
			} else if ("-b".equals(arg)){
				expectBoundingBox = true;
			} else if ("-t".equals(arg)){
				expectTempFolder = true;
			} else if (expectInputFile){
				expectInputFile = false;
				inputFile = new File(arg);
			} else if (expectOutputFile){
				expectOutputFile = false;
				outputFile = new File(arg);
			} else if (expectTempFolder){
				expectTempFolder = false;
				tempFile = new File(arg);
			} else if (expectBoundingBox){
				expectBoundingBox = false;
				String coord[] = arg.split(",");
				double coords[] = new double[4];  
				if (coord.length != 4){
					error = "Wrong bounding box. Correct is -b minLat,minLon,maxLat,maxLon without space.";
				} else {
					for (int i = 0; i < coord.length; i++) {
						coords[i] = Double.parseDouble(coord[i]);
					}
					minLat = Math.min(coords[0], coords[2]);
					minLon = Math.min(coords[1], coords[3]);
					maxLat = Math.max(coords[0], coords[2]);
					maxLon = Math.max(coords[1], coords[3]);
					noBoundingBox = false;
				}
			}
		}
		if (tempFile == null && outputFile != null){
			tempFile = outputFile.getParentFile();
		}
		
		if (noBoundingBox){
			System.out.println("Missing BoundingBox");
			printUsage = true;
		} else if (outputFile == null){
			System.out.println("Missing Output File");
			printUsage = true;
		} else if (inputFile == null){
			System.out.println("Missing Input File");
			printUsage = true;
		} else if (tempFile == null){
			System.out.println("Missing temp File");
			printUsage = true;
		} 
		if (error != null){
			System.out.println(error);
			printUsage = true;
		} 
		
		if (printUsage){
			System.out.println("usage : java -jar mm.gen.jar -i [Input File] -o [Output File] -t [Temp Folder] -b minLat,minLon,maxLat,maxLon");
		} else {
			RelCoordConverter converter = getConverter();
			Generator generator = generateRawFiles();
			genMap(converter, generator.getResolvedFilteredWayFile(), generator.getResolvedRelationFile());
		}
	}
}

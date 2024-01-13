package org.rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.feature.simple.SimpleFeature;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        String geoTiffPath = currentDir + "\\data\\raster\\final_raster.tif";
        String shapeFilePath = currentDir + "\\data\\shape\\NY_3857.shp";

        try {
            // Тестирование класса GeoTiff
            GeoTiff geoTiff = new GeoTiff();
            GridCoverage2D raster = geoTiff.readGeoTiff(geoTiffPath);
            geoTiff.printCoverageSize(raster);
            double pixelValue = geoTiff.getPixelValue(raster, -8232338, 4977671);
            System.out.println("Pixel value at (-8232338, 4977671): " + pixelValue);

            // Тестирование класса ShapeFile
            ShapeFile shapeFile = new ShapeFile();
            String crs = shapeFile.getShapeFileCrs(shapeFilePath);
            System.out.println("Shape CRS: " + crs);
            List<SimpleFeature> roadGeometries = shapeFile.readRoadGeometries(shapeFilePath);
            System.out.println("Number of road geometries: " + roadGeometries.size());
            Double firstRoadWidth = shapeFile.getRoadWidth(roadGeometries.getFirst());
            System.out.println("First road width: " + firstRoadWidth + " meters");

            // Тестирование класса Rasterizer
            Rasterizer rasterizer = new Rasterizer();
            rasterizer.rasterize(geoTiffPath, shapeFilePath);
            System.out.println("Rasterization completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
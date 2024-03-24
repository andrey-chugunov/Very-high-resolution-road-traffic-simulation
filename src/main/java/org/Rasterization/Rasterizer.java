package org.Rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.styling.LineSymbolizer;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;

import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Rasterizer {

    private final Raster raster;
    private final ShapeFile shapeFile;

    public Rasterizer() {
        this.raster = new Raster();
        this.shapeFile = new ShapeFile();
    }

    public void rasterize(String geoTiffPath, String shapeFilePath) throws IOException, FactoryException, TransformException {
        // Читаем геотиф и геометрии из шейпа
        GridCoverage2D raster = this.raster.readGeoTiff(geoTiffPath);
        List<SimpleFeature> geometries = shapeFile.readRoadGeometries(shapeFilePath);

        //Вызов метода растеризации
        int[][] new_raster = rasterizeFunction(raster, geometries);
        Raster gt = new Raster();
        gt.createGeoTiff(new_raster, raster, "output.tif");
    }

    public List<LineString> splitMultiLineString(MultiLineString multiLineString) {
        List<LineString> lineStrings = new ArrayList<>();
        for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
            Geometry geometry = multiLineString.getGeometryN(i);
            if (geometry instanceof LineString) {
                lineStrings.add((LineString) geometry);
            }
        }
        return lineStrings;
    }

    public int determineDirection(Coordinate p1, Coordinate p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double angle = Math.atan2(dy, dx);

        if (angle >= 0 && angle <= Math.PI / 2) {
            return 1; // Вправо-вверх
        } else if (angle > Math.PI / 2 && angle <= Math.PI) {
            return 4; // Вправо-вниз
        } else if (angle < 0 && angle >= -Math.PI / 2) {
            return 2; // Влево-вверх
        } else {
            return 3; // Влево-вниз
        }
    }

    public void findPositions(String geoTiffPath, String shapeFilePath) throws FactoryException, IOException {
        GridCoverage2D raster = this.raster.readGeoTiff(geoTiffPath);
        List<SimpleFeature> geometries = shapeFile.readRoadGeometries(shapeFilePath);
        GridGeometry2D gridGeometry = raster.getGridGeometry();
        Envelope2D envelope2D = gridGeometry.getEnvelope2D();
        DirectPosition2D upperLeftCorner = new DirectPosition2D(envelope2D.getMinX(), envelope2D.getMaxY());
        double upperLeftX = upperLeftCorner.getX();
        double upperLeftY = upperLeftCorner.getY();

        int c = 0;
        List<Point> pixelsLine1 = new ArrayList<>();
        List<Point> pixelsLine2 = new ArrayList<>();
        List<Point> pixelsLine3 = new ArrayList<>();
        List<Point> pixelsLine4 = new ArrayList<>();

        for (SimpleFeature feature : geometries) {
            c++;
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (geometry != null) {
                MultiLineString multiLineString = (MultiLineString) geometry;

                List<LineString> lineStrings = splitMultiLineString(multiLineString);

                for (LineString line : lineStrings) {
                    List<LineString> parallelGeometry1 = shapeFile.generateParallelLineStrings(line, 5);
                    List<LineString> parallelGeometry2 = shapeFile.generateParallelLineStrings(line, -5);
                    for (LineString section : parallelGeometry1) {
                        Coordinate p1 = section.getCoordinateN(0);
                        Coordinate p2 = section.getCoordinateN(1);
                        int direction = determineDirection(p1, p2);
                        Envelope envelope1 = new Envelope(section.getEnvelopeInternal());
                        int minPixelX = (int) Math.floor(envelope1.getMinX());
                        int maxPixelX = (int) Math.ceil(envelope1.getMaxX());
                        int minPixelY = (int) Math.floor(envelope1.getMinY());
                        int maxPixelY = (int) Math.ceil(envelope1.getMaxY());

                        for (int y = minPixelY; y < maxPixelY; y++) {
                            for (int x = minPixelX; x < maxPixelX; x++) {
                                if (section.intersects(createPixelRectangle(x, y))) {
                                    if (direction == 1 || direction == 3) {
                                        pixelsLine1.add(new Point((int) upperLeftY - y, x - (int) upperLeftX));
                                    } else {
                                        pixelsLine2.add(new Point((int) upperLeftY - y, x - (int) upperLeftX));
                                    }
                                }
                            }
                        }
                    }
                    for (LineString section : parallelGeometry2) {
                        Coordinate p1 = section.getCoordinateN(0);
                        Coordinate p2 = section.getCoordinateN(1);
                        int direction = determineDirection(p1, p2);
                        Envelope envelope1 = new Envelope(section.getEnvelopeInternal());
                        int minPixelX = (int) Math.floor(envelope1.getMinX());
                        int maxPixelX = (int) Math.ceil(envelope1.getMaxX());
                        int minPixelY = (int) Math.floor(envelope1.getMinY());
                        int maxPixelY = (int) Math.ceil(envelope1.getMaxY());

                        for (int y = minPixelY; y < maxPixelY; y++) {
                            for (int x = minPixelX; x < maxPixelX; x++) {
                                if (section.intersects(createPixelRectangle(x, y))) {
                                    if (direction == 1 || direction == 3) {
                                        pixelsLine3.add(new Point((int) upperLeftY - y, x - (int) upperLeftX));
                                    } else {
                                        pixelsLine4.add(new Point((int) upperLeftY - y, x - (int) upperLeftX));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println(c);
        }
        writePixelsToFile("position1.txt", pixelsLine1);
        writePixelsToFile("position2.txt", pixelsLine2);
        writePixelsToFile("position3.txt", pixelsLine3);
        writePixelsToFile("position4.txt", pixelsLine4);
    }

    private Polygon createPixelRectangle(int x, int y) {
        int size = 1;
        Envelope pixelEnvelope = new Envelope(x, x + size, y, y + size);
        return JTS.toGeometry(pixelEnvelope);
    }

    private void writePixelsToFile(String filePath, List<Point> pixels) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Point pixel : pixels) {
                writer.write((int) pixel.getX() + " " + (int) pixel.getY());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void readCoordinates(String filename, int[][] new_raster, int value) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                new_raster[x][y] = value;
            }
        }
    }


    private int[][] rasterizeFunction(GridCoverage2D raster, List<SimpleFeature> geometries) throws TransformException, IOException {
        int raster_width = raster.getRenderedImage().getWidth();
        int raster_height = raster.getRenderedImage().getHeight();
        int[][] new_raster = new int[raster_height][raster_width];
        GridGeometry2D gridGeometry = raster.getGridGeometry();
        Envelope2D envelope2D = gridGeometry.getEnvelope2D();
        DirectPosition2D upperLeftCorner = new DirectPosition2D(envelope2D.getMinX(), envelope2D.getMaxY());
        double upperLeftX = upperLeftCorner.getX();
        double upperLeftY = upperLeftCorner.getY();
        int c = 0;
        for (SimpleFeature feature : geometries) {
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            if (geometry != null) {
                Double roadWidth = shapeFile.getRoadWidth(feature);
                Geometry bufferedGeometry = geometry.buffer(roadWidth/2);

                Envelope envelope = new Envelope(bufferedGeometry.getEnvelopeInternal());

                int minPixelX = (int) Math.floor(envelope.getMinX());
                int maxPixelX = (int) Math.ceil(envelope.getMaxX());
                int minPixelY = (int) Math.floor(envelope.getMinY());
                int maxPixelY = (int) Math.ceil(envelope.getMaxY());
                for (int y = minPixelY; y < maxPixelY; y++) {
                    for (int x = minPixelX; x < maxPixelX; x++) {
                        if (bufferedGeometry.intersects(JTS.toGeometry(new DirectPosition2D(x + 0.5, y + 0.5)))) {
                            new_raster[(int) upperLeftY - y][x - (int) upperLeftX] = 1;
                        }
                    }
                }
            }
            System.out.println(c);
            c++;
        }
        int count = 0;
        for (int y = 0; y < raster_height; y++) {
            for (int x = 0; x < raster_width; x++) {
                if (new_raster[y][x] == 1) {
                    count++;
                }
            }
        }
        System.out.println("Counted pixels: " + count);
        //markIntersections(new_raster, geometries, raster);
        System.out.println("intersection");
        readCoordinates("position1.txt", new_raster, 3);
        readCoordinates("position2.txt", new_raster, 4);
        readCoordinates("position3.txt", new_raster, 5);
        readCoordinates("position4.txt", new_raster, 6);
        return new_raster;
    }

//    private void markIntersections(int[][] new_raster, List<SimpleFeature> geometries, GridCoverage2D raster) throws TransformException {
//        GridGeometry2D gridGeometry = raster.getGridGeometry();
//        Envelope2D envelope2D = gridGeometry.getEnvelope2D();
//        DirectPosition2D upperLeftCorner = new DirectPosition2D(envelope2D.getMinX(), envelope2D.getMaxY());
//        double upperLeftX = upperLeftCorner.getX();
//        double upperLeftY = upperLeftCorner.getY();
//
//        for (int i = 0; i < geometries.size(); i++) {
//            SimpleFeature feature1 = geometries.get(i);
//            Geometry geometry1 = (Geometry) feature1.getDefaultGeometry();
//            if (geometry1 != null) {
//                for (int j = i + 1; j < geometries.size(); j++) {
//                    SimpleFeature feature2 = geometries.get(j);
//                    Geometry geometry2 = (Geometry) feature2.getDefaultGeometry();
//                    if (geometry2 != null && geometry1.intersects(geometry2)) {
//                        Geometry intersection = geometry1.intersection(geometry2);
//                        Envelope envelope = intersection.getEnvelopeInternal();
//                        GeometryFactory geometryFactory = new GeometryFactory();
//                        for (int y = (int) Math.floor(envelope.getMinY()); y <= Math.ceil(envelope.getMaxY()); y++) {
//                            for (int x = (int) Math.floor(envelope.getMinX()); x <= Math.ceil(envelope.getMaxX()); x++) {
//                                if (geometry1.intersects(geometryFactory.createPoint(coordinate)) &&
//                                        bufferedGeometry2.intersects(geometryFactory.createPoint(coordinate))) {
//                                    new_raster[(int) upperLeftY - y][x - (int) upperLeftX] = 2;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            System.out.println(i);
//        }
//    }
}
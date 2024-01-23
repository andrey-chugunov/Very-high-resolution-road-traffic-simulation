package org.rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Envelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.List;

public class Rasterizer {

    private final GeoTiff geoTiff;
    private final ShapeFile shapeFile;

    public Rasterizer() {
        this.geoTiff = new GeoTiff();
        this.shapeFile = new ShapeFile();
    }

    public void rasterize(String geoTiffPath, String shapeFilePath) throws IOException, FactoryException, TransformException {
        // Читаем геотиф и геометрии из шейпа
        GridCoverage2D raster = geoTiff.readGeoTiff(geoTiffPath);
        List<SimpleFeature> geometries = shapeFile.readRoadGeometries(shapeFilePath);

        //Вызов метода растеризации
        int[][] new_raster = rasterizeFunction(raster, geometries);
        GeoTiff gt = new GeoTiff();
        gt.createGeoTiff(new_raster, raster, "output.tif");
    }

    private int[][] rasterizeFunction(GridCoverage2D raster, List<SimpleFeature> geometries) throws TransformException {
        int raster_width = raster.getRenderedImage().getWidth();
        int raster_height = raster.getRenderedImage().getHeight();
        int[][] new_raster = new int[raster_height][raster_width];

        //получаем верхнюю левую мировую координату растра
        GridGeometry2D gridGeometry = raster.getGridGeometry();
        Envelope2D envelope2D = gridGeometry.getEnvelope2D();
        DirectPosition2D upperLeftCorner = new DirectPosition2D(envelope2D.getMinX(), envelope2D.getMaxY());
        double upperLeftX = upperLeftCorner.getX();
        double upperLeftY = upperLeftCorner.getY();

        for (SimpleFeature feature : geometries) {
            // Получаем геометрию и ширину дороги
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Double roadWidth = shapeFile.getRoadWidth(feature);
            if (geometry != null) {
                // Выполняем буфферизацию
                Geometry bufferedGeometry = geometry.buffer(roadWidth);

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
        return new_raster;
    }
}
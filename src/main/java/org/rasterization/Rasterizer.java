package org.rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.TransfiniteSet;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.locationtech.jts.geom.Geometry;

import java.io.IOException;
import java.util.List;

import static org.rasterization.RasterWriter.writeRaster;

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
        writeRaster(new_raster, "output.png");
    }

    private int[][] rasterizeFunction(GridCoverage2D raster, List<SimpleFeature> geometries) throws TransformException {
        int raster_width = raster.getRenderedImage().getWidth();
        int raster_height = raster.getRenderedImage().getHeight();
        int[][] new_raster = new int[raster_height][raster_width];
        GridGeometry2D gridGeometry = raster.getGridGeometry();
        for (SimpleFeature feature : geometries) {
            // Получаем геометрию и ширину дороги
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            Double roadWidth = shapeFile.getRoadWidth(feature);
            if (geometry != null) {
                // Выполняем буфферизацию
                Geometry bufferedGeometry = geometry.buffer(roadWidth);
                // Проходим по пикселям растра
                for (int y = 0; y < raster_height; y++) {
                    for (int x = 0; x < raster_width; x++) {
                        DirectPosition2D pixelCenter = new DirectPosition2D(x + 0.5, y + 0.5);
                        DirectPosition2D worldCord = new DirectPosition2D();
                        gridGeometry.getGridToCRS().transform(pixelCenter, worldCord);

                        //Проверка пересечения дороги и пикселя [x, y] в мировых координатах
                        if (bufferedGeometry.intersects(JTS.toGeometry(worldCord))) {
                            new_raster[y][x] = 1;
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
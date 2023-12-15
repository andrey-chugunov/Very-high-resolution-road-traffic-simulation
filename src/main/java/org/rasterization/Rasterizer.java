package org.rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.referencing.FactoryException;
import java.io.IOException;
import java.util.List;

public class Rasterizer {

    private final GeoTiff geoTiff;
    private final ShapeFile shapeFile;

    public Rasterizer() {
        this.geoTiff = new GeoTiff();
        this.shapeFile = new ShapeFile();
    }

    public void rasterize(String geoTiffPath, String shapeFilePath) throws IOException, FactoryException {
        // Читаем геотиф и геометрии из шейпа
        GridCoverage2D raster = geoTiff.readGeoTiff(geoTiffPath);
        List<SimpleFeature> geometries = shapeFile.readRoadGeometries(shapeFilePath);

        //Вызов метода растеризации
        rasterizeFunction(raster, geometries);
    }

    private GridCoverage2D rasterizeFunction(GridCoverage2D raster, List<SimpleFeature> geometries) {
        //Растеризация

        return raster;
    }


}
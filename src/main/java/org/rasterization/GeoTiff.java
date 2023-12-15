package org.rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;

public class GeoTiff {
    public GridCoverage2D readGeoTiff(String filePath) throws IOException, FactoryException {
        File file = new File(filePath);
        AbstractGridFormat format = GridFormatFinder.findFormat(file);
        GridCoverage2DReader reader = format.getReader(file, null);
        return reader.read(null);
    }

    public double getPixelValue(GridCoverage2D raster, int x, int y) {
        DirectPosition position = new DirectPosition2D(x, y);
        double[] value = new double[1];
        raster.evaluate(position, value);
        return value[0];
    }

    public void printCoverageSize(GridCoverage2D coverage) {
        int width = coverage.getRenderedImage().getWidth();
        int height = coverage.getRenderedImage().getHeight();
        System.out.println("Width: " + width + ", Height: " + height);
    }

    public void updatePixelValue(GridCoverage2D raster, int x, int y, double newValue) {

    }
}
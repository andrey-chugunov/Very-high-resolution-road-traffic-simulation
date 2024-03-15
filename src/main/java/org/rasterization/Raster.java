package org.rasterization;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.factory.Hints;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

public class Raster {
    public GridCoverage2D readGeoTiff(String filePath) throws IOException, FactoryException {
        File file = new File(filePath);
        GeoTiffReader reader = new GeoTiffReader(file, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
//        AbstractGridFormat format = GridFormatFinder.findFormat(file);
//        GridCoverage2DReader reader = format.getReader(file, null);
        return reader.read(null);
    }

    public double getPixelValue(GridCoverage2D raster, int x, int y) {
        DirectPosition position = new DirectPosition2D(x, y);
        double[] value = new double[1];
        raster.evaluate(position, value);
        return value[0];
    }

    public void printCoverageSize(GridCoverage2D raster) {
        int width = raster.getRenderedImage().getWidth();
        int height = raster.getRenderedImage().getHeight();
        System.out.println("Width: " + width + ", Height: " + height);
    }

    public void createGeoTiff(int[][] data, GridCoverage2D raster, String outputPath) throws IOException {
        GridCoverageFactory gcf = new GridCoverageFactory();
        CoordinateReferenceSystem originalCRS = raster.getCoordinateReferenceSystem();

        Envelope2D envelope2D = raster.getEnvelope2D();
        double minX = envelope2D.getMinX();
        double minY = envelope2D.getMinY();
        double maxX = envelope2D.getMaxX();
        double maxY = envelope2D.getMaxY();

        ReferencedEnvelope referencedEnvelope = new ReferencedEnvelope(minX, maxX, minY, maxY, originalCRS);

        RenderedImage renderedImage = createRenderedImage(data);
        GridCoverage2D gc = gcf.create("outputRaster", renderedImage, referencedEnvelope);

        File file = new File(outputPath);
        GeoTiffWriter writer = new GeoTiffWriter(file);
        writer.write(gc, null);
        writer.dispose();
    }

    public RenderedImage createRenderedImage(int[][] data) {
        int width = data[0].length;
        int height = data.length;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = data[y][x] == 1 ? 0x000000 : 0xFFFFFF;
                bufferedImage.setRGB(x, y, pixelValue);
            }
        }

        return bufferedImage;
    }

}
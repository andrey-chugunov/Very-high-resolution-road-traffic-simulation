package org.rasterization;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RasterWriter {

    public static void writeRaster(int[][] raster, String outputPath) throws IOException {
        int width = raster[0].length;
        int height = raster.length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixelValue = raster[y][x] == 1 ? 0x000000 : 0xFFFFFF;
                image.setRGB(x, y, pixelValue);
            }
        }

        File outputImageFile = new File(outputPath);
        ImageIO.write(image, "png", outputImageFile);
    }
}

package org.UserInterface;

import org.geotools.coverage.grid.GridCoverage2D;
import org.opengis.referencing.FactoryException;
import org.Rasterization.Raster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.awt.event.KeyAdapter;

public class GUI {

    private static final String currentDir = System.getProperty("user.dir");
    private static final String outputPath = currentDir + "\\data\\raster\\output.tif";
    private static GridCoverage2D coverage2D;
    private static int offsetX = -7500;
    private static int offsetY = -15000;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Raster raster = new Raster();
                coverage2D = raster.readGeoTiff(outputPath);

                createAndShowGUI();
            } catch (FactoryException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Raster Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final Image[] swingImage = {convertToSwingImage(coverage2D)};
        System.out.println(swingImage[0]);

        ImageIcon icon = new ImageIcon(swingImage[0]);
        JLabel label = new JLabel(icon);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.CENTER);

        label.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int stepX = 50;
                int stepY = 100;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> offsetX += stepX;
                    case KeyEvent.VK_RIGHT -> offsetX -= stepX;
                    case KeyEvent.VK_UP -> offsetY += stepY;
                    case KeyEvent.VK_DOWN -> offsetY -= stepY;
                }
                swingImage[0] = convertToSwingImage(coverage2D);
                icon.setImage(swingImage[0]);
                label.repaint();
            }
        });

        label.setFocusable(true);
        frame.getContentPane().add(panel);
        int frameWidth = swingImage[0].getWidth(null);
        int frameHeight = swingImage[0].getHeight(null);
        frame.setSize(frameWidth, frameHeight);
        frame.setVisible(true);
    }

    private static Image convertToSwingImage(GridCoverage2D coverage) {
        try {
            return getRenderedImage(coverage);
        } catch (IOException | FactoryException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static BufferedImage getRenderedImage(GridCoverage2D coverage) throws IOException, FactoryException {
        RenderedImage renderedImage = coverage.getRenderedImage();
        BufferedImage bufferedImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        AffineTransform transform = new AffineTransform();
        transform.translate(offsetX, offsetY);
        g.drawRenderedImage(renderedImage, transform);
        g.dispose();
        return bufferedImage;
    }
}
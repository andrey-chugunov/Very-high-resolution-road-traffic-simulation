package org.Traffic;

import org.geotools.coverage.grid.GridCoverage2D;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Traffic {
    private final RenderedImage raster;
    private final int rasterWidth;
    private final int rasterHeight;
    private Position position;
    private List<Car> cars;
    public Traffic(GridCoverage2D coverage2D) throws IOException {
        this.raster = coverage2D.getRenderedImage();
        this.rasterWidth = raster.getWidth();
        this.rasterHeight = raster.getHeight();
        this.position = new Position();
    }

    public void generateCars(int n) {
        Random random = new Random();
        List<Point> position1 = position.getPosition1();
        List<Point> position2 = position.getPosition2();
        List<Point> position3 = position.getPosition3();
        List<Point> position4 = position.getPosition4();
        List<Car> new_cars = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Point randomPosition;
            int randomDirection = random.nextInt(4) + 1;  // 1, 2, 3 или 4

            switch (randomDirection) {
                case 1:
                    if (!position1.isEmpty()) {
                        randomPosition = position1.get(random.nextInt(position1.size()));
                        break;
                    }
                case 2:
                    if (!position2.isEmpty()) {
                        randomPosition = position2.get(random.nextInt(position2.size()));
                        break;
                    }
                case 3:
                    if (!position3.isEmpty()) {
                        randomPosition = position3.get(random.nextInt(position3.size()));
                        break;
                    }
                case 4:
                    if (!position4.isEmpty()) {
                        randomPosition = position4.get(random.nextInt(position4.size()));
                        break;
                    }
                default:
                    continue;
            }

            int speed = random.nextInt(4);
            Car car = new Car(randomPosition, speed);
            new_cars.add(car);
        }

        this.cars = new_cars;
    }

    public List<Car> getTraffic() {
        return cars;
    }

    public void findPossibleMoves() {

    }

}

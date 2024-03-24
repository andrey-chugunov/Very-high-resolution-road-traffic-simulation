package org.Traffic;

import org.geotools.coverage.grid.GridCoverage2D;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.*;
import java.util.List;

public class Traffic {
    private final RenderedImage raster;
    private final int rasterWidth;
    private final int rasterHeight;
    private Position position;
    private List<Car> cars;
    private Map<Point, List<Point>> graph;
    public Traffic(GridCoverage2D coverage2D) throws IOException {
        this.raster = coverage2D.getRenderedImage();
        this.rasterWidth = raster.getWidth();
        this.rasterHeight = raster.getHeight();
        this.position = new Position();
        //createGraph("graph.dat");
        System.out.println("start");
        this.graph = loadGraphFromFile("graph.dat");
        System.out.println("end");
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
            int randomDirection = random.nextInt(4) + 1;

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

    public BufferedImage makeIteration(BufferedImage currentImage, int offsetX, int offsetY) {
        for (Car car : cars) {

            List<Point> possibleMoves = graph.get(new Point(car.getPosition_x(), car.getPosition_y()));

            if (possibleMoves != null && !possibleMoves.isEmpty()) {
                Random random = new Random();
                Point selectedMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
                car.setPosition(selectedMove);

                if (car.getPosition_x() + offsetX >= 0 && car.getPosition_x() + offsetX < currentImage.getWidth() &&
                        car.getPosition_y() + offsetY >= 0 && car.getPosition_y() + offsetY < currentImage.getHeight()) {
                    int pixelColor = 0xFF00;
                    currentImage.setRGB(car.getPosition_x() + offsetX, car.getPosition_y() + offsetY, pixelColor);
                }
            }
        }
        return currentImage;
    }

    public void createGraph(String filename) {
        Map<Point, List<Point>> graph = new HashMap<>();
        Position positions = null;
        try {
            positions = new Position();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<Point> allPoints = new ArrayList<>();
        allPoints.addAll(positions.getPosition1());
        allPoints.addAll(positions.getPosition2());
        allPoints.addAll(positions.getPosition3());
        allPoints.addAll(positions.getPosition4());

        int c = 0;
        for (Point point : allPoints) {
            List<Point> possibleMoves = findPossibleMoves(new Car(point, 1));
            if (!possibleMoves.isEmpty()) {
                graph.put(point, possibleMoves);
            }
            c++;
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(graph);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Point, List<Point>> loadGraphFromFile(String filename) {
        Map<Point, List<Point>> graph = null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            graph = (Map<Point, List<Point>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return graph;
    }

    public List<Point> findPossibleMoves(Car car) {
        int x = car.getPosition_x();
        int y = car.getPosition_y();
        List<Point> moves = new ArrayList<>();
        Raster rasterData = raster.getData(new Rectangle(x - 1, y - 1, 3, 3));
        int red = rasterData.getSample(x, y, 0);
        int green = rasterData.getSample(x, y, 1);
        int blue = rasterData.getSample(x, y, 2);

        //первая четверть (красный)
        if (red == 255 && green == 0 && blue == 0) {
            if (!((rasterData.getSample(x + 1, y, 0) == 0) &&
                    (rasterData.getSample(x + 1, y, 1) == 0) &&
                    (rasterData.getSample(x + 1, y, 2) == 0))) {
                moves.add(new Point(x + 1, y));
            }
            if (!((rasterData.getSample(x, y - 1, 0) == 0) &&
                    (rasterData.getSample(x, y - 1, 1) == 0) &&
                    (rasterData.getSample(x, y - 1, 2) == 0))) {
                moves.add(new Point(x, y - 1));
            }
            if (!((rasterData.getSample(x + 1, y - 1, 0) == 0) &&
                    (rasterData.getSample(x + 1, y - 1, 1) == 0) &&
                    (rasterData.getSample(x + 1, y - 1, 2) == 0))) {
                moves.add(new Point(x + 1, y - 1));
            }
        }
        //вторая четверть (синий)
        if (red == 0 && green == 0 && blue == 255) {
            if (!((rasterData.getSample(x, y - 1, 0) == 0) &&
                    (rasterData.getSample(x, y - 1, 1) == 0) &&
                    (rasterData.getSample(x, y - 1, 2) == 0))) {
                moves.add(new Point(x, y - 1));
            }
            if (!((rasterData.getSample(x - 1, y, 0) == 0) &&
                    (rasterData.getSample(x - 1, y, 1) == 0) &&
                    (rasterData.getSample(x - 1, y, 2) == 0))) {
                moves.add(new Point(x - 1, y));
            }
            if (!((rasterData.getSample(x - 1, y - 1, 0) == 0) &&
                    (rasterData.getSample(x - 1, y - 1, 1) == 0) &&
                    (rasterData.getSample(x - 1, y - 1, 2) == 0))) {
                moves.add(new Point(x - 1, y - 1));
            }
        }
        //третья четверть (голубой)
        if (red == 0 && green == 255 && blue == 255) {
            if (!((rasterData.getSample(x - 1, y, 0) == 0) &&
                    (rasterData.getSample(x - 1, y, 1) == 0) &&
                    (rasterData.getSample(x - 1, y, 2) == 0))) {
                moves.add(new Point(x - 1, y));
            }
            if (!((rasterData.getSample(x, y + 1, 0) == 0) &&
                    (rasterData.getSample(x, y + 1, 1) == 0) &&
                    (rasterData.getSample(x, y + 1, 2) == 0))) {
                moves.add(new Point(x, y + 1));
            }
            if (!((rasterData.getSample(x - 1, y + 1, 0) == 0) &&
                    (rasterData.getSample(x - 1, y + 1, 1) == 0) &&
                    (rasterData.getSample(x - 1, y + 1, 2) == 0))) {
                moves.add(new Point(x - 1, y + 1));
            }
        }
        //четвертая четверть (желтый)
        if (red == 255 && green == 255 && blue == 0) {
            if (!((rasterData.getSample(x + 1, y, 0) == 0) &&
                    (rasterData.getSample(x + 1, y, 1) == 0) &&
                    (rasterData.getSample(x + 1, y, 2) == 0))) {
                moves.add(new Point(x + 1, y));
            }
            if (!((rasterData.getSample(x + 1, y - 1, 0) == 0) &&
                    (rasterData.getSample(x + 1, y - 1, 1) == 0) &&
                    (rasterData.getSample(x + 1, y - 1, 2) == 0))) {
                moves.add(new Point(x + 1, y - 1));
            }
            if (!((rasterData.getSample(x, y + 1, 0) == 0) &&
                    (rasterData.getSample(x, y + 1, 1) == 0) &&
                    (rasterData.getSample(x, y + 1, 2) == 0))) {
                moves.add(new Point(x, y + 1));
            }
        }
        return moves;
    }
}

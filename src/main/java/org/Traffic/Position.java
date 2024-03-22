package org.Traffic;

import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Position {
    private final List<Point> position1;
    private final List<Point> position2;
    private final List<Point> position3;
    private final List<Point> position4;

    public Position() throws IOException {
        this.position1 = readCoordinates("position1.txt");
        this.position2 = readCoordinates("position2.txt");
        this.position3 = readCoordinates("position3.txt");
        this.position4 = readCoordinates("position4.txt");
    }

    private static List<Point> readCoordinates(String filename) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            List<Point> list = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                int x = Integer.parseInt(parts[0]);
                int y = Integer.parseInt(parts[1]);
                Point point = new Point(x, y);
                list.add(point);
            }
            return list;
        }
    }

    public List<Point> getPosition1() {
        return position1;
    }

    public List<Point> getPosition2() {
        return position2;
    }
    public List<Point> getPosition3() {
        return position3;
    }
    public List<Point> getPosition4() {
        return position4;
    }
}

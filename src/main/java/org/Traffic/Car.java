package org.Traffic;

import java.awt.*;

public class Car {
    private int speed;
    private int position_x;
    private int position_y;
    public Car(Point position, int speed) {
        this.position_x = (int) position.getX();
        this.position_y = (int) position.getY();
        this.speed = speed;
    }

    public int getPosition_x() {
        return position_x;
    }

    public int getPosition_y() {
        return position_y;
    }

    public int getSpeed() {
        return speed;
    }


    public void setPosition(Point position) {
        this.position_x = (int) position.getX();
        this.position_y = (int) position.getY();
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }
}

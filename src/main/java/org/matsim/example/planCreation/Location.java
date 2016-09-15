package org.matsim.example.planCreation;

/**
 * Created by carlloga on 9/12/2016.
 */
public class Location {

    private int id;
    private double x;
    private double y;
    private double accessibility;

    public Location(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setAccessibility(double accessibility) {
        this.accessibility = accessibility;
    }

    public double getAccessibility() {
        return accessibility;
    }
}

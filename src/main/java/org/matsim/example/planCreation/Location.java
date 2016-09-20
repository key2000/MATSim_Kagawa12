package org.matsim.example.planCreation;

/**
 * Created by carlloga on 9/12/2016.
 */
public class Location {

    private int id;
    private double x;
    private double y;
    private long population;
    private double accessibility;
    private double travelTime;

    public Location(int id, double x, double y, long population) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.population = population;
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

    public long getPopulation() {
        return population;
    }

    public void setAccessibility(double accessibility) {
        this.accessibility = accessibility;
    }

    public double getAccessibility() {
        return accessibility;
    }

    public double getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(double travelTime) {
        this.travelTime = travelTime;
    }
}


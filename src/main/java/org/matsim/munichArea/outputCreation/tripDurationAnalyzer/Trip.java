package org.matsim.munichArea.outputCreation.tripDurationAnalyzer;

import org.matsim.api.core.v01.Id;

/**
 * Created by carlloga on 17.03.2017.
 */
public class Trip {
    private double departureTime;
    private double arrivalTime;
    private double vehicleStartTime;
    private Id id;
    private String mode;
    private boolean atWorkPlace;

    public Trip(Id id) {
        this.id = id;
        atWorkPlace = false;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public boolean isAtWorkPlace() {
        return atWorkPlace;
    }

    public void setAtWorkPlace(boolean atWorkPlace) {
        this.atWorkPlace = atWorkPlace;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getDuration() {
        return (arrivalTime - departureTime);
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setVehicleStartTime(double vehicleStartTime) {
        this.vehicleStartTime = vehicleStartTime;
    }

    public double getWaitingTimeBefore(){
        return (vehicleStartTime - departureTime);

    }
}

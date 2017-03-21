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

    public Trip(Id id, double departureTime) {
        this.id = id;
        this.departureTime = departureTime;
    }



    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
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

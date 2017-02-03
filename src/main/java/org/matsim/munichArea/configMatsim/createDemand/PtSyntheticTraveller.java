package org.matsim.munichArea.configMatsim.createDemand;

import com.sun.xml.internal.stream.events.LocationImpl;
import org.matsim.api.core.v01.population.Person;
import org.matsim.munichArea.planCreation.Location;

/**
 * Created by carlloga on 3/2/17.
 */
public class PtSyntheticTraveller {

    private int id;
    private Location origLoc;
    private Location destLoc;
    private Person person;
    private double departureTime;
    private double arrivalTime;

    public PtSyntheticTraveller(int id, Location origLoc, Location destLoc, Person person) {
        this.id = id;
        this.origLoc = origLoc;
        this.destLoc = destLoc;
        this.person = person;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Location getOrigLoc() {
        return origLoc;
    }

    public void setOrigLoc(Location origLoc) {
        this.origLoc = origLoc;
    }

    public Location getDestLoc() {
        return destLoc;
    }

    public void setDestLoc(Location destLoc) {
        this.destLoc = destLoc;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}

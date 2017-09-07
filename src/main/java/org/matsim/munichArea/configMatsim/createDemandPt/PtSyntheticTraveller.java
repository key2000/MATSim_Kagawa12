package org.matsim.munichArea.configMatsim.createDemandPt;

import org.matsim.api.core.v01.population.Person;
import org.matsim.munichArea.configMatsim.planCreation.Location;

import java.util.HashMap;

/**
 * Created by carlloga on 3/2/17.
 */
public class PtSyntheticTraveller {

    private int id;
    private Location origLoc;
    private Location destLoc;
    private Person person;
    private double departureTime;
    //in case the traveller nevel arrive gives a very long duration in mins
    private double arrivalTime = 1000000;
    private HashMap<Integer,Double> boardingMap;
    private HashMap<Integer,Double> alightingMap;
    private int boardSeq;
    private int alightSeq;




    public PtSyntheticTraveller(int id, Location origLoc, Location destLoc, Person person) {
        this.id = id;
        this.origLoc = origLoc;
        this.destLoc = destLoc;
        this.person = person;
        this.boardingMap = new HashMap<>();
        this.alightingMap = new HashMap<>();
        this.boardSeq = 0;
        this.alightSeq = 0;
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

    public void boards(double boardingTime){
        boardingMap.put(boardSeq,boardingTime);
        boardSeq++;
    }

    public void alights(double alightingTime){
        alightingMap.put(alightSeq,alightingTime);
        alightSeq++;
    }

    public HashMap<Integer, Double> getBoardingMap() {
        return boardingMap;
    }

    public HashMap<Integer, Double> getAlightingMap() {
        return alightingMap;
    }

    public int getTransfers() {
        return alightSeq -1;
    }

    public double getInVehicleTime() {
        int size_b = boardingMap.size();
        int size_a = alightingMap.size();
        int size = Math.min(size_b,size_a);
        double vehicleInTime = 0;
        for (int i =0; i< size; i++){
            vehicleInTime += alightingMap.get(i) - boardingMap.get(i);

/*
            try {
                vehicleInTime += alightingMap.get(i) - boardingMap.get(i);
            } catch (Exception e) {

                if((!alightingMap.get(i).isNaN()))
                throw new RuntimeException(e);
            }
*/
/*
            if((!alightingMap.get(i).isNaN()) && (!alightingMap.get(i).isInfinite()) && (!boardingMap.get(i).isNaN()) && (!boardingMap.get(i).isInfinite()) ) {
                vehicleInTime += alightingMap.get(i) - boardingMap.get(i);
            }
*/
        }
        return vehicleInTime;
    }

    public double getAccessTimeByWalk() {
        double accessTime = -1;
        if (boardingMap.size()>0)  accessTime = boardingMap.get(0)-departureTime;
        return  accessTime;
    }

    public double getEgressTimeByWalk() {
        double egressTime = -1;
        if (alightingMap.size()>0)  egressTime = -alightingMap.get(alightingMap.size()-1)+arrivalTime;
        return  egressTime;
    }

}

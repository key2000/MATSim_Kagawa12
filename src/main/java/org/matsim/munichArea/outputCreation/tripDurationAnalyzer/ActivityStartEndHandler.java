package org.matsim.munichArea.outputCreation.tripDurationAnalyzer;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.munichArea.configMatsim.createDemand.PtSyntheticTraveller;

import java.util.Map;

/**
 * Created by carlloga on 17.03.2017.
 */

public class ActivityStartEndHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler, PersonEntersVehicleEventHandler {



    private Map<Id, Trip> tripMap;


    public ActivityStartEndHandler(Map<Id, Trip> tripMap) {
        this.tripMap = tripMap;

    }

    @Override
    public void reset(int iteration) {
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {
        //detects the event of arriving to work
        if (event.getEventType().equals("work")) {
            Trip t = tripMap.get(event.getPersonId());
            t.setArrivalTime(event.getTime());
        }
    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        //detects the event of departing from home
        if (event.getEventType().equals("home")) {
            Trip t = new Trip(event.getPersonId(), event.getTime());
            tripMap.put(event.getPersonId(), t);
        }

    }

    @Override
    public void handleEvent (PersonEntersVehicleEvent event){
        if (event.getAttributes().get("legMode").equals("taxi")){
            Trip t = tripMap.get(event.getPersonId());
            t.setVehicleStartTime(event.getTime());
        }
    }

}


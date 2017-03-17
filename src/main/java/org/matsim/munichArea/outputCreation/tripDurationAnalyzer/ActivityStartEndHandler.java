package org.matsim.munichArea.outputCreation.tripDurationAnalyzer;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.munichArea.configMatsim.createDemand.PtSyntheticTraveller;

import java.util.Map;

/**
 * Created by carlloga on 17.03.2017.
 */

public class ActivityStartEndHandler implements PersonDepartureEventHandler, PersonArrivalEventHandler {



    private Map<Id, Trip> tripMap;


    public ActivityStartEndHandler(Map<Id, Trip> tripMap) {
        this.tripMap = tripMap;

    }

    @Override
    public void reset(int iteration) {
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        Trip t = tripMap.get(event.getPersonId());
        t.setArrivalTime(event.getTime());

    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {

        Trip t = new Trip(event.getPersonId(), event.getTime());
        tripMap.put(event.getPersonId(), t);

    }

}


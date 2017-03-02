package org.matsim.munichArea.outputCreation;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.PersonLeavesVehicleEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.PersonLeavesVehicleEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;
import org.matsim.munichArea.configMatsim.createDemand.PtSyntheticTraveller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by carlloga on 3/2/17.
 */
public class ODTripAnalyzer implements PersonDepartureEventHandler, PersonArrivalEventHandler, PersonEntersVehicleEventHandler, PersonLeavesVehicleEventHandler {

    private Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap;


    public ODTripAnalyzer(Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap) {
        this.ptSyntheticTravellerMap = ptSyntheticTravellerMap;
    }


    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        try {
            PtSyntheticTraveller ptSyntheticTraveller = ptSyntheticTravellerMap.get(event.getPersonId());
            ptSyntheticTraveller.setDepartureTime(event.getTime());
        } catch (Exception e) {}
    }

    public void handleEvent(PersonEntersVehicleEvent event) {
        try {
            PtSyntheticTraveller ptSyntheticTraveller = ptSyntheticTravellerMap.get(event.getPersonId());
            ptSyntheticTraveller.boards(event.getTime());
        } catch (Exception e) {}
    }

    public void handleEvent(PersonLeavesVehicleEvent event) {
        try {
            PtSyntheticTraveller ptSyntheticTraveller = ptSyntheticTravellerMap.get(event.getPersonId());
            ptSyntheticTraveller.alights(event.getTime());
        } catch (Exception e) {}
    }

    public void handleEvent(PersonArrivalEvent event) {
        try {
            PtSyntheticTraveller ptSyntheticTraveller = ptSyntheticTravellerMap.get(event.getPersonId());
            ptSyntheticTraveller.setArrivalTime(event.getTime());
        } catch (Exception e) {}
    }

}

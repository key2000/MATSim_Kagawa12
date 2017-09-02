package org.matsim.munichArea.outputCreation.transitSkim;


import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.*;
import org.matsim.api.core.v01.events.handler.*;
import org.matsim.munichArea.configMatsim.createDemandPt.PtSyntheticTraveller;

import java.util.Map;

/**
 * Created by carlloga on 3/2/17.
 */
public class ODTripAnalyzer implements ActivityEndEventHandler, ActivityStartEventHandler, PersonEntersVehicleEventHandler,
        PersonLeavesVehicleEventHandler {

    private Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap;


    public ODTripAnalyzer(Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap) {
        this.ptSyntheticTravellerMap = ptSyntheticTravellerMap;
    }


    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        try {
            PtSyntheticTraveller ptSyntheticTraveller = ptSyntheticTravellerMap.get(event.getPersonId());
            if (event.getActType().equals("home")) ptSyntheticTraveller.setDepartureTime(event.getTime());

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

            //person does not leave the vehicle, then this event does not happen.
            if(ptSyntheticTraveller.getAlightingMap().get(ptSyntheticTraveller.getAlightingMap().keySet().size()-1)==null) {
                System.out.println("alights event size is zero");
            }

        } catch (Exception e) {}
    }

    public void handleEvent(ActivityStartEvent event) {
        try {
            PtSyntheticTraveller ptSyntheticTraveller = ptSyntheticTravellerMap.get(event.getPersonId());
            if (event.getActType().equals("work")) ptSyntheticTraveller.setArrivalTime(event.getTime());
        } catch (Exception e) {}
    }

}

package org.matsim.munichArea.outputCreation;


import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.internal.HasPersonId;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by carlloga on 3/2/17.
 */
public class ODTripAnalyzer implements PersonDepartureEventHandler, PersonArrivalEventHandler {

    private Network network;

    private Map<Integer,Double> departureTimeMap = new HashMap<>();
    private Map<Integer, Double> arrivalTimeMap = new HashMap<>();



    public ODTripAnalyzer(Network network) {
        this.network = network;
    }


    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(PersonDepartureEvent event) {
        try {
            this.departureTimeMap.put(Integer.parseInt(event.getPersonId().toString()), event.getTime());
        } catch (Exception e) {}
    }


    public void handleEvent(PersonArrivalEvent event) {
        try {
            this.arrivalTimeMap.put(Integer.parseInt(event.getPersonId().toString()), event.getTime());
        } catch (Exception e) {}
    }

    public Map<Integer, Double> getDepartureTimeMap(){
        return departureTimeMap;
    }

    public Map<Integer, Double> getArrivalTimeMap() {
        return arrivalTimeMap;
    }
}

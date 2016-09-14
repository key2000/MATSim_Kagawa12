package org.matsim.example;



import org.matsim.api.core.v01.population.Population;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.example.configMatsim.MatsimPopulationCreator;
import org.matsim.example.configMatsim.MatsimRunFromJava;
import org.matsim.example.configMatsim.Zone2ZoneTravelTimeListener;
import org.matsim.example.planCreation.CentroidsToLocations;
import org.matsim.example.planCreation.Location;
import org.matsim.example.outputCreation.travelTimeMatrix;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.valueOf;


/**
 * Created by carlloga on 9/12/2016.
 */
public class MatsimExecuter {

    public static void main (String[] args){


        //createNetwork();

        //create XML file for plans
        //CreatePlanXmlStAX.createPlan();
        //get the list of centroids

        ArrayList<Location> locationList = CentroidsToLocations.readCentroidList();

        Population matsimPopulation = MatsimPopulationCreator.createMatsimPopulation(locationList, 2013, true);

        Map<Tuple<Integer, Integer>,Float> travelTimeMap = new HashMap<Tuple<Integer, Integer>, Float>();

        ArrayList<Location> smallLocationList = new ArrayList<>();

        for (Location location : locationList){
            if (location.getId()<401){
                smallLocationList.add(location);
            }
        }

        travelTimeMap = MatsimRunFromJava.runMatsimToCreateTravelTimes(travelTimeMap, 1 , 1, "./input/merged_network_DHDN_GK4.xml", matsimPopulation, 2013, TransformationFactory.WGS84, 1, "travelTime", "./output", 2, 2, smallLocationList);

        travelTimeMatrix.createOmxSkimMatrix(travelTimeMap,smallLocationList);

        //run MATSim
        //matsimRunFromFile();


    }
}

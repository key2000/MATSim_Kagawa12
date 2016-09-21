package org.matsim.example;




import com.pb.common.matrix.Matrix;
import org.matsim.example.Accessibility;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.example.configMatsim.MatsimPopulationCreator;
import org.matsim.example.configMatsim.MatsimRunFromJava;
import org.matsim.example.configMatsim.Zone2ZoneTravelTimeListener;
import org.matsim.example.planCreation.CentroidsToLocations;
import org.matsim.example.planCreation.Location;
import org.matsim.example.outputCreation.travelTimeMatrix;

import org.matsim.contrib.networkEditor.run.RunNetworkEditor;


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

        //create network from OSM file
        //CreateNetwork.createNetwork();


        //read centroids and get list of locations
        ArrayList<Location> locationList = CentroidsToLocations.readCentroidList();

        //create population
        Population matsimPopulation = MatsimPopulationCreator.createMatsimPopulation(locationList, 2013, true);

        //create an empty map to store travel times
        Matrix autoTravelTime = new Matrix(locationList.size(), locationList.size());

        //make a subset of locations to test the calculation of travel times (ONLY FOR TESTING)
        /*ArrayList<Location> smallLocationList = new ArrayList<>();

        for (Location location : locationList){
            if (location.getId()<5429){
                smallLocationList.add(location);
            }
        }*/

        //get travel times nad/or run Matsim
        autoTravelTime = MatsimRunFromJava.runMatsimToCreateTravelTimes(autoTravelTime, 1 , 1,
                                                                    "./input/studyNetwork.xml", matsimPopulation, 2013,
                                                                    TransformationFactory.WGS84, 10, "travelTime",
                                                                    "./output", /*1, 2,*/ locationList);

        //store the map in omx file
        //travelTimeMatrix.createOmxSkimMatrix(autoTravelTime,locationList);

        //System.out.println("travel time matrix written");

        //read omx files and calculate accessibility
//        Accessibility acc = new Accessibility();
//        acc.calculateAccessibility(locationList);
//        acc.calculateTravelTimesToZone(locationList, 1989);
//        acc.printAccessibility(locationList);

        //run MATSim from file configs
        //matsimRunFromFile();


    }
}

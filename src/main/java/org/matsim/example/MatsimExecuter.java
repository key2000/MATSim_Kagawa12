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



        //make a subset of locations to test the calculation of travel times (ONLY FOR TESTING)
        ArrayList<Location> smallLocationList = new ArrayList<>();
//
//        for (Location location : locationList){
//            if (location.getId()<100){
//                smallLocationList.add(location);
//            }
//        }
//
//        locationList = smallLocationList;

        //create an empty map to store travel times
        Matrix autoTravelTime = new Matrix(locationList.size(), locationList.size());

        //get travel times and run Matsim
        autoTravelTime = MatsimRunFromJava.runMatsimToCreateTravelTimes(autoTravelTime, 8*60*60 , 1,
                                                                    "./input/studyNetwork.xml", matsimPopulation, 2013,
                                                                    TransformationFactory.WGS84, 5, "travelTime",
                                                                    "./output" /*,1, 2*/, locationList);

        //store the map in omx file
        travelTimeMatrix.createOmxSkimMatrix(autoTravelTime,locationList);

        //read omx files and calculate accessibility
        Accessibility acc = new Accessibility();
        acc.calculateAccessibility(locationList);
        acc.calculateTravelTimesToZone(locationList, 1989);
        acc.printAccessibility(locationList);

        //run MATSim from file configs
//        matsimRunFromFile();

        //generate an animation using the OTFVIS extension
        //program arguments
        String arguments[] = new String[5];
        arguments[1]="C:/Models/AmberImplementation/output/travelTime_2013.output_events.xml.gz";
        arguments[2]="C:/Models/AmberImplementation/output/travelTime_2013.output_network.xml.gz";
        arguments[3]="C:/Models/AmberImplementation/visualization.mvi";
        arguments[4]="300";
        //run the conversion
//        org.matsim.contrib.otfvis.OTFVis.convert(arguments);
        //run the visualization
//        org.matsim.contrib.otfvis.OTFVis.playMVI(arguments[3]);

    }
}

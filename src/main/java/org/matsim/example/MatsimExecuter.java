package org.matsim.example;




import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
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


import java.io.File;
import java.util.*;

import static java.lang.Integer.highestOneBit;
import static java.lang.Integer.valueOf;


/**
 * Created by carlloga on 9/12/2016.
 */
public class MatsimExecuter {

    public static ResourceBundle munich;

    public static void main (String[] args){

        File propFile = new File("munich.properties");
        munich = ResourceUtil.getPropertyBundle(propFile);

        boolean createNetwork = ResourceUtil.getBooleanProperty(munich,"create.network");
        boolean runMatsim = ResourceUtil.getBooleanProperty(munich,"run.matsim");
        boolean getTravelTimes = ResourceUtil.getBooleanProperty(munich,"get.travel.times");
        boolean analyzeAccessibility = ResourceUtil.getBooleanProperty(munich,"analyze.accessibility");
        boolean visualize = ResourceUtil.getBooleanProperty(munich,"run.oftvis");

        //create network from OSM file
        if (createNetwork) CreateNetwork.createNetwork();


        //read centroids and get list of locations
        ArrayList<Location> locationList = CentroidsToLocations.readCentroidList();

        //make a subset of locations to test the calculation of travel times (ONLY FOR TESTING)
//        ArrayList<Location> smallLocationList = new ArrayList<>();
//        for (Location location : locationList){
//            if (location.getId()<100){
//                smallLocationList.add(location);
//            }
//        }
//
//        locationList = smallLocationList;

        int iterations = Integer.parseInt(munich.getString("last.iteration"));
        int year = Integer.parseInt(munich.getString("simulation.year"));
        int hourOfDay = Integer.parseInt(munich.getString("hour.of.day"));
        String networkFile = munich.getString("network.folder")+munich.getString("xml.network.file");
        String simulationName = munich.getString("simulation.name");
        String outputFolder = munich.getString("output.folder");
        if (runMatsim) {



            //create population
            Population matsimPopulation = MatsimPopulationCreator.createMatsimPopulation(locationList, 2013, true);
            //create an empty map to store travel times
            Matrix autoTravelTime = new Matrix(locationList.size(), locationList.size());


            //get travel times and run Matsim
            autoTravelTime = MatsimRunFromJava.runMatsimToCreateTravelTimes(autoTravelTime, hourOfDay * 60 * 60, 1,
                    networkFile, matsimPopulation, year,
                    TransformationFactory.WGS84, iterations, simulationName,
                    outputFolder /*,1, 2*/, locationList,getTravelTimes);

            //store the map in omx file
            if (getTravelTimes) travelTimeMatrix.createOmxSkimMatrix(autoTravelTime, locationList);
        }

        //read omx files and calculate accessibility
        if (analyzeAccessibility) {
            Accessibility acc = new Accessibility();
            acc.calculateAccessibility(locationList);
            acc.calculateTravelTimesToZone(locationList, 1989);
            acc.printAccessibility(locationList);
        }

        //run MATSim from file configs
//        matsimRunFromFile();

        //generate an animation using the OTFVIS extension

        if (visualize) {
            //program arguments
            String arguments[] = new String[5];
            arguments[1] = outputFolder + simulationName + "_" + year + ".output_events.xml.gz";
            arguments[2] = outputFolder + simulationName + "_" + year + ".output_network.xml.gz";
            arguments[3] = munich.getString("output.mvi.file");
            arguments[4] = munich.getString("seconds.frame");
            //run the conversion
            org.matsim.contrib.otfvis.OTFVis.convert(arguments);
            //run the visualization
            org.matsim.contrib.otfvis.OTFVis.playMVI(arguments[3]);
        }
    }
}

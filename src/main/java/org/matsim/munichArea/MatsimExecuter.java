package org.matsim.munichArea;

import com.pb.common.matrix.Matrix;
import com.pb.common.util.ResourceUtil;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.munichArea.configMatsim.createDemand.MatsimGravityModel;
import org.matsim.munichArea.configMatsim.createDemand.MatsimPopulationCreator;
import org.matsim.munichArea.configMatsim.MatsimRunFromJava;
import org.matsim.munichArea.configMatsim.createDemand.PtSyntheticTraveller;
import org.matsim.munichArea.outputCreation.EuclideanDistanceCalculator;
import org.matsim.munichArea.outputCreation.PtEventHandler;
import org.matsim.munichArea.planCreation.CentroidsToLocations;
import org.matsim.munichArea.planCreation.Location;
import org.matsim.munichArea.outputCreation.TravelTimeMatrix;


import java.io.File;
import java.util.*;

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
        boolean runGravityModel = ResourceUtil.getBooleanProperty(munich, "run.gravity.model");
        boolean autoSkims = ResourceUtil.getBooleanProperty(munich,"skim.auto.times");
        boolean ptSkimsFromEvents = ResourceUtil.getBooleanProperty(munich,"skim.pt.events");
        boolean eucliddistSkims = ResourceUtil.getBooleanProperty(munich,"skim.eucliddist" );
        boolean analyzeAccessibility = ResourceUtil.getBooleanProperty(munich,"analyze.accessibility");
        boolean visualize = ResourceUtil.getBooleanProperty(munich,"run.oftvis");
        String networkFile = munich.getString("network.folder")+munich.getString("xml.network.file");
        String scheduleFile = munich.getString("network.folder")+munich.getString("schedule.file");
        String vehicleFile = munich.getString("network.folder")+munich.getString("vehicle.file");

        //create network from OSM file
        if (createNetwork) CreateNetwork.createNetwork();

        //read centroids and get list of locations
        ArrayList<Location> locationList = CentroidsToLocations.readCentroidList();

        //get arrays of parameters
       double[] tripScalingFactorVector = ResourceUtil.getDoubleArray(munich, "trip.scaling.factor");
       int[] lastIterationVector =   ResourceUtil.getIntegerArray(munich, "last.iteration");

        if (runMatsim) {
            for (int iterations : lastIterationVector)
                for (double tripScalingFactor : tripScalingFactorVector) {
                    double flowCapacityFactor = tripScalingFactor;
                    System.out.println("Starting MATSim simulation. Sampling factor = " + tripScalingFactor);
                    double storageCapacityFactor = tripScalingFactor;
                    int year = Integer.parseInt(munich.getString("simulation.year"));
                    int hourOfDay = Integer.parseInt(munich.getString("hour.of.day"));
                    String simulationName = String.format("TF%.2fCF%.2fSF%.2fIT%d", tripScalingFactor, flowCapacityFactor, storageCapacityFactor, iterations);
                    simulationName += munich.getString("simulation.name");
                    String outputFolder = munich.getString("output.folder") + simulationName;
                    String omxFileName = munich.getString("output.skim.file") + simulationName + ".omx";



                    //run gravity model to get HBW trips
                    if (runGravityModel) MatsimGravityModel.createMatsimPopulation(locationList, 2013, false, tripScalingFactor);

                    //create population
                    MatsimPopulationCreator matsimPopulationCreator = new MatsimPopulationCreator();
                    matsimPopulationCreator.createMatsimPopulation(locationList, 2013, true, tripScalingFactor);
                    Population matsimPopulation = matsimPopulationCreator.getMatsimPopulation();


                    //create empty matrices and map of pt-travellers for skims
                    Matrix autoTravelTime = new Matrix(locationList.size(), locationList.size());
                    Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap = matsimPopulationCreator.getPtSyntheticTravellerMap();
                    Matrix transitTotalTime = new Matrix(locationList.size(), locationList.size());
                    Matrix transitInTime = new Matrix(locationList.size(), locationList.size());
                    Matrix transitTransfers = new Matrix(locationList.size(), locationList.size());




                    //get travel times and run Matsim
                    //TODO need to improve this part of the code
                    MatsimRunFromJava matsimRunner = new MatsimRunFromJava();
                    autoTravelTime = matsimRunner.runMatsim(autoTravelTime, hourOfDay * 60 * 60, 1,
                                networkFile, matsimPopulation, year,
                                TransformationFactory.WGS84, iterations, simulationName,
                                outputFolder, flowCapacityFactor, storageCapacityFactor, locationList, autoSkims, scheduleFile, vehicleFile);

                    //store the map in omx file
                    if (autoSkims) {
                        TravelTimeMatrix.createOmxSkimMatrix(autoTravelTime, locationList, omxFileName);
                        if (analyzeAccessibility) {
                            Accessibility acc = new Accessibility();
                            acc.calculateAccessibility(locationList);
                            acc.calculateTravelTimesToZone(locationList, 1989);
                            acc.printAccessibility(locationList);
                        }
                    }

                    //read omx files and calculate accessibility


                    //visualization
                    if (visualize) {
                        //program arguments
                        String arguments[] = new String[5];
                        arguments[1] = outputFolder + "/" + simulationName + "_" + year + ".output_events.xml.gz";
                        arguments[2] = outputFolder +"/" + simulationName + "_" + year + ".output_network.xml.gz";
                        arguments[3] = munich.getString("output.mvi.file");
                        arguments[4] = munich.getString("seconds.frame");
                        //run the conversion
                        org.matsim.contrib.otfvis.OTFVis.convert(arguments);
                        //run the visualization
                        org.matsim.contrib.otfvis.OTFVis.playMVI(arguments[3]);
                    }

                    if (ptSkimsFromEvents) {
                        String eventFile = outputFolder + "/" + simulationName + "_" + year + ".output_events.xml.gz";
                        PtEventHandler ptEH = new PtEventHandler();

                        ptEH.runPtEventAnalyzer(eventFile, ptSyntheticTravellerMap);
                        transitTotalTime = ptEH.ptTotalTime(ptSyntheticTravellerMap, transitTotalTime);
                        transitInTime = ptEH.ptInTransitTime(ptSyntheticTravellerMap, transitInTime);
                        transitTransfers = ptEH.ptTransfers(ptSyntheticTravellerMap, transitTransfers);

                        String omxPtFileName = munich.getString("pt.total.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(transitTotalTime, locationList, omxPtFileName);
                        omxPtFileName = munich.getString("pt.in.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(transitInTime, locationList, omxPtFileName);
                        omxPtFileName = munich.getString("pt.transfer.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(transitTransfers, locationList, omxPtFileName);

                    }

                    if (eucliddistSkims){
                        EuclideanDistanceCalculator edc = new EuclideanDistanceCalculator();
                        Matrix euclideanDistanceMatrix = edc.createEuclideanDistanceMatrix(locationList);
                        String omxDistFileName = munich.getString("skim.eucliddist.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(euclideanDistanceMatrix, locationList, omxDistFileName);
                    }

                }
        }




        //run MATSim from file configs
//        matsimRunFromFile();




    }
}

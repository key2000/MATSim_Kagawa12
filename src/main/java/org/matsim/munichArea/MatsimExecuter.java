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
import org.matsim.munichArea.configMatsim.createDemand.ReadZonesServedByTransit;
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

    public static void main(String[] args) {

        File propFile = new File("munich.properties");
        munich = ResourceUtil.getPropertyBundle(propFile);

        boolean createNetwork = ResourceUtil.getBooleanProperty(munich, "create.network");
        boolean runMatsim = ResourceUtil.getBooleanProperty(munich, "run.matsim");
        boolean runGravityModel = ResourceUtil.getBooleanProperty(munich, "run.gravity.model");
        boolean autoSkims = ResourceUtil.getBooleanProperty(munich, "skim.auto.times");
        boolean ptSkimsFromEvents = ResourceUtil.getBooleanProperty(munich, "skim.pt.events");
        boolean eucliddistSkims = ResourceUtil.getBooleanProperty(munich, "skim.eucliddist");
        boolean analyzeAccessibility = ResourceUtil.getBooleanProperty(munich, "analyze.accessibility");
        boolean visualize = ResourceUtil.getBooleanProperty(munich, "run.oftvis");
        String networkFile = munich.getString("network.folder") + munich.getString("xml.network.file");
        String scheduleFile = munich.getString("network.folder") + munich.getString("schedule.file");
        String vehicleFile = munich.getString("network.folder") + munich.getString("vehicle.file");
        String simulationName = munich.getString("simulation.name");
        int year = Integer.parseInt(munich.getString("simulation.year"));
        int hourOfDay = Integer.parseInt(munich.getString("hour.of.day"));

        //create network from OSM file
        if (createNetwork) CreateNetwork.createNetwork();

        //read centroids and get list of locations
        CentroidsToLocations centroidsToLocations = new CentroidsToLocations();
        ArrayList<Location> locationList = centroidsToLocations.readCentroidList();

        ReadZonesServedByTransit servedZoneReader = new ReadZonesServedByTransit();
        ArrayList<Location> servedZoneList = servedZoneReader.readZonesServedByTransit(locationList);

        //get arrays of parameters for single runs
        double[] tripScalingFactorVector = ResourceUtil.getDoubleArray(munich, "trip.scaling.factor");
        int[] lastIterationVector = ResourceUtil.getIntegerArray(munich, "last.iteration");

        //initialize matrices
        Matrix autoTravelTime = new Matrix(locationList.size(), locationList.size());
        Matrix transitTotalTime = new Matrix(locationList.size(), locationList.size());
        transitTotalTime.fill(-1F);
        Matrix transitInTime = new Matrix(locationList.size(), locationList.size());
        transitInTime.fill(-1F);
        Matrix transitTransfers = new Matrix(locationList.size(), locationList.size());
        transitTransfers.fill(-1F);

        if (runMatsim) {
            for (int iterations : lastIterationVector) //loop iteration vector
                for (double tripScalingFactor : tripScalingFactorVector) {  //loop trip Scaling
                    double flowCapacityFactor = tripScalingFactor;
                    System.out.println("Starting MATSim simulation. Sampling factor = " + tripScalingFactor);
                    double storageCapacityFactor = tripScalingFactor;
                    //update simulation name
                    String singleRunName = String.format("TF%.2fCF%.2fSF%.2fIT%d", tripScalingFactor, flowCapacityFactor, storageCapacityFactor, iterations) + simulationName;
                    String outputFolder = munich.getString("output.folder") + singleRunName;

                    int maxSubRuns = 1;
                    int min;
                    int max = 0;

                    //start new loop
                    if (ptSkimsFromEvents) {
                        maxSubRuns = 60;

                    }
                    for (int subRun = 0; subRun < maxSubRuns; subRun++) {

                        min = max;
                        max = (int) (Math.sqrt(Math.pow(servedZoneList.size(), 2)/ maxSubRuns + Math.pow(min, 2)));

                        max = Math.min(max, servedZoneList.size());

                        ArrayList<Location> shortServedZoneList = new ArrayList<>();
                        shortServedZoneList.addAll(servedZoneList.subList(min, max));

                        System.out.println("sub-iteration: " + subRun  );
                        System.out.println("getting PT skim matrix between zone " + min + " and zone " + max  + " which count a total of " + shortServedZoneList.size());


                        //run gravity model to get HBW trips
                        if (runGravityModel)
                            MatsimGravityModel.createMatsimPopulation(locationList, 2013, false, tripScalingFactor);

                        //create population
                        MatsimPopulationCreator matsimPopulationCreator = new MatsimPopulationCreator();
                        matsimPopulationCreator.createMatsimPopulation(locationList, 2013, true, tripScalingFactor);
                        if (ptSkimsFromEvents) {
                            matsimPopulationCreator.createSyntheticPtPopulation(servedZoneList, shortServedZoneList);
                        }



                        Population matsimPopulation = matsimPopulationCreator.getMatsimPopulation();
                        Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap;


                        ptSyntheticTravellerMap = matsimPopulationCreator.getPtSyntheticTravellerMap();
                        System.out.println("PTSynthetic trips: " + ptSyntheticTravellerMap.size());
                        //update map of pt-travellers for skims


                        //get travel times and run Matsim
                        //TODO need to improve this part of the code
                        MatsimRunFromJava matsimRunner = new MatsimRunFromJava();
                        autoTravelTime = matsimRunner.runMatsim(autoTravelTime, hourOfDay * 60 * 60, 1,
                                networkFile, matsimPopulation, year,
                                TransformationFactory.WGS84, iterations, simulationName,
                                outputFolder, flowCapacityFactor, storageCapacityFactor, locationList, autoSkims, scheduleFile, vehicleFile);


                        //visualization
                        if (visualize) {
                            //program arguments
                            String arguments[] = new String[5];
                            arguments[1] = outputFolder + "/" + simulationName + "_" + year + ".output_events.xml.gz";
                            arguments[2] = outputFolder + "/" + simulationName + "_" + year + ".output_network.xml.gz";
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
                        }

                    }
                    //end of the new loop

                    if (eucliddistSkims) {
                        EuclideanDistanceCalculator edc = new EuclideanDistanceCalculator();
                        Matrix euclideanDistanceMatrix = edc.createEuclideanDistanceMatrix(locationList);
                        String omxDistFileName = munich.getString("skim.eucliddist.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(euclideanDistanceMatrix, locationList, omxDistFileName);
                    }


                    if (autoSkims) {
                        String omxFileName = munich.getString("output.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(autoTravelTime, locationList, omxFileName);
                    }

                    if (ptSkimsFromEvents) {
                        String omxPtFileName = munich.getString("pt.total.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(transitTotalTime, locationList, omxPtFileName);
                        omxPtFileName = munich.getString("pt.in.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(transitInTime, locationList, omxPtFileName);
                        omxPtFileName = munich.getString("pt.transfer.skim.file") + simulationName + ".omx";
                        TravelTimeMatrix.createOmxSkimMatrix(transitTransfers, locationList, omxPtFileName);
                    }
                }

        }

        if (analyzeAccessibility) {

            String omxFile = munich.getString("omx.access.calc") + ".omx";
            Accessibility acc = new Accessibility(omxFile);
            acc.calculateAccessibility(locationList);
            acc.calculateTravelTimesToZone(locationList, 3627);
            acc.printAccessibility(locationList);
        }


        //run MATSim from file configs
//        matsimRunFromFile();


    }


}

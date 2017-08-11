package org.matsim.munichArea.configMatsim.createDemandPt;

/**
 * Created by carlloga on 9/14/2016.
 */

import com.pb.common.matrix.Matrix;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.munichArea.configMatsim.planCreation.Location;
import org.matsim.munichArea.outputCreation.accessibilityCalculator.Accessibility;

import java.util.*;
import java.util.Map.Entry;

import static org.matsim.munichArea.MatsimExecuter_DistMatInitTest01.rb;


/**
 * discontinued
 */
public class MatsimPopulationCreator_DistMatInitTest01 {

    public static boolean ASC = true;
    public static boolean DESC = false;

    private Population matsimPopulation;
    private Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap;

    public void createMatsimPopulation(ArrayList<Location> locationList, /*HouseholdDataManager householdDataManager,*/ int year,
                                                    /*Map<Integer,SimpleFeature>zoneFeatureMap, String crs, */ boolean writePopulation,
                                       double tripScalingFactor) {


        //in this class, purpose is to obtain teh travel distance and time between all zones
        //therefore actual population is not needed. Just create one trip between one OD -> tmpPopulation.xml.


        // MATSim "infrastructure"
        Config matsimConfig = ConfigUtils.createConfig();
        Scenario matsimScenario = ScenarioUtils.createScenario(matsimConfig);

        Network matsimNetwork = matsimScenario.getNetwork();
        matsimPopulation = matsimScenario.getPopulation();
        PopulationFactory matsimPopulationFactory = matsimPopulation.getFactory();

        ArrayList<Location> destList = locationList;
        ArrayList<Location> origList = locationList;

        int personId = 0;





        Random rnd = new Random();
        double alpha = 1.5;
        double g = 1;
        double minTravelTime = 3;

        int totalTripsAllDestinations = 0;


        //start loop to generate trips to work
        //one person is assigned for all OD pair
        for (Location destLoc : destList) {

            for (Location origLoc : origList) {

                Person matsimPerson =
                        matsimPopulationFactory.createPerson(Id.create(personId, Person.class));
                matsimPopulation.addPerson(matsimPerson);
                personId++;

                Plan matsimPlan = matsimPopulationFactory.createPlan();
                matsimPerson.addPlan(matsimPlan);

                //SimpleFeature homeFeature = zoneFeatureMap.get(origLoc.getId());
                Coord homeCoordinates = new Coord(origLoc.getX() + origLoc.getSize() * (Math.random() - 0.5), origLoc.getY() + origLoc.getSize() * (Math.random() - 0.5));

                Activity activity1 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);
                //randomly between 7 and 9 AM
                double time = 8 * 60 * 60 + rnd.nextGaussian() * 60 * 60;
                activity1.setEndTime(Math.max(4 * 60 * 60, Math.min(time, 12 * 60 * 60)));
                matsimPlan.addActivity(activity1);
                matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));

                Coord workCoordinates = new Coord(destLoc.getX() + destLoc.getSize() * (Math.random() - 0.5), destLoc.getY() + destLoc.getSize() * (Math.random() - 0.5));
                Activity activity2 = matsimPopulationFactory.createActivityFromCoord("work", workCoordinates);
                //randomly between 4 and 8 PM
                time = 17 * 60 * 60 + rnd.nextGaussian() * 60 * 60;
                activity2.setEndTime(Math.max(14 * 60 * 60, Math.min(time, 22 * 60 * 60)));
                matsimPlan.addActivity(activity2);
                matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));

                Activity activity3 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);
                matsimPlan.addActivity(activity3);

            }
        }

        System.out.println("The total number of trips by car is = " + totalTripsAllDestinations);

        if (writePopulation) {
            MatsimWriter popWriter = new PopulationWriter(matsimPopulation, matsimNetwork);
            popWriter.write("./input/temp/population_initZoneDist.xml");
        }


/*
                Map<Integer, Double> origZoneWeight = new HashMap<>();
//            double[] origRate = new double[origList.size()];
            double sumOrigRates = 0;
            //first loop to calculate weights
            for (Location origLoc : origList) {
                double weight;
//                double travelTime = acc.getAutoTravelTime(origLoc.getId(), destLoc.getId(), autoTravelTime);
//                just assume to be one to make the uniform demand
                double travelTime = 1;

                if (travelTime < minTravelTime) {
//                    origRate[origLoc.getId() - 1] = (origLoc.getPopulation() * g / Math.pow(minTravelTime, alpha));
                    weight = (origLoc.getPopulation() * g / Math.pow(minTravelTime, alpha));


                } else {
//                    origRate[origLoc.getId() - 1] = (origLoc.getPopulation() * g / Math.pow(travelTime, alpha));
                    weight = (origLoc.getPopulation() * g / Math.pow(travelTime, alpha));

                }


//                sumOrigRates += origRate[origLoc.getId() - 1];
                sumOrigRates += weight;
                origZoneWeight.put(origLoc.getId(), weight);
            }


            float carShare = Float.parseFloat(rb.getString("car.modal.share"));


            //order the map according to decreasing weights
            origZoneWeight = sortByComparator(origZoneWeight, DESC);

            //second loop to normalize weights and calculate integer trips
            Map<Integer, Integer> origZoneTrips = new HashMap<>();
            List<Integer> origOrderedList = new ArrayList<>(origZoneWeight.keySet());
            int totalTripsToDestination = Math.round(destLoc.getEmployment() * carShare);
            int tripsAssigned = 0;

            for (int loc : origOrderedList) {
                int trips = (int) Math.ceil(totalTripsToDestination * origZoneWeight.get(loc) / sumOrigRates);
                if (tripsAssigned <= totalTripsToDestination) {
                    tripsAssigned += trips;
                } else {
                    trips = 0;
                }
                origZoneTrips.put(loc, trips);
            }

            totalTripsAllDestinations += tripsAssigned;
*/

    }

    public void createSyntheticPtPopulation(ArrayList<Location> servedZonesList, ArrayList<Location> shortServedZonesList) {

        //public transportation demand to get skims

        int personId = 10000000;

        TransitDemandForSkim tdSkims = new TransitDemandForSkim();
        ptSyntheticTravellerMap = tdSkims.createDemandForSkims(servedZonesList, shortServedZonesList, personId, matsimPopulation);


        //add demand to munichArea transit line
        float transitShare = Float.parseFloat(rb.getString("transit.modal.share"));

        //int numberOfS1travelers = (int) (500 * transitShare);

    }


    public Population getMatsimPopulation() {
        return matsimPopulation;
    }

    public Map<Id, PtSyntheticTraveller> getPtSyntheticTravellerMap() {
        return ptSyntheticTravellerMap;
    }

    private static Map<Integer, Double> sortByComparator(Map<Integer, Double> unsortMap, final boolean order) {

        List<Entry<Integer, Double>> list = new LinkedList<>(unsortMap.entrySet());
        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<Integer, Double>>() {
            public int compare(Entry<Integer, Double> o1,
                               Entry<Integer, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });
        // Maintaining insertion order with the help of LinkedList
        Map<Integer, Double> sortedMap = new LinkedHashMap<>();
        for (Entry<Integer, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}

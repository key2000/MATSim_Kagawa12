package org.matsim.example.configMatsim;

/**
 * Created by carlloga on 9/14/2016.
 */

import java.io.FileWriter;

import java.util.*;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.example.planCreation.Location;
import org.matsim.utils.leastcostpathtree.LeastCostPathTree;
import org.matsim.vehicles.Vehicle;
import org.opengis.feature.simple.SimpleFeature;



/**
 * @author dziemke
 */
public class Zone2ZoneTravelTimeListener implements IterationEndsListener {
    private final static Logger log = Logger.getLogger(Zone2ZoneTravelTimeListener.class);

    private Controler controler;
    private Network network;
    private int finalIteration;
    //private Map<Integer, SimpleFeature> zoneFeatureMap;
    private ArrayList<Location> locationList;
    private int departureTime;
    private int numberOfCalcPoints;
    //	private CoordinateTransformation ct;
    private Map<Tuple<Integer, Integer>, Float> travelTimesMap;


    public Zone2ZoneTravelTimeListener(Controler controler, Network network, int finalIteration, /*Map<Integer, SimpleFeature> zoneFeatureMap*/ ArrayList<Location> locationList,
                                       int timeOfDay, int numberOfCalcPoints, //CoordinateTransformation ct,
                                       Map<Tuple<Integer, Integer>, Float> travelTimesMap) {
        this.controler = controler;
        this.network = network;
        this.finalIteration = finalIteration;
        //this.zoneFeatureMap = zoneFeatureMap;
        this.locationList = locationList;
        this.departureTime = timeOfDay;
        this.numberOfCalcPoints = numberOfCalcPoints;
//		this.ct = ct;
        this.travelTimesMap = travelTimesMap;
    }


    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        if (event.getIteration() == this.finalIteration) {

            log.info("Starting to calculate average zone-to-zone travel times based on MATSim.");

            TravelTime travelTime = controler.getLinkTravelTimes();
            TravelDisutility travelDisutility = controler.getTravelDisutilityFactory().createTravelDisutility(travelTime);
//			TravelDisutility travelTimeAsTravelDisutility = new MyTravelTimeDisutility(controler.getLinkTravelTimes());

            LeastCostPathTree leastCoastPathTree = new LeastCostPathTree(travelTime, travelDisutility);
//			Dijkstra dijkstra = new Dijkstra(network, travelTimeAsTravelDisutility, travelTime);

            //Map<Integer, List<Node>> zoneCalculationNodesMap = new HashMap<>();
            //simplified to one node per location
            Map<Integer, Node> zoneCalculationNodesMap = new HashMap<>();

            //get the nodes of each location
            //for (int zoneId : zoneFeatureMap.keySet()) {
            for (Location loc : locationList){
//                for (int i = 0; i < numberOfCalcPoints; i++) { // several points in a given origin zone
                    //SimpleFeature originFeature = zoneFeatureMap.get(zoneId);
//					Coord originCoord = ct.transform(SiloMatsimUtils.getRandomCoordinateInGeometry(originFeature));
                    //Coord originCoord = SiloMatsimUtils.getRandomCoordinateInGeometry(originFeature);
                    Coord originCoord = new Coord(loc.getX(), loc.getY());
                    Link originLink = NetworkUtils.getNearestLink(network, originCoord);
                    Node originNode = originLink.getFromNode();

                    //if (!zoneCalculationNodesMap.containsKey(zoneId)) {
//                        zoneCalculationNodesMap.put(zoneId, new LinkedList<Node>());
//                zoneCalculationNodesMap.put(loc.getId(), new LinkedList<Node>());
                zoneCalculationNodesMap.put(loc.getId(),originNode);
                    //}
                //zoneCalculationNodesMap.get(loc.getId()).add(originNode);
//                }
            }

            int counter =0;
            for (Location originZone : locationList) { // going over all origin zones

                //for (Node originNode : zoneCalculationNodesMap.get(originZoneId)) { // several points in a given origin zone
                    // Run Dijkstra for originNode
                    Node originNode = zoneCalculationNodesMap.get(originZone.getId());
                    leastCoastPathTree.calculate(network, originNode, departureTime);

                    for (Location destinationZone : locationList) { // going over all destination zones

                        Tuple<Integer, Integer> originDestinationRelation = new Tuple<Integer, Integer>(originZone.getId(), destinationZone.getId());

                        if (!travelTimesMap.containsKey(originDestinationRelation)) {
                            travelTimesMap.put(originDestinationRelation, 0.f);
                        }

                        //for (Node destinationNode : zoneCalculationNodesMap.get(destinationZoneId)) {// several points in a given destination zone

                            double arrivalTime = leastCoastPathTree.getTree().get(zoneCalculationNodesMap.get(destinationZone.getId()).getId()).getTime();
                            // congested car travel times in minutes
                            float congestedTravelTimeMin = (float) ((arrivalTime - departureTime) / 60.);
//							System.out.println("congestedTravelTimeMin = " + congestedTravelTimeMin);

                            // following lines form kai/thomas, see Zone2ZoneImpedancesControlerListener
//							// we guess that any value less than 1.2 leads to errors on the UrbanSim side
//							// since ln(0) is not defined or ln(1) = 0 causes trouble as a denominator ...
//							if(congestedTravelTimeMin < 1.2)
//								congestedTravelTimeMin = (float) 1.2;
							//LeastCostPathCalculator.Path path = dijkstra.calcLeastCostPath(originLink.getFromNode(), destinationNode, timeOfDay, null, null);
                            float previousSumTravelTimeMin = travelTimesMap.get(originDestinationRelation);
                            travelTimesMap.put(originDestinationRelation, previousSumTravelTimeMin + congestedTravelTimeMin);
                            counter++;
                            if (counter % 10000 ==0){
                                System.out.println("pairs already calculated = " +  counter);
                            }
//							System.out.println("previousSumTravelTimeMin = " + previousSumTravelTimeMin);
                        //}
                    //}
                }
            }



//            If only one node, this is not needed
            for (Tuple<Integer, Integer> originDestinationRelation : travelTimesMap.keySet()) {
                float sumTravelTimeMin = travelTimesMap.get(originDestinationRelation);
                float averageTravelTimeMin = sumTravelTimeMin / numberOfCalcPoints / numberOfCalcPoints;
                travelTimesMap.put(originDestinationRelation, averageTravelTimeMin);

//				log.info(fipsPuma5Tuple + " -- travel time = " + averageTravelTimeMin);
            }
        }
    }



    // inner class to use travel time as travel disutility
    class MyTravelTimeDisutility implements TravelDisutility {
        TravelTime travelTime;
        public MyTravelTimeDisutility(TravelTime travelTime) {
            this.travelTime = travelTime;
        }


        @Override
        public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
            return travelTime.getLinkTravelTime(link, time, person, vehicle);
        }


        @Override
        public double getLinkMinimumTravelDisutility(Link link) {
            return link.getLength() / link.getFreespeed(); // minimum travel time
        }
    }

    /**
     * @author dziemke
     */
}
package org.matsim.munichArea.configMatsim;

/**
 * Created by carlloga on 9/14/2016. copyed from siloMatsim package in github silo
 */

import java.util.*;

import com.pb.common.matrix.Matrix;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.munichArea.CreateNetwork;
import org.matsim.munichArea.planCreation.Location;
import org.matsim.utils.leastcostpathtree.LeastCostPathTree;
import org.matsim.vehicles.Vehicle;


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
    private Matrix autoTravelTime;


    public Zone2ZoneTravelTimeListener(Controler controler, Network network,
                                       int finalIteration, /*Map<Integer, SimpleFeature> zoneFeatureMap*/
                                       ArrayList<Location> locationList,
                                       int timeOfDay,
                                       int numberOfCalcPoints, //CoordinateTransformation ct,
                                       Matrix autoTravelTime) {
        this.controler = controler;
        this.network = network;
        this.finalIteration = finalIteration;
        //this.zoneFeatureMap = zoneFeatureMap;
        this.locationList = locationList;
        this.departureTime = timeOfDay;
        this.numberOfCalcPoints = numberOfCalcPoints;
//		this.ct = ct;
        this.autoTravelTime = autoTravelTime;
    }


    @Override
    public void notifyIterationEnds(IterationEndsEvent event) {
        if (event.getIteration() == this.finalIteration) {

            log.info("Starting to calculate average zone-to-zone travel times based on MATSim.");

            TravelTime travelTime = controler.getLinkTravelTimes();

            TravelDisutility travelDisutility = controler.getTravelDisutilityFactory().createTravelDisutility(travelTime);


            LeastCostPathTree leastCoastPathTree = new LeastCostPathTree(travelTime, travelDisutility);

            //Map to asign a node to each zone
            Map<Integer, Node> zoneCalculationNodesMap = new HashMap<>();

            //reclean the network will remove all pt links and will make possible getting auto travel times
            NetworkCleaner networkCleaner = new NetworkCleaner();
            networkCleaner.run(network);

            for (Location loc : locationList) {

                Coord originCoord = new Coord(loc.getX(), loc.getY());

                Link originLink = NetworkUtils.getNearestLink(network, originCoord);

                Node originNode = originLink.getFromNode();

                //Get distance from node to centroid, if required
//                Double dist = NetworkUtils.getEuclideanDistance(originCoord, originNode.getCoord());
//                log.info("Zone: " + loc.getId() + " Distance to nearest node: " + dist);

                //if (!zoneCalculationNodesMap.containsKey(zoneId)) {
//                        zoneCalculationNodesMap.put(zoneId, new LinkedList<Node>());
//                zoneCalculationNodesMap.put(loc.getId(), new LinkedList<Node>());

                zoneCalculationNodesMap.put(loc.getId(), originNode);

            }

            int counter = 0;
            for (Location originZone : locationList) { // going over all origin zones

                Node originNode = zoneCalculationNodesMap.get(originZone.getId());

                leastCoastPathTree.calculate(network, originNode, departureTime);

                for (Location destinationZone : locationList) { // going over all destination zones

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
                    //for path
//                    LeastCostPathCalculator.Path path = dijkstra.calcLeastCostPath(zoneCalculationNodesMap.get(destinationZone.getId()),
//                            zoneCalculationNodesMap.get(destinationZone.getId()),
//                            departureTime, null, null);
                    //this is 0 in the current status of
                    //float previousSumTravelTimeMin = travelTimesMap.get(originDestinationRelation);

                    //travelTimesMap.put(originDestinationRelation, previousSumTravelTimeMin + congestedTravelTimeMin);
                    autoTravelTime.setValueAt(originZone.getId(), destinationZone.getId(),congestedTravelTimeMin);
                    counter++;
                    if (counter % 10000 == 0) {
                        log.info("pairs already calculated = " + counter);
                    }
//							System.out.println("previousSumTravelTimeMin = " + previousSumTravelTimeMin);
                    //}
                    //}
                }
            }


//            If only one node, this is not needed
//            for (Tuple<Integer, Integer> originDestinationRelation : travelTimesMap.keySet()) {
//                float sumTravelTimeMin = travelTimesMap.get(originDestinationRelation);
//                float averageTravelTimeMin = sumTravelTimeMin / numberOfCalcPoints / numberOfCalcPoints;
//                travelTimesMap.put(originDestinationRelation, averageTravelTimeMin);

//				log.info(fipsPuma5Tuple + " -- travel time = " + averageTravelTimeMin);
//            }
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
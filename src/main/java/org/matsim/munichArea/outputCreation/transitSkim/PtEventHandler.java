package org.matsim.munichArea.outputCreation.transitSkim;

import com.pb.common.matrix.Matrix;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.munichArea.configMatsim.createDemandPt.PtSyntheticTraveller;

import java.util.Map;

/**
 * Created by carlloga on 3/2/17.
 */
public class PtEventHandler {


    public void runPtEventAnalyzer(String eventsFile, Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap){
        EventsManager eventsManager = EventsUtils.createEventsManager();
        ODTripAnalyzer odTripAnalyzer = new ODTripAnalyzer(ptSyntheticTravellerMap);
        eventsManager.addHandler(odTripAnalyzer);
        new MatsimEventsReader(eventsManager).readFile(eventsFile);
    }

    public Matrix ptTotalTime(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix transitTravelTime) {

        //transitTravelTime.fill(-1F);

        System.out.println("Number of PT synthetic trips: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){
            float tt = (float) (( ptst.getArrivalTime() - ptst.getDepartureTime())/60);

            if (tt > 300) tt=-1F;

            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);
            transitTravelTime.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), tt);
            transitTravelTime.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), tt);
        }

        return transitTravelTime;
    }

    public Matrix ptInTransitTime(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix transitTravelTime) {

        //transitTravelTime.fill(-1F);

        System.out.println("Number of PT synthetic trips: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){

            //<System.out.println(ptst.getOrigLoc().getId() + "-" + tt);

            //if(!ptst.getBoardingMap().isEmpty()) {
            //this is not essential solution, but just avoid the empty alighting travel
            if((!ptst.getBoardingMap().isEmpty()) && (!ptst.getAlightingMap().isEmpty())) {

                //a person on board does not alight the vehicle, then end time comes to be null
                //i do not know why this happens

/*
                if(ptst.getAlightingMap().get(ptst.getAlightingMap().keySet().size() - 1) == null) {
                    System.out.println("null pointer exception 01");
                }
*/

                double end = ptst.getAlightingMap().get(ptst.getAlightingMap().keySet().size() - 1);

                double start = ptst.getBoardingMap().get(0);

                float tt = (float) ((end - start)/60);

                if (tt > 300) {tt=-1F;}

                transitTravelTime.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), tt);
                transitTravelTime.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), tt);
            }

        }

        return transitTravelTime;
    }

    public Matrix ptTransfers(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix transfers) {

       // transfers.fill(-1F);

        System.out.println("Number of PT synthetic trips: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){

            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);

            float numberOfTransfers = (float) ptst.getTransfers();

            transfers.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), numberOfTransfers);
            transfers.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), numberOfTransfers);

        }

        return transfers;
    }

    public Matrix inVehicleTt(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix ptInVehicleTt) {

        // transfers.fill(-1F);

        System.out.println("Number of PT synthetic trips: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){

            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);

            float time = (float) ptst.getInVehicleTime()/60;

            ptInVehicleTt.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), time);
            ptInVehicleTt.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), time);

        }

        return ptInVehicleTt;
    }

    public Matrix transitAccessTt(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix transitAccessTt) {

        // transfers.fill(-1F);

        System.out.println("Number of PT synthetic trips: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){

            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);
            float time;
            if (ptst.getAccessTimeByWalk() != -1 ){
                time = (float) ptst.getAccessTimeByWalk()/60;
            } else {
                time = -1;
            }



            transitAccessTt.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), time);
            transitAccessTt.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), time);

        }

        return transitAccessTt;
    }

    public Matrix transitEgressTt(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix transitEgressTt) {

        // transfers.fill(-1F);

        System.out.println("Number of PT synthetic trips: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){

            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);
            float time;
            if (ptst.getEgressTimeByWalk() != -1 ){
                time = (float) ptst.getEgressTimeByWalk()/60;
            } else {
                time = -1;
            }

            transitEgressTt.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), time);
            transitEgressTt.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), time);

        }

        return transitEgressTt;
    }







}




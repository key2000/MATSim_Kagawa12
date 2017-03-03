package org.matsim.munichArea.outputCreation;

import com.pb.common.matrix.Matrix;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.munichArea.configMatsim.createDemand.PtSyntheticTraveller;
import org.matsim.munichArea.configMatsim.createDemand.TransitDemandForSkim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.matsim.munichArea.MatsimExecuter.munich;

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

            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);

            if(!ptst.getBoardingMap().isEmpty()) {

                double end = ptst.getAlightingMap().get(ptst.getAlightingMap().keySet().size() - 1);

                double start = ptst.getBoardingMap().get(0);

                float tt = (float) ((end - start)/60);

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

            float inVehicleTt = (float) ptst.getInVehicleTime();

            ptInVehicleTt.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), inVehicleTt);
            ptInVehicleTt.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), inVehicleTt);

        }

        return ptInVehicleTt;
    }







}




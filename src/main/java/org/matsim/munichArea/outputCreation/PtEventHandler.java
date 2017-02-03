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

    public Matrix ptEventHandler(String eventsFile, Matrix transitTravelTime, Map<Id, PtSyntheticTraveller> ptSyntheticTravellerMap) {

        EventsManager eventsManager = EventsUtils.createEventsManager();
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        //new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);
        TransitDemandForSkim tdSkim = new TransitDemandForSkim();

        ODTripAnalyzer odTripAnalyzer = new ODTripAnalyzer(ptSyntheticTravellerMap);
        eventsManager.addHandler(odTripAnalyzer);
        new MatsimEventsReader(eventsManager).readFile(eventsFile);


        //writeSkimToFile(odTripAnalyzer.getDepartureTimeMap(), odTripAnalyzer.getArrivalTimeMap(), munich.getString("skim.pt.file"));



        return getPtSkimMatrix(ptSyntheticTravellerMap, transitTravelTime);

    }

    private Matrix getPtSkimMatrix(Map<Id,PtSyntheticTraveller> ptSyntheticTravellerMap, Matrix transitTravelTime) {

        transitTravelTime.fill(-1F);

        System.out.println("Analyzing trips of: " + ptSyntheticTravellerMap.size());
        for (PtSyntheticTraveller ptst : ptSyntheticTravellerMap.values()){
            float tt = (float) ( ptst.getArrivalTime() - ptst.getDepartureTime())/60;
            //System.out.println(ptst.getOrigLoc().getId() + "-" + tt);
            transitTravelTime.setValueAt(ptst.getOrigLoc().getId(), ptst.getDestLoc().getId(), tt);
            transitTravelTime.setValueAt(ptst.getDestLoc().getId(), ptst.getOrigLoc().getId(), tt);

        }


        return transitTravelTime;
    }


    static void writeSkimToFile(Map<Integer, Double> departureTimeMap, Map<Integer, Double> arrivalTimeMap, String fileName) {
        BufferedWriter bw = IOUtils.getBufferedWriter(fileName,IOUtils.CHARSET_UTF8,false);
        try {
            bw.write("person,departureTime, arrivalTime");

            for (int person : departureTimeMap.keySet()){

                bw.newLine();
                bw.write(person +"," +  departureTimeMap.get(person) + ","  + arrivalTimeMap.get(person));
            }

            bw.flush();
            bw.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }




}




package org.matsim.munichArea.outputCreation;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

import static org.matsim.munichArea.MatsimExecuter.munich;

/**
 * Created by carlloga on 3/2/17.
 */
public class PtEventHandler {

    public void ptEventHandler(String networkFile, String eventsFile) {

        EventsManager eventsManager = EventsUtils.createEventsManager();
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());

        //new MatsimNetworkReader(scenario.getNetwork()).readFile(networkFile);

        ODTripAnalyzer odTripAnalyzer = new ODTripAnalyzer(scenario.getNetwork());
        eventsManager.addHandler(odTripAnalyzer);
        new MatsimEventsReader(eventsManager).readFile(eventsFile);


        writeSkimToFile(odTripAnalyzer.getDepartureTimeMap(), odTripAnalyzer.getArrivalTimeMap(), munich.getString("skim.pt.file"));


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




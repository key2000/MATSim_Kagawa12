package org.matsim.munichArea.outputCreation.tripDurationAnalyzer;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static org.matsim.munichArea.MatsimExecuter.rb;

/**
 * Created by carlloga on 17.03.2017.
 */
public class AgentTripDurationEventAnalyzer {

    private ResourceBundle rb;

    private Map<Id, Trip> tripMap;

    public AgentTripDurationEventAnalyzer(ResourceBundle rb) {
        this.rb = rb;
        this.tripMap = new HashMap<>();

    }

    public void runAgentTripDurationAnalyzer(String eventsFile) {

        EventsManager eventsManager = EventsUtils.createEventsManager();
        ActivityStartEndHandler eventAnalyzer = new ActivityStartEndHandler(tripMap);
        eventsManager.addHandler(eventAnalyzer);
        new MatsimEventsReader(eventsManager).readFile(eventsFile);

        System.out.println(tripMap.size());
    }

    public void writeListTripDurations(String eventsFile){

        BufferedWriter bw = IOUtils.getBufferedWriter(eventsFile + ".csv");
        try {

            bw.write("id, tripDuration");
            bw.newLine();

        for (Id id : tripMap.keySet()){
            double tripDuration = tripMap.get(id).getDuration();
            double waitingTimeBefore = tripMap.get(id).getWaitingTimeBefore();
            bw.write(id.toString() + "," + tripDuration + "," + waitingTimeBefore);
            bw.newLine();
        }

            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

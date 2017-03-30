package org.matsim.munichArea.outputCreation.tripDurationAnalyzer;

import com.pb.common.util.ResourceUtil;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by carlloga on 17.03.2017.
 */
public class AllModesTripAnalyzer {

    private static ResourceBundle rb;


    //filenames are passed to the application as args and as absolute paths

    public static void main (String[] args){

        File propFile = new File("munich.properties");
        rb = ResourceUtil.getPropertyBundle(propFile);

        for (String eventsFileName : args) {

            AgentTripDurationEventAnalyzer analyzer = new AgentTripDurationEventAnalyzer(rb);
            analyzer.runAgentTripDurationAnalyzer(eventsFileName);
            analyzer.writeListTripDurations(eventsFileName);

        }

    }
}

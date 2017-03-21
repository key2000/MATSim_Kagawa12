package org.matsim.munichArea.outputCreation.tripDurationAnalyzer;

import com.pb.common.util.ResourceUtil;

import java.io.File;
import java.util.ResourceBundle;

/**
 * Created by carlloga on 17.03.2017.
 */
public class AllModesTripAnalyzer {

    private static ResourceBundle rb;

    public static void main (String[] args){

        File propFile = new File("munich.properties");
        rb = ResourceUtil.getPropertyBundle(propFile);

        AgentTripDurationEventAnalyzer analyzer = new AgentTripDurationEventAnalyzer(rb);

        //String eventsFileName = "C:/models/munich/outputTF0,05CF0,05SF0,11IT20TestAVNoAV/TestAV_2016.output_events.xml.gz";
        String eventsFileName = "C:/models/munich/cottbus_robotaxi/output/robotaxi_munich_50000/output_events.xml.gz";
        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
        analyzer.writeListTripDurations(eventsFileName);

    }
}

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

        //cases for trip sampling analysis
//        String eventsFileName = "C:/projects/MATSim/scaling/events/TF0.05CF0.05SF0.05IT20_2016.output_events.xml.gz";
//        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
//        analyzer.writeListTripDurations("C:/projects/MATSim/scaling/events/5");
//
//        analyzer = new AgentTripDurationEventAnalyzer(rb);
//        eventsFileName = "C:/projects/MATSim/scaling/events/TF0.10CF0.10SF0.10IT20_2016.output_events.xml.gz";
//        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
//        analyzer.writeListTripDurations("C:/projects/MATSim/scaling/events/10");
//
//        analyzer = new AgentTripDurationEventAnalyzer(rb);
//        eventsFileName = "C:/projects/MATSim/scaling/events/TF0.50CF0.50SF0.50IT20_2016.output_events.xml.gz";
//        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
//        analyzer.writeListTripDurations("C:/projects/MATSim/scaling/events/50");
//
//        analyzer = new AgentTripDurationEventAnalyzer(rb);
//        eventsFileName = "C:/projects/MATSim/scaling/events/TF1.00CF1.00SF1.00IT20_2016.output_events.xml.gz";
//        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
//        analyzer.writeListTripDurations("C:/projects/MATSim/scaling/events/100");



        String eventsFileName = "C:/models/munich/outputTF0,05CF0,10SF0,18IT20TestAV2/TestAV2_2016.output_events.xml.gz";
        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
        analyzer.writeListTripDurations(eventsFileName);

        analyzer = new AgentTripDurationEventAnalyzer(rb);

        eventsFileName = "C:/models/munich/cottbus_robotaxi/output/robotaxi_munich4/output_events.xml.gz";
        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
        analyzer.writeListTripDurations(eventsFileName);

        analyzer = new AgentTripDurationEventAnalyzer(rb);

        eventsFileName = "C:/models/munich/cottbus_robotaxi/output/robotaxi_munich8/output_events.xml.gz";
        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
        analyzer.writeListTripDurations(eventsFileName);

        analyzer = new AgentTripDurationEventAnalyzer(rb);

        eventsFileName = "C:/models/munich/cottbus_robotaxi/output/robotaxi_munich12/output_events.xml.gz";
        analyzer.runAgentTripDurationAnalyzer(eventsFileName);
        analyzer.writeListTripDurations(eventsFileName);

    }
}

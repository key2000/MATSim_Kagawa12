package org.matsim.munichArea.matsimAvExample;

import org.matsim.contrib.av.robotaxi.run.RunRobotaxiExample;

/**
 * Created by carlloga on 16.03.2017.
 */
public class RunAVExample {

    public static void main (String[] args) {
        String configFileName = "config.xml";
        RunRobotaxiExample.run(configFileName, false);
    }

}

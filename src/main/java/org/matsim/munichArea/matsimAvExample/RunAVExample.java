package org.matsim.munichArea.matsimAvExample;

import org.matsim.contrib.av.robotaxi.run.RunRobotaxiExample;
import org.matsim.contrib.av.robotaxi.vehicles.CreateTaxiVehicles;

/**
 * Created by carlloga on 16.03.2017.
 */
public class RunAVExample {

    public static void main (String[] args) {

        CreateAVDemand createAVDemand = new CreateAVDemand();
        createAVDemand.createAVDemand(0.05F);

        CreateAVs createAVs = new CreateAVs();
        createAVs.createAVs(5000);

        String configFileName = "cottbus_robotaxi/config.xml";
        RunRobotaxiExample.run(configFileName, false);

    }

}

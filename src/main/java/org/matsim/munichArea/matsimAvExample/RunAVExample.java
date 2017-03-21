package org.matsim.munichArea.matsimAvExample;

import org.matsim.contrib.av.robotaxi.run.RunRobotaxiExample;
import org.matsim.contrib.av.robotaxi.vehicles.CreateTaxiVehicles;

/**
 * Created by carlloga on 16.03.2017.
 */
public class RunAVExample {

    public static void main (String[] args) {

        //manually edit as needed

//        CreateAVDemand createAVDemand = new CreateAVDemand();
//        createAVDemand.createAVDemand(0.05F, 0, "./cottbus_robotaxi/plans0.xml");
//        createAVDemand.createAVDemand(0.05F, 0.1F, "./cottbus_robotaxi/plans1.xml");
//        createAVDemand.createAVDemand(0.05F, 0.2F, "./cottbus_robotaxi/plans2.xml");
//        createAVDemand.createAVDemand(0.05F, 0.3F, "./cottbus_robotaxi/plans3.xml");
//        createAVDemand.createAVDemand(0.05F, 0.4F, "./cottbus_robotaxi/plans4.xml");
//
//        CreateAVs createAVs = new CreateAVs();
//        createAVs.createAVs(530);
//        createAVs.createAVs(1060);
//        createAVs.createAVs(1590);
//        createAVs.createAVs(2120);
//        createAVs.createAVs(3180);
//        createAVs.createAVs(4240);
//        createAVs.createAVs(2650);
//        createAVs.createAVs(5300);
//        createAVs.createAVs(7950);
//        createAVs.createAVs(10600);

        String configFileName = "cottbus_robotaxi/config4.xml";
        RunRobotaxiExample.run(configFileName, false);

        configFileName = "cottbus_robotaxi/config8.xml";
        RunRobotaxiExample.run(configFileName, false);

        configFileName = "cottbus_robotaxi/config12.xml";
        RunRobotaxiExample.run(configFileName, false);

    }

}

package org.matsim.munichArea.automated;

import org.matsim.contrib.av.robotaxi.run.RunRobotaxiExample;

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
////
//        CreateAVs createAVs = new CreateAVs();
//        createAVs.createAVs(3180);
//        createAVs.createAVs(6360);
//        createAVs.createAVs(4240);
//        createAVs.createAVs(8480);

        for (String configFileName : args){

            RunRobotaxiExample.run(configFileName, false);
        }

    }

}

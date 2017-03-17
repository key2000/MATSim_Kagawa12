package org.matsim.munichArea.matsimAvExample;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.av.robotaxi.vehicles.CreateTaxiVehicles;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.data.VehicleImpl;
import org.matsim.contrib.dvrp.data.file.VehicleWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by carlloga on 17.03.2017.
 */
public class CreateAVs {

    public void createAVs(int vehicleNumber) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        int numberofVehicles = vehicleNumber;
        double operationStartTime = 0.0D;
        double operationEndTime = 86400.0D;
        byte seats = 4;
        String networkfile = "./cottbus_robotaxi/network.xml";
        String taxisFile = "./cottbus_robotaxi/taxis_" + numberofVehicles + ".xml";
        ArrayList vehicles = new ArrayList();
        Random random = MatsimRandom.getLocalInstance();
        (new MatsimNetworkReader(scenario.getNetwork())).readFile(networkfile);
        ArrayList allLinks = new ArrayList();
        allLinks.addAll(scenario.getNetwork().getLinks().keySet());

        for(int i = 0; i < numberofVehicles; ++i) {
            Link startLink;
            do {
                Id v = (Id)allLinks.get(random.nextInt(allLinks.size()));
                startLink = (Link)scenario.getNetwork().getLinks().get(v);
            } while(!startLink.getAllowedModes().contains("car"));

            VehicleImpl var16 = new VehicleImpl(Id.create("taxi" + i, Vehicle.class), startLink, (double)seats, operationStartTime, operationEndTime);
            vehicles.add(var16);
        }

        (new VehicleWriter(vehicles)).write(taxisFile);
    }
}

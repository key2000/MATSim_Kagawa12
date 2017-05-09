package org.matsim.munichArea.multimodal;

import com.pb.common.util.ResourceUtil;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.multimodal.ControlerDefaultsWithMultiModalModule;
import org.matsim.contrib.multimodal.RunMultimodalExample;
import org.matsim.contrib.multimodal.config.MultiModalConfigGroup;
import org.matsim.contrib.multimodal.tools.PrepareMultiModalScenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.munichArea.planCreation.CentroidsToLocations;
import org.matsim.munichArea.planCreation.Location;
import org.matsim.munichArea.planCreation.ReadSyntheticPopulation;

import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by carlloga on 08-05-17.
 */
public class MultiModalMATSim {

    public static ResourceBundle rb;

    public static void main (String args[]){

        File propFile = new File(args[0]);
        rb = ResourceUtil.getPropertyBundle(propFile);

        //read locations

        CentroidsToLocations centroidsToLocations = new CentroidsToLocations(rb);
        ArrayList<Location> locationList = centroidsToLocations.readCentroidList();

        //demand creation

        ReadSyntheticPopulation readSyntheticPopulation = new ReadSyntheticPopulation(rb, locationList);
        readSyntheticPopulation.demandFromSyntheticPopulation(0,1,"./input/plans.xml");

        //read config

        Config config = ConfigUtils.loadConfig("input/config_multimodal.xml", new MultiModalConfigGroup());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
        Scenario scenario = ScenarioUtils.loadScenario(config);


        //start matsim simulation

        //add scenario preparation
        PrepareMultiModalScenario.run(scenario);

        Controler controler = new Controler(scenario);

        //add new module
        controler.setModules(new ControlerDefaultsWithMultiModalModule());


        controler.run();

    }



}


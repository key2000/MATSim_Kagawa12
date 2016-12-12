package org.matsim.munichArea.configMatsim;

import com.pb.common.matrix.Matrix;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.*;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.munichArea.planCreation.Location;

import java.util.*;


/**
 * Created by carlloga on 9/14/2016.
 */
public class MatsimRunFromJava {

    public static Matrix runMatsimToCreateTravelTimes(Matrix autoTravelTime,
                                                                                   int timeOfDay, int numberOfCalcPoints /*, Map<Integer,SimpleFeature> zoneFeatureMap*/, //CoordinateTransformation ct,
                                                                                   String inputNetworkFile,
                                                                                   Population population, int year,
                                                                                   String crs, int numberOfIterations, String siloRunId, String outputDirectoryRoot,
                                                                                   double flowCapacityFactor, double storageCapacityFactor,
                                                                                   ArrayList<Location> locationList, boolean getTravelTimes
                                                                                   ) {
        // String populationFile, int year, String crs, int numberOfIterations) {
        final Config config = ConfigUtils.createConfig();

        // Global
        config.global().setCoordinateSystem(crs);

        // Network
        config.network().setInputFile(inputNetworkFile);

        //public transport
        config.transit().setTransitScheduleFile("./input/pt/scheduleS1.xml");
        config.transit().setVehiclesFile("./input/pt/vehiclesS1.xml");
        config.transit().setUseTransit(true);
        Set<String> transitModes = new TreeSet<>();
        transitModes.add("pt");
        config.transit().setTransitModes(transitModes);

        //experimental settings - I don't know if they are useful/required





        //end of experimental settings

        // Plans
        //		config.plans().setInputFile(inputPlansFile);

        // Simulation
        //		config.qsim().setFlowCapFactor(0.01);
        config.qsim().setFlowCapFactor(flowCapacityFactor);
        //		config.qsim().setStorageCapFactor(0.018);
        config.qsim().setStorageCapFactor(storageCapacityFactor);
        config.qsim().setRemoveStuckVehicles(false);

        config.qsim().setStartTime(0);
        config.qsim().setEndTime(24*60*60);

        // Controller
        //		String siloRunId = "run_09";
        String runId = siloRunId + "_" + year;
        String outputDirectory = outputDirectoryRoot;
        config.controler().setRunId(runId);
        config.controler().setOutputDirectory(outputDirectory);
        config.controler().setFirstIteration(1);
        config.controler().setLastIteration(numberOfIterations);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(numberOfIterations);
        config.controler().setWriteEventsInterval(numberOfIterations);

        config.controler().setRoutingAlgorithmType(ControlerConfigGroup.RoutingAlgorithmType.Dijkstra);

        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        //linkstats
//        config.linkStats().setWriteLinkStatsInterval(1);
//        config.linkStats().setAverageLinkStatsOverIterations(0);

        // QSim and other
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.withHoles);
        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        //Strategy
        StrategyConfigGroup.StrategySettings strategySettings1 = new StrategyConfigGroup.StrategySettings();
        strategySettings1.setStrategyName("ChangeExpBeta");
        strategySettings1.setWeight(0.5); //originally 0.8
        config.strategy().addStrategySettings(strategySettings1);

        StrategyConfigGroup.StrategySettings strategySettings2 = new StrategyConfigGroup.StrategySettings();
        strategySettings2.setStrategyName("ReRoute");
        strategySettings2.setWeight(0.5);//originally 0.2
        strategySettings2.setDisableAfter((int) (numberOfIterations * 0.7));
        config.strategy().addStrategySettings(strategySettings2);

        StrategyConfigGroup.StrategySettings strategySettings3 = new StrategyConfigGroup.StrategySettings();
        strategySettings3.setStrategyName("TimeAllocationMutator");
        strategySettings3.setWeight(0.5); //originally 0
        strategySettings3.setDisableAfter((int) (numberOfIterations * 0.7));
        config.strategy().addStrategySettings(strategySettings3);

        config.strategy().setMaxAgentPlanMemorySize(4);

        // Plan Scoring (planCalcScore)
        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work");
        workActivity.setTypicalDuration(8 * 60 * 60);
        workActivity.setOpeningTime(4*60*60);
        config.planCalcScore().addActivityParams(workActivity);

        PlanCalcScoreConfigGroup.ActivityParams newActivity = new PlanCalcScoreConfigGroup.ActivityParams("airport");
        newActivity.setTypicalDuration(3 * 60 * 60);
        newActivity.setOpeningTime(4*60*60);
        config.planCalcScore().addActivityParams(newActivity);

        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);


        config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn);

        // Scenario //chose between population file and population creator in java
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
//        		PopulationReader populationReader = new PopulationReaderMatsimV5(scenario);
//        		populationReader.readFile("./input/population_2013.xml");
        scenario.setPopulation(population);

        // Initialize controller
        final Controler controler = new Controler(scenario);


        //      Add controller listener
        if (getTravelTimes) {
            Zone2ZoneTravelTimeListener zone2zoneTravelTimeListener = new Zone2ZoneTravelTimeListener(
                    controler, scenario.getNetwork(), config.controler().getLastIteration(),
                    locationList, timeOfDay, numberOfCalcPoints, //ct,
                    autoTravelTime);
            controler.addControlerListener(zone2zoneTravelTimeListener);
            // yyyyyy feedback will not work without the above, will it?  kai, apr'16
        }

        // Run controller
        controler.run();

        // Return collected travel times
        return autoTravelTime;
    }
}

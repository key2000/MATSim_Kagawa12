package org.matsim.example.configMatsim;

import com.pb.common.matrix.Matrix;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.example.planCreation.Location;
import org.matsim.pt.counts.PtCountControlerListener;

import java.util.ArrayList;
import java.util.Map;



/**
 * Created by carlloga on 9/14/2016.
 */
public class MatsimRunFromJava {

    public static Matrix runMatsimToCreateTravelTimes(Matrix autoTravelTime,
                                                                                   int timeOfDay, int numberOfCalcPoints /*, Map<Integer,SimpleFeature> zoneFeatureMap*/, //CoordinateTransformation ct,
                                                                                   String inputNetworkFile,
                                                                                   Population population, int year,
                                                                                   String crs, int numberOfIterations, String siloRunId, String outputDirectoryRoot,
                                                                                   //double flowCapacityFactor, double storageCapacityFactor,
                                                                                   ArrayList<Location> locationList
                                                                                   ) {
        //			String populationFile, int year, String crs, int numberOfIterations) {
        final Config config = ConfigUtils.createConfig();

        // Global
        config.global().setCoordinateSystem(crs);

        // Network
        config.network().setInputFile(inputNetworkFile);

        // Plans
        //		config.plans().setInputFile(inputPlansFile);

        // Simulation
        //		config.qsim().setFlowCapFactor(0.01);
        //config.qsim().setFlowCapFactor(flowCapacityFactor);
        //		config.qsim().setStorageCapFactor(0.018);
        //        config.qsim().setStorageCapFactor(storageCapacityFactor);
        config.qsim().setRemoveStuckVehicles(false);

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

        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        // QSim and other
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.withHoles);
        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        // Strategy
        StrategyConfigGroup.StrategySettings strategySettings1 = new StrategyConfigGroup.StrategySettings();
        strategySettings1.setStrategyName("ChangeExpBeta");
        strategySettings1.setWeight(0.2); //originally 0.8
        config.strategy().addStrategySettings(strategySettings1);

        StrategyConfigGroup.StrategySettings strategySettings2 = new StrategyConfigGroup.StrategySettings();
        strategySettings2.setStrategyName("ReRoute");
        strategySettings2.setWeight(0.8);//originally 0.2
        strategySettings2.setDisableAfter((int) (numberOfIterations * 0.7));
        config.strategy().addStrategySettings(strategySettings2);

        config.strategy().setMaxAgentPlanMemorySize(4);

        // Plan Scoring (planCalcScore)
        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work");
        workActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(workActivity);

        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);

        config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.warn);

        // ===
        // Scenario
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        //		PopulationReader populationReader = new PopulationReaderMatsimV5(scenario);
        //		populationReader.readFile(populationFile);
        scenario.setPopulation(population);

        // Initialize controller
        final Controler controler = new Controler(scenario);

        //      Add controller listener
        Zone2ZoneTravelTimeListener zone2zoneTravelTimeListener = new Zone2ZoneTravelTimeListener(
                controler, scenario.getNetwork(), config.controler().getLastIteration(),
                locationList, timeOfDay, numberOfCalcPoints, //ct,
                autoTravelTime);
        controler.addControlerListener(zone2zoneTravelTimeListener);
        // yyyyyy feedback will not work without the above, will it?  kai, apr'16


        // Run controller
        controler.run();

        // Return collected travel times
        return autoTravelTime;
    }
}

package org.matsim.munichArea.configMatsim.planCreation;

import com.pb.common.matrix.Matrix;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by carlloga on 7/24/2017.
 */
public class CreatePopulationExample {




    public void main(String[] args){

        Config matsimConfig;
        Scenario matsimScenario;
        Network matsimNetwork;
        PopulationFactory matsimPopulationFactory;
        Map<Integer, Location> locationMap = new HashMap<>();
        Matrix autoTravelTime;
        Matrix travelDistances;
        Population matsimPopulation;
        Plan matsimPlan;

        matsimConfig = ConfigUtils.createConfig();
        matsimScenario = ScenarioUtils.createScenario(matsimConfig);

        matsimNetwork = matsimScenario.getNetwork();

        matsimPopulation = matsimScenario.getPopulation();
        matsimPopulationFactory = matsimPopulation.getFactory();




        Person person1 = matsimPopulationFactory.createPerson(Id.create(1,Person.class));

        Plan plan1 = matsimPopulationFactory.createPlan();

        Coord homeCoordinates = new Coord(-10, 10);

        //create acts
        Activity homeMorning = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates );
        homeMorning.setEndTime(8*60*60);


        Activity work = matsimPopulationFactory.createActivityFromCoord("work", new Coord(100,250));
        work.setEndTime(17*60*60);

        Activity homeEvening = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates );


        //add acts to plan
        plan1.addActivity(homeMorning);
        Leg leg1 = matsimPopulationFactory.createLeg(TransportMode.car);
        plan1.addLeg(leg1);


        plan1.addActivity(work);
        Leg leg2 = matsimPopulationFactory.createLeg(TransportMode.car);
        plan1.addLeg(leg2);

        plan1.addActivity(homeEvening);


        //add plan to person
        person1.addPlan(plan1);


        //add person to population
        matsimPopulation.addPerson(person1);


        //print the xml plan file

        MatsimWriter popWriter = new PopulationWriter(matsimPopulation, matsimNetwork);
        popWriter.write("./input/population.xml");



        //run the created population
        MutableScenario scenario = (MutableScenario) ScenarioUtils.loadScenario(matsimConfig);
//        		PopulationReader populationReader = new PopulationReaderMatsimV5(scenario);
//        		populationReader.readFile("./input/population_2013.xml");
        scenario.setPopulation(matsimPopulation);


        final Controler controler = new Controler(matsimScenario);

        controler.run();



    }


}

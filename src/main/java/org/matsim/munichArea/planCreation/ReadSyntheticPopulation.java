package org.matsim.munichArea.planCreation;

import com.pb.common.matrix.Matrix;
import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.api.internal.MatsimWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.munichArea.SkimMatrixReader;
import org.matsim.munichArea.planCreation.Location;

import java.io.*;
import java.util.*;

import static org.matsim.munichArea.MatsimExecuter.rb;

/**
 * Created by carlloga on 16.03.2017.
 */
public class ReadSyntheticPopulation {

    ResourceBundle rb;
    Config matsimConfig;
    Scenario matsimScenario;
    Network matsimNetwork;
    PopulationFactory matsimPopulationFactory;
    Map<Integer, Location> locationMap = new HashMap<>();
    Matrix travelDistances;
    private Population matsimPopulation;

    private int[] classes = new int[60];
    private int[][] frequencies = new int[60][4];

    private Map<Integer, Double > jobStartTimeMap = new HashMap<>();
    private Map<Integer, Double > departureFromHomeMap = new HashMap<>();

    Random rnd = new Random();


    public ReadSyntheticPopulation(ResourceBundle rb, ArrayList<Location> locationList) {

        this.rb = rb;
        matsimConfig = ConfigUtils.createConfig();
        matsimScenario = ScenarioUtils.createScenario(matsimConfig);

        matsimNetwork = matsimScenario.getNetwork();
        matsimPopulation = matsimScenario.getPopulation();
        matsimPopulationFactory = matsimPopulation.getFactory();

        //creates a map to look up locations from their ID
        for (Location loc : locationList) {
            locationMap.put(loc.getId(), loc);
        }

        for (int i = 0; i < classes.length; i++) {
            classes[i] = 2 + i * 2;
        }

        SkimMatrixReader skmReader = new SkimMatrixReader();
        travelDistances = skmReader.readSkim(rb.getString("out.skim.auto.dist") + "Test.omx", "mat1");


    }

    public void demandFromSyntheticPopulation(boolean useAvs, float avPenetrationRate, float scalingFactor, String plansFileName) {

        //todo remove useAvs variable from this class

        String fileName = rb.getString("syn.pop.file");
        String cvsSplitBy = ",";
        BufferedReader br = null;
        String line = "";

        try {

            br = new BufferedReader(new FileReader(fileName));

            int lines = 0;
            while ((line = br.readLine()) != null) {
                if (lines > 0) {
                    String[] row = line.split(cvsSplitBy);

                    //applies the following only if the person works
                    if (!row[7].equals("0")) {
                        //create a person
                        org.matsim.api.core.v01.population.Person matsimPerson = addPersonToMatsim(row);
                        //select mode from O to 3
                        int mode = selectMode(row);
                        boolean automatedVehicle = chooseAv(useAvs, avPenetrationRate);
                        //create trips
                        createMatsimTrip(row, mode, automatedVehicle, matsimPerson, scalingFactor);
                    }
                }
                lines++;

            }

            System.out.println("Read " + lines + "lines from the SP csv file");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        MatsimWriter popWriter = new PopulationWriter(matsimPopulation, matsimNetwork);
        popWriter.write(plansFileName);

    }

    private org.matsim.api.core.v01.population.Person addPersonToMatsim(String[] row) {
        org.matsim.api.core.v01.population.Person matsimPerson =
                matsimPopulationFactory.createPerson(Id.create(row[0], org.matsim.api.core.v01.population.Person.class));

        return matsimPerson;
    }


    private int selectMode(String[] row) {

        //0: car, 1: walk, 2: bicycle: 3: transit
        int[] alternatives = new int[]{0, 1, 2, 3};

        double[] utilities = calculateUtilities(row, alternatives);

        double probability_denominator = Arrays.stream(utilities).sum();

        double[] probabilities = Arrays.stream(utilities).map(u -> u / probability_denominator).toArray();

        int chosen = new EnumeratedIntegerDistribution(alternatives, probabilities).sample();

        float travelDistance = travelDistances.getValueAt(Integer.parseInt(row[12]), Integer.parseInt(row[7]));
        int i = 0;
        while (travelDistance > classes[i] * 1000 & i < classes.length - 1) {
            i++;
        }
        frequencies[i][chosen]++;


        return chosen;

    }

    private double[] calculateUtilities(String[] row, int[] alternatives) {


        float travelDistance = travelDistances.getValueAt(Integer.parseInt(row[12]), Integer.parseInt(row[7]));


        double[] utilities = new double[alternatives.length];


        //0: car, 1: walk, 2: bicycle: 3: transit
        utilities[0] = Math.exp(-23.4564 * Math.exp(-0.05 * travelDistance / 1000));
        utilities[1] = Math.exp(-51.896 + 89.47667 * Math.exp(-0.2 * travelDistance / 1000));
        utilities[2] = Math.exp(-6.03105 -18.3921 * Math.exp(-0.07 * travelDistance / 1000));
        utilities[3] = Math.exp(-1.30203 -22.8496 * Math.exp(-0.05 * travelDistance / 1000));

        return utilities;


    }

    private boolean chooseAv(boolean useAvs, float penetrationRate) {
        boolean automated = false;

        if (Math.random() < penetrationRate) automated = true;

        return automated;

    }

    private void createMatsimTrip(String[] row, int mode, boolean automatedVehicle, org.matsim.api.core.v01.population.Person matsimPerson,
                                  float scalingFactor) {

        Location origLoc = locationMap.get(Integer.parseInt(row[12]));
        Location destLoc = locationMap.get(Integer.parseInt(row[7]));

        if (mode == 0) {

            if (Math.random() < scalingFactor) {

                //simple time of day selection based on jobtype
                double avg=0;
                double min=0;
                double max=0;
                double sd=0;
                switch (row[16]){
                    case "1": {avg = 7; min = 5; max = 9; sd = 0.5; break;}
                    case "2": {avg = 7; min = 5; max = 9; sd = 0.5;break;}
                    case "3": {avg = 7; min = 5; max = 9; sd = 0.5;break;}
                    case "4": {avg = 7; min = 6; max = 8; sd = 0.3;break;}
                    case "5": {avg = 8; min = 5; max = 11; sd = 1;break;}
                    case "6": {avg = 8; min = 5; max = 11; sd = 1;break;}
                    case "7": {avg = 8.5; min = 7; max = 10; sd = 0.5;break;}
                    case "8": {avg = 8.5; min = 7; max = 10; sd = 0.5;break;}
                    case "9": {avg = 8.5; min = 7; max = 10; sd = 0.5;break;}
                    case "10": {avg = 8.5; min = 7; max = 10; sd = 0.5;break;}

                };


                Plan matsimPlan = matsimPopulationFactory.createPlan();
                matsimPerson.addPlan(matsimPlan);

                Coord homeCoordinates = new Coord(origLoc.getX() + origLoc.getSize() * (Math.random() - 0.5), origLoc.getY() + origLoc.getSize() * (Math.random() - 0.5));

                Activity activity1 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);

                double time = avg * 60 * 60 + sd*rnd.nextGaussian() * 60 * 60;
                time = Math.max(min * 60 * 60, Math.min(time, max * 60 * 60));

                jobStartTimeMap.put(Integer.parseInt(matsimPerson.getId().toString()),time);
                departureFromHomeMap.put(Integer.parseInt(matsimPerson.getId().toString()),time - 1.25 * Float.parseFloat(row[14])*60 );

                activity1.setEndTime(time - 1.25 * Float.parseFloat(row[14])*60);
                matsimPlan.addActivity(activity1);

                if (automatedVehicle) {
                    matsimPlan.addLeg(matsimPopulationFactory.createLeg("taxi"));
                } else {
                    matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));
                }

                Coord workCoordinates = new Coord(destLoc.getX() + destLoc.getSize() * (Math.random() - 0.5), destLoc.getY() + destLoc.getSize() * (Math.random() - 0.5));
                Activity activity2 = matsimPopulationFactory.createActivityFromCoord("work", workCoordinates);
                activity2.setStartTime(time);
                matsimPlan.addActivity(activity2);


                matsimPopulation.addPerson(matsimPerson);

            }
        }


    }

    public Population getMatsimPopulation() {
        return matsimPopulation;
    }

    public void printHistogram() {

        BufferedWriter bw = IOUtils.getBufferedWriter("sp/tripDistanceHistogram.csv");
        try {
            bw.write("distance, car, walk, bicycle, transit");
            bw.newLine();
            for (int i = 0; i < classes.length; i++) {
                bw.write(classes[i] + "," + frequencies[i][0]+ "," + frequencies[i][1] + "," + frequencies[i][2] + "," + frequencies[i][3]);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void printSyntheticPlansList(String fileName) {

        BufferedWriter bw = IOUtils.getBufferedWriter(fileName);
        try {
            bw.write("id,departs,arrives");
            bw.newLine();
            for (int id : departureFromHomeMap.keySet()) {
                bw.write(id + "," + departureFromHomeMap.get(id) + "," + jobStartTimeMap.get(id));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

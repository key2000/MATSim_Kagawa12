package org.matsim.munichArea.planCreation;

import com.pb.common.datafile.TableDataSet;
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
import org.matsim.munichArea.Util;

import java.io.*;
import java.util.*;

/**
 * Created by carlloga on 16.03.2017.
 */
public class ReadSyntheticPopulation {

    private ResourceBundle rb;
    private Config matsimConfig;
    private Scenario matsimScenario;
    private Network matsimNetwork;
    private PopulationFactory matsimPopulationFactory;
    private Map<Integer, Location> locationMap = new HashMap<>();
    private Matrix autoTravelTime;
    private Matrix travelDistances;
    private Population matsimPopulation;
    private Plan matsimPlan;
    private double time;
    private org.matsim.api.core.v01.population.Person matsimPerson;
    private TableDataSet timeOfDayDistributions;
    private int[] timeClasses;
    private double[] departure2WProb;
    private double[] departure2OProb;
    private double[] wDurationProb;
    private double[] oDurationProb;
    private double oTripRatePerPerson;
    private TableDataSet oDistanceDistribution;
    private int[] oDistanceClasses;
    private double[] oDistanceProb;

    private int h2wTripCount;
    private int h2oTripCount;
    private int travelerCount;

    private double carOcccupancyW;
    private double carOcccupancyO;

    private int[] distanceClasses = new int[60];

    private int[][] frequencies = new int[60][4];

    //maps to store duration of H2W trips
    private Map<Integer, Double> jobArrivalsMap = new HashMap<>();
    private Map<Integer, Double> jobDeparturesMap = new HashMap<>();


    private Map<Integer, Double> otherDeparturesMap = new HashMap<>();
    private Map<Integer, Double> otherArrivalsMap = new HashMap<>();

    private Map<Integer, Float> jobDistances = new HashMap<>();
    private Map<Integer, Float> otherDistances = new HashMap<>();

    Random rnd = new Random();

    public ReadSyntheticPopulation(ResourceBundle rb, ArrayList<Location> locationList) {

        SkimMatrixReader skmReader1 = new SkimMatrixReader();
        autoTravelTime = skmReader1.readSkim(rb.getString("base.skim.file") , "mat1");

        this.rb = rb;
        matsimConfig = ConfigUtils.createConfig();
        matsimScenario = ScenarioUtils.createScenario(matsimConfig);

        matsimNetwork = matsimScenario.getNetwork();
        matsimPopulation = matsimScenario.getPopulation();
        matsimPopulationFactory = matsimPopulation.getFactory();

        timeOfDayDistributions = Util.readCSVfile(rb.getString("time.of.day.distr"));
        timeClasses = timeOfDayDistributions.getColumnAsInt("classes");
        departure2WProb = timeOfDayDistributions.getColumnAsDouble("H2W_departure");
        wDurationProb = timeOfDayDistributions.getColumnAsDouble("W_duration");
        departure2OProb = timeOfDayDistributions.getColumnAsDouble("H2O_departure");
        oDurationProb = timeOfDayDistributions.getColumnAsDouble("O_duration");

        oDistanceDistribution = Util.readCSVfile(rb.getString("other.distance.distr"));
        oDistanceClasses = oDistanceDistribution.getColumnAsInt("distanceClass");
        oDistanceProb = oDistanceDistribution.getColumnAsDouble("H20_length");

        //todo as input?
        oTripRatePerPerson = 1.977;
        carOcccupancyO = 2.76;
        carOcccupancyW = 1.13;

        h2wTripCount = 0;
        h2oTripCount = 0;
        travelerCount = 0;


        //creates a map to look up locations from their ID
        for (Location loc : locationList) {
            locationMap.put(loc.getId(), loc);
        }

        for (int i = 0; i < distanceClasses.length; i++) {
            distanceClasses[i] = 2 + i * 2;
        }

        SkimMatrixReader skmReader2 = new SkimMatrixReader();
        travelDistances = skmReader2.readSkim(rb.getString("out.skim.auto.dist") + "Test.omx", "mat1");

    }

    public void demandFromSyntheticPopulation(float avPenetrationRate, float scalingFactor, String plansFileName) {

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

                    int origin = Integer.parseInt(row[12]);
                    int destinationWork = Integer.parseInt(row[7]);
                    boolean occupation = destinationWork == 0? false : true;

                    time = 0;
                    matsimPlan = matsimPopulationFactory.createPlan();
                    matsimPerson = createMatsimPerson(row);
                    matsimPerson.addPlan(matsimPlan);

                    boolean storePerson = false;

                    //be at home
                    Location origLoc = locationMap.get(origin);
                    Coord homeCoordinates = new Coord(origLoc.getX() + origLoc.getSize() * (Math.random() - 0.5), origLoc.getY() + origLoc.getSize() * (Math.random() - 0.5));

                    //generate H-2-W-2
                    if (occupation) {
                        float travelDistance = travelDistances.getValueAt(origin, destinationWork);
                        jobDistances.put(Integer.parseInt(matsimPerson.getId().toString()), travelDistance);
                        int mode = selectMode(travelDistance);
                        boolean automatedVehicle = chooseAv(avPenetrationRate);
                        //create trips applying SCALING FACTOR
                        if (rnd.nextFloat() < scalingFactor/carOcccupancyW && mode == 0 && travelDistance < 80000) {
                            time = new EnumeratedIntegerDistribution(timeClasses, departure2WProb).sample()*60
                                            + (rnd.nextDouble()-.5)*60*60;
                            jobDeparturesMap.put(Integer.parseInt(matsimPerson.getId().toString()), time);



                            Activity activity1 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);
                            activity1.setEndTime(time);
                            matsimPlan.addActivity(activity1);

                            time += autoTravelTime.getValueAt(origin, destinationWork)*60;
                            createMatsimWorkTrip(origin, destinationWork, automatedVehicle);
                            time += autoTravelTime.getValueAt(destinationWork, origin)*60;

                            jobArrivalsMap.put(Integer.parseInt(matsimPerson.getId().toString()), time);


                            storePerson = true;

                        }
                    }

                    //generate H-2-O-2-

                    for (int trip = 0; trip < Math.round(rnd.nextGaussian() + oTripRatePerPerson);trip++) {

                        float travelDistance = selectDistanceOtherTrip();
                        int mode = selectMode(travelDistance);

                        if (rnd.nextFloat() < scalingFactor/carOcccupancyO && mode == 0 && time < 20*60*60 && travelDistance < 80000) {

                            int destinationOther = selectDestionationOtherTrip(origin, travelDistance);

                            time = Math.max(time, new EnumeratedIntegerDistribution(timeClasses, departure2OProb).sample()*60 +
                                    (rnd.nextDouble()-0.5)*60*60);

                            otherDeparturesMap.put(Integer.parseInt(matsimPerson.getId().toString()+ trip), time);
                            otherDistances.put(Integer.parseInt(matsimPerson.getId().toString()+ trip), travelDistances.getValueAt(origin, destinationOther));

                            Activity activity10 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);
                            activity10.setEndTime(time);
                            matsimPlan.addActivity(activity10);

                            time += autoTravelTime.getValueAt(origin, destinationOther)*60;
                            createMatsimOtherTrip(origin, destinationOther);
                            time += autoTravelTime.getValueAt(destinationOther, origin)*60;

                            otherArrivalsMap.put(Integer.parseInt(matsimPerson.getId().toString() + trip), time);

                            storePerson = true;
                        }
                    }

                    //add the person to the matsim population
                    if (storePerson) {
                        //generate -H
                        Activity activity100 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);
                        matsimPlan.addActivity(activity100);
                        travelerCount++;
                        matsimPopulation.addPerson(matsimPerson);
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

        System.out.println("Travelers = " + travelerCount);
        System.out.println("H2W trips = " + h2wTripCount);
        System.out.println("H2O trips = " + h2oTripCount);


        MatsimWriter popWriter = new PopulationWriter(matsimPopulation, matsimNetwork);
        popWriter.write(plansFileName);

    }



    private org.matsim.api.core.v01.population.Person createMatsimPerson(String[] row) {
        org.matsim.api.core.v01.population.Person matsimPerson =
                matsimPopulationFactory.createPerson(Id.create(row[0], org.matsim.api.core.v01.population.Person.class));

        return matsimPerson;
    }


    private int selectMode(float travelDistance) {

        //0: car, 1: walk, 2: bicycle: 3: transit
        int[] alternatives = new int[]{0, 1, 2, 3};

        double[] utilities = calculateUtilities(travelDistance, alternatives);

        double probability_denominator = Arrays.stream(utilities).sum();

        double[] probabilities = Arrays.stream(utilities).map(u -> u / probability_denominator).toArray();

        int chosen = new EnumeratedIntegerDistribution(alternatives, probabilities).sample();


        int i = 0;
        while (travelDistance > distanceClasses[i] * 1000 & i < distanceClasses.length - 1) {
            i++;
        }
        frequencies[i][chosen]++;

        return chosen;

    }

    private double[] calculateUtilities(float travelDistance, int[] alternatives) {

        double[] utilities = new double[alternatives.length];

        //0: car, 1: walk, 2: bicycle: 3: transit
        utilities[0] = Math.exp(-23.4564 * Math.exp(-0.05 * travelDistance / 1000));
        utilities[1] = Math.exp(-51.896 + 89.47667 * Math.exp(-0.2 * travelDistance / 1000));
        utilities[2] = Math.exp(-6.03105 - 18.3921 * Math.exp(-0.07 * travelDistance / 1000));
        utilities[3] = Math.exp(-1.30203 - 22.8496 * Math.exp(-0.05 * travelDistance / 1000));

        return utilities;

    }

    private boolean chooseAv(float penetrationRate) {
        boolean automated = false;

        if (Math.random() < penetrationRate) automated = true;

        return automated;

    }

    private void createMatsimOtherTrip(int origin, int destination) {

        matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));
        Location destLoc = locationMap.get(destination);
        Coord destCoordinate = new Coord(destLoc.getX() + destLoc.getSize() * (Math.random() - 0.5), destLoc.getY() + destLoc.getSize() * (Math.random() - 0.5));
        Activity activity4 = matsimPopulationFactory.createActivityFromCoord("other", destCoordinate);
        time =Math.max(time, Math.min( time + new EnumeratedIntegerDistribution(timeClasses, oDurationProb).sample()*60, 22*60*60 +
                (rnd.nextDouble()-0.5)*60*60));
        activity4.setEndTime(time);
        matsimPlan.addActivity(activity4);
        matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));

        h2oTripCount++;

    }

    private int selectDestionationOtherTrip(int origin, float travelDistance) {

        int[] alternatives = new int[travelDistances.getRowCount()];
        //get 4953 length empty int
        double[] probabilities = new double[alternatives.length];

        for (int i=0; i<alternatives.length; i++){
            float distanceDiff = Math.abs(travelDistances.getValueAt(origin, i+1) - travelDistance);
            //System.out.println(distanceDiff);
            alternatives[i] = i + 1;
            probabilities[i] = distanceDiff>0 ? 1 / distanceDiff : 1;
        }

        //selects a random destination of travelDistance +- 1 km
        return new EnumeratedIntegerDistribution(alternatives, probabilities).sample() ;
    }

    private float selectDistanceOtherTrip() {

        //randomly select a distance at 1 km intervals according to mid distributions
        return (float) (new EnumeratedIntegerDistribution(oDistanceClasses, oDistanceProb).sample()*1000);

    }


    private void createMatsimWorkTrip(int origin, int destinationWork, boolean automatedVehicle) {

        //Location origLoc = locationMap.get(origin);
        Location destLoc = locationMap.get(destinationWork);

        //old method tho select departure time divided by industry sector
        //if (mode == 0) {

            //if (Math.random() < scalingFactor) {

            //simple time of day selection based on jobtype
            /*double avg = 0;
            double min = 0;
            double max = 0;
            double sd = 0;
            switch (row[16]) {
                case "1": {
                    avg = 7; min = 5; max = 9; sd = 0.5;
                    break;
                }
                case "2": {
                    avg = 7; min = 5; max = 9; sd = 0.5;
                    break;
                }
                case "3": {
                    avg = 7; min = 5; max = 9; sd = 0.5;
                    break;
                }
                case "4": {
                    avg = 7; min = 6;max = 8;  sd = 0.3;
                    break;
                }
                case "5": {
                    avg = 8;
                    min = 5;
                    max = 11;
                    sd = 1;
                    break;
                }
                case "6": {
                    avg = 8;
                    min = 5;
                    max = 11;
                    sd = 1;
                    break;
                }
                case "7": {
                    avg = 8.5;
                    min = 7;
                    max = 10;
                    sd = 0.5;
                    break;
                }
                case "8": {
                    avg = 8.5;
                    min = 7;
                    max = 10;
                    sd = 0.5;
                    break;
                }
                case "9": {
                    avg = 8.5;
                    min = 7;
                    max = 10;
                    sd = 0.5;
                    break;
                }
                case "10": {
                    avg = 8.5;
                    min = 7;
                    max = 10;
                    sd = 0.5;
                    break;
                }

            }
            */
            ;



            //take a departure time taken from the distribution of MiD

            //this is an expected travel time to work



            if (automatedVehicle) {
                matsimPlan.addLeg(matsimPopulationFactory.createLeg("taxi"));
            } else {
                matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));
            }





            Coord workCoordinates = new Coord(destLoc.getX() + destLoc.getSize() * (Math.random() - 0.5), destLoc.getY() + destLoc.getSize() * (Math.random() - 0.5));
            Activity activity2 = matsimPopulationFactory.createActivityFromCoord("work", workCoordinates);
            activity2.setStartTime(time);

            //add the duration of the job to time and send person back home

            time =  Math.max(time, Math.min(time + new EnumeratedIntegerDistribution(timeClasses, wDurationProb).sample()*60,20*60*60 +
                    (rnd.nextDouble()-0.5)*60*60));

            activity2.setEndTime(time);

            matsimPlan.addActivity(activity2);

            if (automatedVehicle) {
                matsimPlan.addLeg(matsimPopulationFactory.createLeg("taxi"));
            } else {
                matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.car));
            }






            h2wTripCount++;
            //}
        //}
    }


    public Population getMatsimPopulation() {
        return matsimPopulation;
    }

    public void printHistogram() {

        BufferedWriter bw = IOUtils.getBufferedWriter("sp/tripDistanceHistogram.csv");
        try {
            bw.write("distance, car, walk, bicycle, transit");
            bw.newLine();
            for (int i = 0; i < distanceClasses.length; i++) {
                bw.write(distanceClasses[i] + "," + frequencies[i][0] + "," + frequencies[i][1] + "," + frequencies[i][2] + "," + frequencies[i][3]);
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
            bw.write("id,departs,arrives, type, distance");
            bw.newLine();
            for (int id : jobDeparturesMap.keySet()) {
                bw.write(id + "," + jobDeparturesMap.get(id) + "," + jobArrivalsMap.get(id) + ",work," + jobDistances.get(id));
                bw.newLine();
            }
            for (int id : otherDeparturesMap.keySet()) {
                bw.write(id + "," + otherDeparturesMap.get(id) + "," + otherArrivalsMap.get(id) + ",other," + otherDistances.get(id));
                bw.newLine();
            }


            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

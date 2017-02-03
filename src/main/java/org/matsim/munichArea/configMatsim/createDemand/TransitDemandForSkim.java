package org.matsim.munichArea.configMatsim.createDemand;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.munichArea.planCreation.Location;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import static org.matsim.munichArea.MatsimExecuter.munich;

/**
 * Created by carlloga on 3/2/17.
 */
public class TransitDemandForSkim {




    public ArrayList<Location> locationsServedBySUBahn(ArrayList<Location> locationList) {
        String fileName = munich.getString("zones.served.SU.file");

        BufferedReader bufferReader = null;
        ArrayList<Location> zonesServedList = new ArrayList<>();

        try {
            int lineCount = 1;
            String line;
            bufferReader = new BufferedReader(new FileReader(fileName));

            while ((line = bufferReader.readLine()) != null) {
                if (lineCount > 1) {
                    String[] row = line.split(",");
                    int locationId = Integer.parseInt(row[1]);
                    zonesServedList.add(locationList.get(locationId));
                }
                lineCount++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferReader != null) bufferReader.close();
            } catch (IOException crunchifyException) {
                crunchifyException.printStackTrace();
            }
        }
        System.out.println("The number of zones served by transit is = " + zonesServedList.size());
        return zonesServedList;
    }

    public Map< Id, PtSyntheticTraveller> createDemandForSkims(ArrayList<Location> zonesServedList, int personId, Population matsimPopulation) {

        Map< Id, PtSyntheticTraveller> ptSyntheticTravellerMap = new HashMap<>();

        PopulationFactory matsimPopulationFactory = matsimPopulation.getFactory();
        //try {
          //  PrintWriter pw = new PrintWriter(new FileWriter("input/pt/OdPairs.csv", false));

            //pw.println("person, from, to");

            double time = 8 * 60 * 60;
            for (int i = 0; i < zonesServedList.size(); i++) {
                for (int j = 0; j < zonesServedList.size(); j++) {
                    if (i >= j) {
                        // int i = 232;
                        //int j = 69;
                        Location origLoc = zonesServedList.get(i);
                        Location destLoc = zonesServedList.get(j);

                        org.matsim.api.core.v01.population.Person matsimPerson =
                                matsimPopulationFactory.createPerson(Id.create(personId, org.matsim.api.core.v01.population.Person.class));
                        matsimPopulation.addPerson(matsimPerson);
                        //pw.println(personId + "," + origLoc.getId() + "," + destLoc.getId());

                        PtSyntheticTraveller ptSyntheticTraveller = new PtSyntheticTraveller(personId, origLoc, destLoc, matsimPerson);
                        ptSyntheticTravellerMap.put(matsimPerson.getId(), ptSyntheticTraveller);

                        personId++;
                        Plan matsimPlan = matsimPopulationFactory.createPlan();
                        matsimPerson.addPlan(matsimPlan);

                        Coord homeCoordinates = new Coord(origLoc.getX() + origLoc.getSize() * (Math.random() - 0.5), origLoc.getY() + origLoc.getSize() * (Math.random() - 0.5));
                        Activity activity1 = matsimPopulationFactory.createActivityFromCoord("home", homeCoordinates);
                        activity1.setEndTime(time);
                        matsimPlan.addActivity(activity1);
                        matsimPlan.addLeg(matsimPopulationFactory.createLeg(TransportMode.pt));

                        Coord workCoordinates = new Coord(destLoc.getX() + destLoc.getSize() * (Math.random() - 0.5), destLoc.getY() + destLoc.getSize() * (Math.random() - 0.5));
                        Activity activity2 = matsimPopulationFactory.createActivityFromCoord("work", workCoordinates);
                        matsimPlan.addActivity(activity2);

                        time = time + 1;
                    }
                }
                time = 8 * 60 * 60;
            }

            //pw.flush();
            //pw.close();

        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        return ptSyntheticTravellerMap;

    }



}

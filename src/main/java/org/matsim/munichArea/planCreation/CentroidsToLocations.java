package org.matsim.munichArea.planCreation;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import static org.matsim.munichArea.MatsimExecuter.rb;

/**
 * Created by carlloga on 9/12/2016.
 */
public class CentroidsToLocations {

    ResourceBundle rb;

    public CentroidsToLocations(ResourceBundle rb) {
        this.rb = rb;
    }

    public ArrayList<Location> readCentroidList() {


        //read the centroid list
        String workDirectory = rb.getString("location.list.folder");
        String fileName = workDirectory + rb.getString("location.list.file");

        BufferedReader bufferReader = null;
        ArrayList<Location> locationList = new ArrayList<>();

        try {
            String line;
            bufferReader = new BufferedReader(new FileReader(fileName));


            while ((line = bufferReader.readLine()) != null ) {
                Location location = CSVtoLocation(line);
                locationList.add(location);
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

        return locationList;

    }
    public static Location CSVtoLocation(String csvLine) {
        int id;
        double x;
        double y;
        long pop;
        long emp;
        float size;
        String[] splitData = csvLine.split("\\s*,\\s*");
        id = Integer.parseInt(splitData[0]);
        x =Double.parseDouble(splitData[1]);
        y =Double.parseDouble(splitData[2]);
        pop =Long.parseLong(splitData[3]);
        emp = Long.parseLong(splitData[4]);
        size = Float.parseFloat(splitData[5]);
        Location location = new Location(id,x,y, pop,emp, size);
        return location;
    }


}

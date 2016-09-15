package org.matsim.example;

import omx.OmxFile;
import omx.OmxLookup;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.example.planCreation.Location;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Created by carlloga on 9/15/2016.
 */
public class Accessibility {

    public static void calculateAccessibility(ArrayList<Location> locationList){

        double travelTimeArray[][] = readSkim();

        for (Location orig : locationList){
            double accessibility = 0;
            for (Location dest: locationList){
                accessibility += Math.exp(-0.1*getAutoTravelTime(orig.getId(), dest.getId(), travelTimeArray));
            }
            orig.setAccessibility(accessibility);
        }

        BufferedWriter bw = IOUtils.getBufferedWriter("./output/accessibility.csv");
        try {
            bw.write("ID, X, Y, geoAccess");
            bw.newLine();
            for (Location loc : locationList) {
                bw.write(loc.getId() + "," + loc.getX() + "," + loc.getY() + "," + loc.getAccessibility());
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static double[][] readSkim() {
        // read skim file

        OmxFile hSkim = new OmxFile("./output/travelTimes.omx");
        hSkim.openReadOnly();
        OmxMatrix timeOmxSkimAutos = hSkim.getMatrix("mat1");

        double autoTravelTime[][] = (double[][]) timeOmxSkimAutos.getData();

//        OmxLookup omxLookUp = hSkim.getLookup("lookup1");
//        int[] externalNumbers = (int[]) omxLookUp.getLookup();
        return autoTravelTime;
    }

    public static double getAutoTravelTime(int orig, int dest, double[][] travelTimeArray){
        return travelTimeArray[orig-1][dest-1];
    }

 }




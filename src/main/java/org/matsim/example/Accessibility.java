package org.matsim.example;

import com.pb.common.matrix.Matrix;
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

    private Matrix autoTravelTime;

    public void calculateTravelTimesToZone(ArrayList<Location> locationList, int destinationId) {

        readSkim();
        for (Location orig : locationList){
            double travelTime = getAutoTravelTime(orig.getId(), destinationId, autoTravelTime);
            orig.setTravelTime(travelTime);
        }

    }


    public void calculateAccessibility(ArrayList<Location> locationList){

        readSkim();

        for (Location orig : locationList){
            float accessibility = 0;
            for (Location dest: locationList){
                accessibility += Math.pow(dest.getPopulation(),1.25) * Math.exp(-0.1*getAutoTravelTime(orig.getId(), dest.getId(), autoTravelTime));
            }
            orig.setAccessibility(accessibility);
        }




    }

    public static void printAccessibility(ArrayList<Location> locationList) {
        BufferedWriter bw = IOUtils.getBufferedWriter("./output/accessibility.csv");
        try {
            bw.write("ID, X, Y, access, timeToZone");
            bw.newLine();
            for (Location loc : locationList) {
                bw.write(loc.getId() + "," + loc.getX() + "," + loc.getY() + "," + loc.getAccessibility()+"," + loc.getTravelTime());
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readSkim() {
        // read skim file

        OmxFile hSkim = new OmxFile("./data/travelTimes.omx");
        hSkim.openReadOnly();
        OmxMatrix timeOmxSkimAutos = hSkim.getMatrix("mat1");

        autoTravelTime = convertOmxToMatrix(timeOmxSkimAutos);

//        OmxLookup omxLookUp = hSkim.getLookup("lookup1");
//        int[] externalNumbers = (int[]) omxLookUp.getLookup();

    }

    public double getAutoTravelTime(int orig, int dest, Matrix autoTravelTime){
        return autoTravelTime.getValueAt(orig-1,dest-1);
    }

    public static Matrix convertOmxToMatrix (OmxMatrix omxMatrix) {
        // convert OMX matrix into java matrix

        OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        String name = omxMatrix.getName();
        int[] dimensions = omxMatrix.getShape();

        if (type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            float[][] fArray = (float[][]) omxMatrix.getData();
            Matrix mat = new Matrix(name, name, dimensions[0], dimensions[1]);
            for (int i = 0; i < dimensions[0]; i++)
                for (int j = 0; j < dimensions[1]; j++)
                    mat.setValueAt(i + 1, j + 1, fArray[i][j]);
            return mat;
        } else if (type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            double[][] dArray = (double[][]) omxMatrix.getData();
            Matrix mat = new Matrix(name, name, dimensions[0], dimensions[1]);
            for (int i = 0; i < dimensions[0]; i++)
                for (int j = 0; j < dimensions[1]; j++)
                    mat.setValueAt(i + 1, j + 1, (float) dArray[i][j]);
            return mat;
        } else {
            System.out.println("OMX Matrix type " + type.toString() + " not yet implemented. Program exits.");
            exit(1);
            return null;
        }
    }

    public Matrix getAutoTravelTimeMatrix() {
        return autoTravelTime;
    }
}




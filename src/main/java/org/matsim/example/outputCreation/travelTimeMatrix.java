package org.matsim.example.outputCreation;

import com.pb.common.matrix.Matrix;
import omx.*;
import omx.hdf5.*;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.example.planCreation.Location;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;




/**
 * Created by carlloga on 9/14/2016.
 */
public class travelTimeMatrix {


    public static void createOmxSkimMatrix(Matrix autoTravelTime, ArrayList<Location> locationList, String omxFileName){


        try (OmxFile omxFile = new OmxFile(omxFileName)) {

            int dim0 = locationList.size();

            int dim1 = dim0;
            int[] shape = {dim0,dim1};

            float mat1NA = -1;
            //Matrix autoTravelTime;
//            autoTravelTime = new Matrix(dim0,dim1);



            //double[][] mat1Data = new double[dim0][dim1];
//            for (int i = 0; i < dim0; i++)
//                for (int j = 0; j < dim1; j++) {
//                    Tuple<Integer, Integer> tuple = new Tuple<>(i+1,j+1);
//                    //mat1Data[i][j] = travelTimesMap.get(tuple);
//                    autoTravelTime.setValueAt(i,j,travelTimesMap.get(tuple));
//                }

            OmxMatrix.OmxFloatMatrix mat1 = new OmxMatrix.OmxFloatMatrix("mat1",autoTravelTime.getValues(),mat1NA);
            mat1.setAttribute(OmxConstants.OmxNames.OMX_DATASET_TITLE_KEY.getKey(),"travelTimes");



            int lookup1NA = -1;
            int[] lookup1Data = new int[dim0];
            Set<Integer> lookup1Used = new HashSet<>();
            for (int i = 0; i < lookup1Data.length; i++) {
                int lookup = i+1;
                lookup1Data[i] = lookup1Used.add(lookup) ? lookup : lookup1NA;
            }
            OmxLookup.OmxIntLookup lookup1 = new OmxLookup.OmxIntLookup("lookup1",lookup1Data,lookup1NA);


            omxFile.openNew(shape);
            omxFile.addMatrix(mat1);
            omxFile.addLookup(lookup1);
            omxFile.save();
            System.out.println(omxFile.summary());

            System.out.println("travel time matrix written");

        }
// clean the matrix if not needed ?
//        try (OmxFile omxFile = new OmxFile(f)) {
//            omxFile.openReadWrite();
//            System.out.println(omxFile.summary());
//            omxFile.deleteMatrix("mat1");
//            omxFile.deleteLookup("lookup1");
//            System.out.println(omxFile.summary());
//        }

    }

    }








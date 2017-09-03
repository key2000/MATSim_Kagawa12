package org.matsim.munichArea.outputCreation.transitSkim;

import com.pb.common.matrix.Matrix;
import org.matsim.munichArea.SkimMatrixReader;
import org.matsim.munichArea.configMatsim.planCreation.Location;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by carlloga on 02.03.2017.
 */
public class TransitSkimPostProcessing {

    private ResourceBundle munich;
    private Matrix inTransitCompleteMatrix;
    private Matrix totalTimeCompleteMatrix;
    private Matrix accessTimeCompleteMatrix;
    private Matrix egressTimeCompleteMatrix;
    private Matrix transfersCompleteMatrix;
    private Matrix inVehicleTimeCompleteMatrix;
    private ArrayList<Location> locationList;
    private ArrayList<Location> servedZoneList;

    private Matrix inTransit;
    private Matrix totalTime;
    private Matrix accessTime;
    private Matrix egressTime;
    private Matrix transfers;
    private Matrix autoTravelDistance;
    private Matrix inVehicle;


    public TransitSkimPostProcessing(ResourceBundle munich, ArrayList<Location> locationList, ArrayList<Location> servedZoneList) {
        this.munich = munich;
        this.locationList = locationList;
        this.servedZoneList = servedZoneList;

    }
/*
    public void postProcessTransitSkims() {
        SkimMatrixReader skimReader = new SkimMatrixReader();
        //read original matrices
        inTransit = skimReader.readSkim(munich.getString("pt.in.skim.file") + "SkimsPt.omx", "mat1");
        totalTime = skimReader.readSkim(munich.getString("pt.total.skim.file") + "SkimsPt.omx", "mat1");
        accessTime = skimReader.readSkim(munich.getString("pt.access.skim.file") + "SkimsPt.omx", "mat1");
        egressTime = skimReader.readSkim(munich.getString("pt.egress.skim.file") + "SkimsPt.omx", "mat1");
        transfers = skimReader.readSkim(munich.getString("pt.transfer.skim.file") + "SkimsPt.omx", "mat1");
        inVehicle = skimReader.readSkim(munich.getString("pt.in.vehicle.skim.file") + "SkimsPt.omx", "mat1");
        //read the distances
        autoTravelDistance = skimReader.readSkim(munich.getString("out.skim.auto.dist.base") + "Test.omx", "mat1");

        String omxPtFileName = rb.getString("pt.total.skim.file") + simulationName + ".omx";


        //fill in the locations without access by transit
        fillTransitMatrix();

    }*/

    public void postProcessTransitSkims(String simulationName) {
        SkimMatrixReader skimReader = new SkimMatrixReader();
        //read original matrices
        String omxPtFileName = munich.getString("pt.in.skim.file") + simulationName + ".omx";
        inTransit = skimReader.readSkim(omxPtFileName, "mat1");
        omxPtFileName = munich.getString("pt.total.skim.file") + simulationName + ".omx";
        totalTime = skimReader.readSkim(omxPtFileName, "mat1");
        omxPtFileName = munich.getString("pt.access.skim.file") + simulationName + ".omx";
        accessTime = skimReader.readSkim(omxPtFileName, "mat1");
        omxPtFileName = munich.getString("pt.egress.skim.file") + simulationName + ".omx";
        egressTime = skimReader.readSkim(omxPtFileName, "mat1");
        omxPtFileName = munich.getString("pt.transfer.skim.file") + simulationName + ".omx";
        transfers = skimReader.readSkim(omxPtFileName , "mat1");
        omxPtFileName = munich.getString("pt.in.vehicle.skim.file") + simulationName + ".omx";
        inVehicle = skimReader.readSkim(omxPtFileName , "mat1");
        //read the distances
        autoTravelDistance = skimReader.readSkim(munich.getString("out.skim.auto.dist.base") + "Test.omx", "mat1");



        //fill in the locations without access by transit
        fillTransitMatrix();

    }



    public void fillTransitMatrix() {

        //duplicate the matrices
        inTransitCompleteMatrix = inTransit;
        totalTimeCompleteMatrix = totalTime;
        accessTimeCompleteMatrix = accessTime;
        egressTimeCompleteMatrix = egressTime;
        transfersCompleteMatrix = transfers;
        inVehicleTimeCompleteMatrix = inVehicle;

        locationList.parallelStream().forEach((Location origLoc) -> {
            int i = origLoc.getId();
            float access;
            float egress;
            float tt;
            //total time matrix is used as check if -1
            for (int j = 1; j <= totalTime.getColumnCount(); j++) {
                if (totalTime.getValueAt(i, j) == -1 & i <= j) {
                    tt = Float.MAX_VALUE;
                    float addAccessTime = 0;
                    float addEgressTime = 0;
                    int startZoneIndex = 0;
                    int finalZoneIndex = 0;
                    for (Location k : servedZoneList) {
                        access = (float) (autoTravelDistance.getValueAt(i, k.getId()) / 1.4 / 60);
                        if (access < 30 ) {
                            //there is a location k which one can get to transit by walk in less than 30 min
                            for (Location l : servedZoneList) {
                                egress = (float) (autoTravelDistance.getValueAt(l.getId(), j) / 1.4 / 60);
                                if (egress < 30) {
                                    //there is a location l from which one can get dest by walk in less than 30 min
                                    if (totalTime.getValueAt(k.getId(), l.getId()) > 0) {
                                        if (tt > totalTime.getValueAt(k.getId(), l.getId()) + access + egress) {
                                            //a better OD tranist pair has been found
                                            tt = totalTime.getValueAt(k.getId(), l.getId()) + access + egress;
                                            //stores the access and egress points
                                            startZoneIndex = k.getId();
                                            finalZoneIndex = l.getId();
                                            addAccessTime = access;
                                            addEgressTime = egress;
                                        }
                                    }
                                }
                            }
                        }

                    }

                    if (startZoneIndex != 0 & finalZoneIndex != 0){

                        if (tt < 500 & tt > 0) {
                        //found the best k and l that link i and j by transit inn tt mins
                        inTransitCompleteMatrix.setValueAt(i,j,inTransit.getValueAt(startZoneIndex, finalZoneIndex));
                        inTransitCompleteMatrix.setValueAt(j,i,inTransit.getValueAt(startZoneIndex, finalZoneIndex));

                        accessTimeCompleteMatrix.setValueAt(i,j,addAccessTime + accessTime.getValueAt(startZoneIndex, finalZoneIndex));
                        accessTimeCompleteMatrix.setValueAt(j,i,addAccessTime + accessTime.getValueAt(startZoneIndex, finalZoneIndex));

                        egressTimeCompleteMatrix.setValueAt(i,j,addEgressTime + egressTime.getValueAt(startZoneIndex, finalZoneIndex));
                        egressTimeCompleteMatrix.setValueAt(j,i,addEgressTime + egressTime.getValueAt(startZoneIndex, finalZoneIndex));

                        transfersCompleteMatrix.setValueAt(i,j,transfers.getValueAt(startZoneIndex, finalZoneIndex));
                        transfersCompleteMatrix.setValueAt(j,i,transfers.getValueAt(startZoneIndex, finalZoneIndex));

                        inVehicleTimeCompleteMatrix.setValueAt(i,j,inVehicle.getValueAt(startZoneIndex, finalZoneIndex));
                        inVehicleTimeCompleteMatrix.setValueAt(j,i,inVehicle.getValueAt(startZoneIndex, finalZoneIndex));

                        totalTimeCompleteMatrix.setValueAt(i, j, tt);
                        totalTimeCompleteMatrix.setValueAt(j, i, tt);
                        }
                    }

                }
                //if not found a -1 then skip this


            }

        System.out.println("zone completed " + i);
        });
    }

    public Matrix getInTransitCompleteMatrix() {
        return inTransitCompleteMatrix;
    }

    public Matrix getTotalTimeCompleteMatrix() {
        return totalTimeCompleteMatrix;
    }

    public Matrix getAccessTimeCompleteMatrix() {
        return accessTimeCompleteMatrix;
    }

    public Matrix getEgressTimeCompleteMatrix() {
        return egressTimeCompleteMatrix;
    }

    public Matrix getTransfersCompleteMatrix() {
        return transfersCompleteMatrix;
    }

    public Matrix getInVehicleTimeCompleteMatrix() {
        return inVehicleTimeCompleteMatrix;
    }
}

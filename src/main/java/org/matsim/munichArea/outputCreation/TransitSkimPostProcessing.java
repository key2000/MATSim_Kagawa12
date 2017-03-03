package org.matsim.munichArea.outputCreation;

import com.pb.common.matrix.Matrix;
import org.matsim.munichArea.SkimMatrixReader;
import org.matsim.munichArea.planCreation.Location;

import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by carlloga on 02.03.2017.
 */
public class TransitSkimPostProcessing {

    private ResourceBundle munich;
    private Matrix totalTimeCompleteMatrix;
    private ArrayList<Location> locationList;
    private ArrayList<Location> servedZoneList;


    public TransitSkimPostProcessing(ResourceBundle munich, ArrayList<Location> locationList, ArrayList<Location> servedZoneList) {
        this.munich = munich;
        this.locationList = locationList;
        this.servedZoneList = servedZoneList;

    }

    public void postProcessTransitSkims() {
        SkimMatrixReader skimReader = new SkimMatrixReader();
        //read in vehicle tt
        Matrix inTransit = skimReader.readSkim(munich.getString("pt.in.skim.file") + "SkimsPt.omx", "mat1");
        //read in transit tt
        Matrix totalTime = skimReader.readSkim(munich.getString("pt.total.skim.file") + "SkimsPt.omx", "mat1");
        //read distance by walk
        Matrix autoTravelDistance = skimReader.readSkim(munich.getString("out.skim.auto.dist") + "Test.omx", "mat1");
        //fill in the locations without access by transit
        fillTransitMatrix(inTransit, totalTime, autoTravelDistance);

    }

    public void fillTransitMatrix(Matrix inTansit, Matrix totalTime, Matrix autoTravelDistance) {

        totalTimeCompleteMatrix = new Matrix(totalTime.getRowCount(), totalTime.getColumnCount());
        totalTimeCompleteMatrix.fill(-1F);


        locationList.parallelStream().forEach((Location origLoc) -> {
            int i = origLoc.getId();
            float access;
            float egress;
            float tt;
            for (int j = 1; j <= totalTime.getColumnCount(); j++) {
                if (totalTime.getValueAt(i, j) == -1 & i <= j) {
                    tt = Float.MAX_VALUE;
                    for (Location k : servedZoneList) {
                        access = (float) (autoTravelDistance.getValueAt(i, k.getId()) / 1.4 / 60);
                        if (access < 60) {
                            for (Location l : servedZoneList) {
                                egress = (float) (autoTravelDistance.getValueAt(l.getId(), j) / 1.4 / 60);
                                if (egress < 60) {
                                    if (totalTime.getValueAt(k.getId(), l.getId()) > 0) {
                                        if (tt > totalTime.getValueAt(k.getId(), l.getId()) + access + egress) {
                                            tt = totalTime.getValueAt(k.getId(), l.getId()) + access + egress;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //found the best k and l that link i and j by transit inn tt mins
                    // todo solve issue regarding negative tts
                    if (tt < 500 & tt > 0) {
                        totalTimeCompleteMatrix.setValueAt(i, j, tt);
                        totalTimeCompleteMatrix.setValueAt(j, i, tt);
                        //System.out.println("tt= " + tt);

                    } else {
                        totalTimeCompleteMatrix.setValueAt(i, j, -1F);
                        totalTimeCompleteMatrix.setValueAt(j, i, -1F);
                    }

                } else {
                    if (totalTime.getValueAt(i, j) > 0) {
                        totalTimeCompleteMatrix.setValueAt(i, j, totalTime.getValueAt(i, j));
                        totalTimeCompleteMatrix.setValueAt(j, i, totalTime.getValueAt(i, j));
                    }else {
                        totalTimeCompleteMatrix.setValueAt(i, j, -1F);
                        totalTimeCompleteMatrix.setValueAt(j, i, -1F);
                    }
                }

            }

        System.out.println("zone completed " + i);
        });
    }

    public Matrix getInTransitCompleteMatrix() {
        return totalTimeCompleteMatrix;
    }
}

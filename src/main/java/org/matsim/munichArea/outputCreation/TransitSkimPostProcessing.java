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


    public TransitSkimPostProcessing(ResourceBundle munich, ArrayList<Location> servedZoneList) {
        this.munich = munich;
        this.servedZoneList = servedZoneList;

    }

    public void postProcessTransitSkims(){
        SkimMatrixReader skimReader = new SkimMatrixReader();
        //read in vehicle tt
        Matrix inTransit = skimReader.readSkim(munich.getString("pt.in.skim.file")+ "SkimsPt.omx", "mat1");
        //read in transit tt
        Matrix totalTime = skimReader.readSkim(munich.getString("pt.total.skim.file")+ "SkimsPt.omx", "mat1");
        //read distance by walk
        Matrix autoTravelDistance = skimReader.readSkim(munich.getString("out.skim.auto.dist")+ "Test.omx", "mat1");
        //fill in the locations without acces by transit

        fillTransitMatrix(inTransit, totalTime, autoTravelDistance);

    }

    public void fillTransitMatrix(Matrix inVehicle, Matrix inTransit, Matrix autoTravelDistance){

        totalTimeCompleteMatrix = new Matrix (inVehicle.getRowCount(), inVehicle.getColumnCount());
        totalTimeCompleteMatrix.fill(-1F);

        for (int i=1; i<= inVehicle.getRowCount(); i++){
            for (int j =1; j<= inVehicle.getColumnCount(); j++){
                if (inVehicle.getValueAt(i,j)==-1 & i<=j){
                    float tt =Float.MAX_VALUE;
                    float access;
                    float egress;
                    for (Location k : servedZoneList){
                        access = (float)(autoTravelDistance.getValueAt(i, k.getId())/1.4/60);
                        if (access < 60){
                            for (Location l :servedZoneList){
                                egress = (float)(autoTravelDistance.getValueAt(l.getId(),j)/1.4/60);
                                if (egress < 60) {
                                    if (inVehicle.getValueAt(k.getId(),l.getId()) > 0) {
                                        if (tt < inVehicle.getValueAt(k.getId(),l.getId()) + access + egress) {
                                            tt = inVehicle.getValueAt(k.getId(),l.getId()) + access + egress;
                                        }
                                    }
                                }
                            }
                        }


                    }
                    if (tt< 500) {
                        totalTimeCompleteMatrix.setValueAt(i,j,tt);
                        totalTimeCompleteMatrix.setValueAt(j,i,tt);
                    }else {
                        totalTimeCompleteMatrix.setValueAt(i,j,-1F);
                        totalTimeCompleteMatrix.setValueAt(j,i,-1F);
                    }

                }else{
                    totalTimeCompleteMatrix.setValueAt(i,j,inTransit.getValueAt(i,j));
                    totalTimeCompleteMatrix.setValueAt(j,i,inTransit.getValueAt(i,j));
                }

            }
            System.out.println("OD pairs from origin " + i + " completed!");
        }


    }

    public Matrix getInTransitCompleteMatrix() {
        return totalTimeCompleteMatrix;
    }
}

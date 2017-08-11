package org.matsim.munichArea.configMatsim.createDemandPt;

import org.matsim.munichArea.configMatsim.planCreation.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//importing resource bundle have to be changed: therefore this class also have to be changed. No easy.
import static org.matsim.munichArea.MatsimExecuter_DistMatInitTest01.rb;

//import static org.matsim.munichArea.MatsimExecuter.rb;

/**
 * Created by carlloga on 28.02.2017.
 */
public class ReadZonesServedByTransit_DistMatInitTest01 {

    public ArrayList<Location> ReadZonesServedByTransit_DistMatInitTest01(ArrayList<Location> locationList) {
        String fileName = rb.getString("zones.served.SU.file");

        BufferedReader bufferReader = null;

        Map<Integer, Location> zoneMap = new HashMap<>();
        for (Location loc : locationList){
          zoneMap.put(loc.getId(), loc);
        }

        ArrayList<Location> zonesServedList = new ArrayList<>();

        try {
            int lineCount = 1;
            String line;
            bufferReader = new BufferedReader(new FileReader(fileName));

            while ((line = bufferReader.readLine()) != null) {
                if (lineCount > 1) {
                    String[] row = line.split(";");
                    int locationId = Integer.parseInt(row[0]);
                    if (zoneMap.containsKey(locationId)){
                        zonesServedList.add(zoneMap.get(locationId));
                    }
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
}

package org.matsim.munichArea.configMatsim.planCreation;


import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by carlloga on 9/12/2016.
 */
public class CreatePlanXmlStAX {



    public static void createPlan() {


        //read the centroid list
        String workDirectory = "C:/Models/AmberImplementation/input/plans/";
        String fileName = "centroids_test_id_no_header.csv";

        BufferedReader bufferReader = null;
        ArrayList<Location> locationList = new ArrayList<>();

        try {
            String line;
            bufferReader = new BufferedReader(new FileReader(workDirectory + fileName));

            // How to read file in java line by line?

            while ((line = bufferReader.readLine()) != null ) {
                Location location = CentroidsToLocations.CSVtoLocation(line);
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
        int centroids = locationList.size();
        System.out.println("ended input from csv. There are a total of: " + centroids + " centroids");

        ArrayList<Location> origList = locationList;
        ArrayList<Location> destList = locationList;
        //Collections.shuffle(origList);
        //Collections.shuffle(destList);


        int maxOrig = 10;
        int maxDest = 10;
        int origCount;
        int destCount;

        //initialize time
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Long time = 0L ;

        try {
            StringWriter stringWriter = new StringWriter();
            XMLOutputFactory xMLOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter xMLStreamWriter = new IndentingXMLStreamWriter(xMLOutputFactory.createXMLStreamWriter(new FileWriter("input/plansCarlos.xml")));

            xMLStreamWriter.writeStartDocument();

            //xMLStreamWriter.writeCharacters("<!DOCTYPE plans PUBLIC \"plans SYSTEM\" \"http://www.matsim.org/files/dtd/plans_v4.dtd\">");

            xMLStreamWriter.writeDTD("<!DOCTYPE plans PUBLIC \"plans SYSTEM\" \"http://www.matsim.org/files/dtd/plans_v4.dtd\">");

            xMLStreamWriter.writeStartElement("plans");

            destCount = 0;
            for (Location destLoc : destList) {
                destCount++;
                origCount = 0;
                if (destCount < maxDest) {
                    for (Location origLoc : origList) {
                        origCount++;
                        if (origCount < maxOrig) {

                            xMLStreamWriter.writeStartElement("person");
                            xMLStreamWriter.writeAttribute("id", Integer.toString(origLoc.getId()) + "-" + Integer.toString(destLoc.getId()));

                            xMLStreamWriter.writeStartElement("plan");
                            xMLStreamWriter.writeAttribute("type", "car");


                            xMLStreamWriter.writeStartElement("act");
                            xMLStreamWriter.writeAttribute("type", "h");
                            xMLStreamWriter.writeAttribute("x", Double.toString(origLoc.getX()));
                            xMLStreamWriter.writeAttribute("y", Double.toString(origLoc.getY()));
                            String date = df.format(time);
                            xMLStreamWriter.writeAttribute("end_time", date);
                            xMLStreamWriter.writeEndElement();

                            xMLStreamWriter.writeStartElement("leg");
                            xMLStreamWriter.writeAttribute("mode", "car");
                            xMLStreamWriter.writeEndElement();

                            xMLStreamWriter.writeStartElement("act");
                            xMLStreamWriter.writeAttribute("type", "w");
                            xMLStreamWriter.writeAttribute("x", Double.toString(destLoc.getX()));
                            xMLStreamWriter.writeAttribute("y", Double.toString(destLoc.getY()));
                            xMLStreamWriter.writeEndElement();

                            xMLStreamWriter.writeEndElement();
                            xMLStreamWriter.writeEndElement();

                        }
                    }

                    //ad 10 seconds
                    time += 10000L;
                }
            }

            xMLStreamWriter.writeEndDocument();

            xMLStreamWriter.flush();
            xMLStreamWriter.close();

            String xmlString = stringWriter.getBuffer().toString();
            stringWriter.close();
            System.out.println(xmlString);





        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }


        // Output to console for testing
        //StreamResult consoleResult = new StreamResult(System.out);
        //transformer.transform(source, consoleResult);

    }



}

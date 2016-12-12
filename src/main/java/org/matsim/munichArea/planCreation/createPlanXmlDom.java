package org.matsim.munichArea.planCreation;


import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by carlloga on 9/12/2016.
 */
public class createPlanXmlDom {



    public static void createPlanXmlDom() {


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


        int maxOrig = 5429;
        int maxDest = 5429;
        int origCount;
        int destCount;

        //initialize time
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        Long time = 0L ;

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.newDocument();
            //store XML

            Element rootElement = doc.createElement("plans");
            doc.appendChild(rootElement);

            destCount=0;
            for (Location destLoc: destList){
                destCount++;
                origCount = 0;
                if (destCount < maxDest) {
                    for (Location origLoc : origList) {
                        origCount++;
                        if (origCount < maxOrig){

                            Element person = doc.createElement("person");
                            rootElement.appendChild(person);
                            //person
                            Attr id = doc.createAttribute("id");
                            person.setAttributeNode(id);
                            id.setValue(Integer.toString(origLoc.getId())+"-"+Integer.toString(destLoc.getId()));

                            //create plan
                            Element plan = doc.createElement("plan");
                            person.appendChild(plan);
                            Attr type =doc.createAttribute("type");
                            plan.setAttributeNode(type);
                            type.setValue("car");

                            //activity
                            //origin
                            Element act = doc.createElement("act");
                            plan.appendChild(act);
                            Attr typeAct =doc.createAttribute("type");
                            act.setAttributeNode(typeAct);
                            typeAct.setValue("h");

                            Attr x =doc.createAttribute("x");
                            act.setAttributeNode(x);
                            x.setValue(Double.toString(origLoc.getX()));

                            Attr y =doc.createAttribute("y");
                            act.setAttributeNode(y);
                            y.setValue(Double.toString(origLoc.getY()));

                            Attr endTime = doc.createAttribute("end_time");
                            act.setAttributeNode(endTime);
                            String date = df.format(time);
                            endTime.setValue(date);

                            Element leg = doc.createElement("leg");
                            plan.appendChild(leg);

                            Attr mode = doc.createAttribute("mode");
                            leg.setAttributeNode(mode);
                            mode.setValue("car");

                            //destination
                            Element act2 = doc.createElement("act");
                            plan.appendChild(act2);
                            Attr typeAct2 =doc.createAttribute("type");
                            act2.setAttributeNode(typeAct2);
                            typeAct2.setValue("w");

                            Attr x2 =doc.createAttribute("x");
                            act2.setAttributeNode(x2);
                            x2.setValue(Double.toString(destLoc.getX()));

                            Attr y2 =doc.createAttribute("y");
                            act2.setAttributeNode(y2);
                            y2.setValue(Double.toString(destLoc.getY()));
                        }
                    }
                    System.out.println("destination completed" + destCount);
                    //ad 10 seconds
                    time+= 10000L;
                }
            }


            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            DOMImplementation domImpl = doc.getImplementation();
            DocumentType doctype = domImpl.createDocumentType("doctype", "plans SYSTEM" , "http://www.matsim.org/files/dtd/plans_v4.dtd");

            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("C:/Models/AmberImplementation/input/plansCarlos.xml"));
            transformer.transform(source, result);

            // Output to console for testing
            //StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }



}

package org.matsim.example;

import com.pb.common.util.ResourceUtil;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;
import org.matsim.example.planCreation.Location;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import static org.matsim.example.MatsimExecuter.munich;


/**
 * "P" has to do with "Potsdam" and "Z" with "Zurich", but P and Z are mostly used to show which classes belong together.
 */
public class CreateNetwork {

    public static void createNetwork() {

		/*
		 * The input file name.
		 */
        String networkFolder = munich.getString("network.folder");
        String osm = networkFolder + munich.getString("osm.input.file");


		/*
		 * The coordinate system to use. OpenStreetMap uses WGS84, but for MATSim, we need a projection where distances
		 * are (roughly) euclidean distances in meters.
		 *
		 * UTM 33N is one such possibility (for parts of Europe, at least).
		 *
		 */
        /*CoordinateTransformation ct =
                TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:25832");*/

        CoordinateTransformation ct =
                TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:31468");
		/*
		 * First, create a new Config and a new Scenario. One always has to do this when working with the MATSim
		 * data containers.
		 *
		 */
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);




		/*
		 * Pick the Network from the Scenario for convenience.
		 */
        Network network = scenario.getNetwork();

        OsmNetworkReader onr = new OsmNetworkReader(network,ct);
        onr.parse(osm);



        boolean cleanEmpty = ResourceUtil.getBooleanProperty(munich,"clean.empty.link");
        if (cleanEmpty) {
            String emptyLinksFileName = networkFolder + munich.getString("empty.links.file");

            BufferedReader bufferReader = null;
            ArrayList<Integer> emptyLinkList = new ArrayList<>();

            try {
                String line;
                bufferReader = new BufferedReader(new FileReader(emptyLinksFileName));

                while ((line = bufferReader.readLine()) != null) {
                    int emptyLink = Integer.parseInt(line);
                    emptyLinkList.add(emptyLink);
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

            for (int i : emptyLinkList) {
                Id linkId = Id.createLinkId(i);
                network.removeLink(linkId);
            }
        }

        /*
		 * Clean the Network. Cleaning means removing disconnected components, so that afterwards there is a route from every link
		 * to every other link. This may not be the case in the initial network converted from OpenStreetMap.
		 */
        new NetworkCleaner().run(network);

		/*
		 * Write the Network to a MATSim network file.
		 */
        new NetworkWriter(network).write(networkFolder + munich.getString("xml.network.file"));

        System.out.println("MATSIM network created");


    }

}

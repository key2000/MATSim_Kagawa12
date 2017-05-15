package org.matsim.munichArea.multimodal;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.objectattributes.attributable.Attributes;

import java.util.*;

import static org.matsim.munichArea.MatsimExecuter.rb;

/**
 * Created by carlloga on 12-05-17.
 */
public class AddCyclingHighway {
    private ResourceBundle rb;

    public AddCyclingHighway(ResourceBundle rb) {
        this.rb = rb;
    }

    public void addCyclingHighway() {
        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(rb.getString("old.network.file"));

        Scenario scenario = ScenarioUtils.loadScenario(config);



        Network network = scenario.getNetwork();

        //System.out.println(network.getLinks().size());

        Coord fromCoord = new Coord(4470647.527249246, 5343666.593939311);
        Coord toCoord = new Coord(4473369.175665694, 5344983.813056033);

       //todo complete the searh for nodes
        Node fromNode = NetworkUtils.getNearestNode(network, fromCoord);
        Node toNode = NetworkUtils.getNearestNode(network, toCoord);


        //System.out.println(fromNode.getId().toString());

        Link link =  NetworkUtils.createLink(Id.createLinkId("a1"), fromNode, toNode, network, 10, 10, 10000, 1);
        network.addLink(link);

        link =  NetworkUtils.createLink(Id.createLinkId("a2"), toNode, fromNode, network, 10, 10, 10000, 1);
        network.addLink(link);


        new NetworkWriter(network).write(rb.getString("new.network.file"));
    }
}

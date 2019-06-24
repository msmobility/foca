package de.tum.bgu.msm.networkEdition;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

public class PrepareNetworkForCargoBikeMode {

    private static final Logger logger = Logger.getLogger(PrepareNetworkForCargoBikeMode.class);

    static String inputFile = "./networks/matsim/final_V5_emissions.xml.gz";
    static String outputFile = "./networks/matsim/final_V7_emissions.xml.gz";

    static int counter = 0;
    static Set<String> modes = new HashSet<>();

    public static void main(String[] args) {



        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            link.getAttributes().putAttribute("onlyCargoBike", false );
        }

        modes.add(TransportMode.car);

        //small demonstration example for regensburg
        String[] nodesOrigin = new String[]{"33160296","1177219337","38433131"};
        String[] nodesDestination =  new String[]{"247883701","33210235","49511778"};

        for (int i = 0; i < nodesOrigin.length; i++){
            String fromNodeId = nodesOrigin[i];
            String toNodeId = nodesDestination[i];
            createAndAddLinkForCargoBikeOnly(network, fromNodeId, toNodeId);
        }

        new NetworkWriter(network).write(outputFile);

    }

    private static void createAndAddLinkForCargoBikeOnly(Network network, String fromNodeId, String toNodeId){

        counter ++;

        Node fromNode = network.getNodes().get(Id.createNodeId(fromNodeId));
        Node toNode = network.getNodes().get(Id.createNodeId(toNodeId));
        Link link = NetworkUtils.createLink(Id.createLinkId("cargoBikeOnly_" + counter), fromNode,
                toNode, network, NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()),
                30/3.6, 2000, 2);
        link.getAttributes().putAttribute("onlyCargoBike", true );
        link.getAttributes().putAttribute("type", "unclassified" );
        link.setAllowedModes(modes);

        network.addLink(link);

        counter++;

        Link link_2 = NetworkUtils.createLink(Id.createLinkId("cargoBikeOnly_" + counter), toNode,
                fromNode, network, NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()),
                30/3.6, 2000, 2);
        link_2.getAttributes().putAttribute("onlyCargoBike", true );
        link_2.getAttributes().putAttribute("type", "unclassified" );
        link_2.setAllowedModes(modes);

        network.addLink(link_2);




    }

}

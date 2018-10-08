package de.tum.bgu.msm.networkEdition;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.*;

public class NetworkMergeTool {

    private String roadNetowrkFileName;
    private String transitNetworkFileName;
    private String finalNetowrkFileName;

    public static void main (String[] args){

        String file1 = "networks/matsim/germany.xml.gz";
        String file2 = "networks/matsim/europe.xml.gz";

        String outputFile = "networks/matsim/merge.xml.gz";

        NetworkMergeTool networkMergeTool = new NetworkMergeTool(file1, file2, outputFile);
        networkMergeTool.mergeNetworks();
    }

    public NetworkMergeTool(String roadNetowrkFileName, String transitNetworkFileName, String finalNetowrkFileName) {
        this.roadNetowrkFileName = roadNetowrkFileName;
        this.transitNetworkFileName = transitNetworkFileName;
        this.finalNetowrkFileName = finalNetowrkFileName;
    }

    public void mergeNetworks(){

        Config config1 = ConfigUtils.createConfig();
        config1.network().setInputFile(roadNetowrkFileName);

        Scenario scenario1 = ScenarioUtils.loadScenario(config1);
        Network network1 = scenario1.getNetwork();

        Config config2 = ConfigUtils.createConfig();
        config2.network().setInputFile(transitNetworkFileName);

        Scenario scenario2 = ScenarioUtils.loadScenario(config2);
        Network network2 = scenario2.getNetwork();

        Network finalNetwork = merge(network1, network2);

        new NetworkWriter(finalNetwork).write(finalNetowrkFileName);

    }

    private Network merge(Network baseNetwork, Network addNetwork) {

        NetworkFactory factory = baseNetwork.getFactory();
        Set<Id<Node>> nodesInBaseNetwork = new HashSet<>();
        baseNetwork.getNodes().keySet().forEach(nodeId -> {nodesInBaseNetwork.add(nodeId);});

        Iterator addNetowrkIterator = addNetwork.getNodes().values().iterator();

        int counter = 0;

        while(addNetowrkIterator.hasNext()) {
            Node node = (Node)addNetowrkIterator.next();
            Node node2 = factory.createNode(Id.create(node.getId().toString(), Node.class), node.getCoord());
            if (!nodesInBaseNetwork.contains(node2.getId())){
                baseNetwork.addNode(node2);
                counter++;
            }
        }

        System.out.println("ADDED " + counter + " NODES.");

        addNetowrkIterator = addNetwork.getLinks().values().iterator();

        counter = 0;
        while(addNetowrkIterator.hasNext()) {
            Link link = (Link)addNetowrkIterator.next();
            Id<Node> fromNodeId = Id.create(link.getFromNode().getId().toString(), Node.class);
            Id<Node> toNodeId = Id.create(link.getToNode().getId().toString(), Node.class);
            Node fromNode = baseNetwork.getNodes().get(fromNodeId);
            Node toNode = baseNetwork.getNodes().get(toNodeId);
            Link link2 = factory.createLink(Id.create(link.getId().toString() + "_2", Link.class), fromNode, toNode);
            if(!nodesInBaseNetwork.contains(fromNodeId) ||
                    !nodesInBaseNetwork.contains(toNodeId)) {
                link2.setAllowedModes(link.getAllowedModes());
                link2.setCapacity(link.getCapacity());
                link2.setFreespeed(link.getFreespeed());
                link2.setLength(link.getLength());
                link2.setNumberOfLanes(link.getNumberOfLanes());
                baseNetwork.addLink(link2);
                counter++;
            }
        }
        System.out.println("ADDED " + counter + " LINKS.");

        return baseNetwork;

    }


}

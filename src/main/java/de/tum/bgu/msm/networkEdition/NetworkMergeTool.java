package de.tum.bgu.msm.networkEdition;



import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.*;

public class NetworkMergeTool {

    public static void main (String[] args){

        String file1 = "networks/matsim/europe_v2.xml.gz";
        String file2 = "networks/matsim/germany.xml.gz";
        String file3 = "networks/matsim/munich.xml.gz";
        String file4 = "networks/matsim/regensburg.xml.gz";

        List<String> inputFiles = new ArrayList<>();
        inputFiles.add(file1);
        inputFiles.add(file2);
        inputFiles.add(file3);
        inputFiles.add(file4);

        String outputFile = "networks/matsim/final_v4.xml.gz";

        NetworkMergeTool networkMergeTool = new NetworkMergeTool();
        networkMergeTool.mergeNetworks(inputFiles, outputFile);
    }


    public void mergeNetworks(List<String> inputFiles, String outputFile){

        Config config0 = ConfigUtils.createConfig();
        config0.network().setInputFile(inputFiles.get(0));
        Scenario scenario0 = ScenarioUtils.loadScenario(config0);
        Network finalNetwork = scenario0.getNetwork();

        for (int i =1; i < inputFiles.size(); i++ ) {
            Config config = ConfigUtils.createConfig();
            config.network().setInputFile(inputFiles.get(i));
            String suffix = "_" + (i + 1);
            Scenario scenario = ScenarioUtils.loadScenario(config);
            Network thisNetwork = scenario.getNetwork();
            finalNetwork = merge(finalNetwork, thisNetwork, suffix);
        }

        new NetworkWriter(finalNetwork).write(outputFile);

    }

    /**
     *
     * @param baseNetwork
     * @param addNetwork
     * @param suffix to add to the links of addnetwork so their names do not repeat the ones of base network
     * @return
     */
    private Network merge(Network baseNetwork, Network addNetwork, String suffix) {

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
            Link link2 = factory.createLink(Id.create(link.getId().toString() + suffix, Link.class), fromNode, toNode);
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
            for (String key : link.getAttributes().getAsMap().keySet()){
                link2.getAttributes().putAttribute(key, link.getAttributes().getAsMap().get(key));
            }


        }
        System.out.println("ADDED " + counter + " LINKS using the suffix " + suffix);

        return baseNetwork;

    }


}

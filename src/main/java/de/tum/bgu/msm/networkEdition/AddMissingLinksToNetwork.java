package de.tum.bgu.msm.networkEdition;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.collections.Tuple;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddMissingLinksToNetwork {

    public static void main(String[] args) {

        Network network = NetworkUtils.readNetwork(args[0]);

        List<Tuple<String, String>> nodeToNodeList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(args[1]));

            br.readLine(); //read headers and ignore line
            String line;
            while ((line = br.readLine()) != null){

                String[] splitLine = line.split(",");

                nodeToNodeList.add(new Tuple<>(splitLine[0], splitLine[1]));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //a reference link from which the attributes will be copied
        Link referenceLink = network.getLinks().get(Id.createLinkId(args[2]));


        for (Tuple<String, String> nodeToNode : nodeToNodeList) {

            Node fromNode = network.getNodes().get(Id.createNodeId(nodeToNode.getFirst()));
            Node toNode = network.getNodes().get(Id.createNodeId(nodeToNode.getSecond()));

            String id = nodeToNode.getFirst() + "to" + nodeToNode.getSecond();

            Link link = NetworkUtils.createLink(Id.createLinkId(id), fromNode, toNode, network, NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()), referenceLink.getFreespeed(), referenceLink.getCapacity(), referenceLink.getNumberOfLanes());
            link.setAllowedModes(referenceLink.getAllowedModes());
            referenceLink.getAttributes().getAsMap().forEach( (key,attribute) -> {
                link.getAttributes().putAttribute(key, attribute);
            });


            String opposingId = nodeToNode.getSecond() + "to" + nodeToNode.getFirst();

            Link opposingDirectionLink = NetworkUtils.createLink(Id.createLinkId(opposingId), toNode, fromNode, network, NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()), referenceLink.getFreespeed(), referenceLink.getCapacity(), referenceLink.getNumberOfLanes());
            link.setAllowedModes(referenceLink.getAllowedModes());
            referenceLink.getAttributes().getAsMap().forEach( (key,attribute) -> {
                opposingDirectionLink.getAttributes().putAttribute(key, attribute);
            });

            network.addLink(link);
            network.addLink(opposingDirectionLink);

        }

        NetworkUtils.writeNetwork(network, args[3]);

    }

}

package de.tum.bgu.msm.networkEdition;

import de.tum.bgu.msm.freight.io.GenericCSVReader;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
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
import sun.nio.ch.Net;

import java.lang.reflect.Field;
import java.util.*;

public class CreateNetworkFromCsv {

    private static Logger logger = Logger.getLogger(CreateNetworkFromCsv.class);

    private final static Set<String> ALLOWED_MODES = new HashSet<>(Arrays.asList("bike"));
    private final static double CAPACITY = 1000;
    private final static int LANES = 2;
    private final static double FREE_SPEED = 20/3.6;

    public static void main(String args[]) {

        Config config0 = ConfigUtils.createConfig();
        config0.network().setInputFile("./networks/matsim/muc_all.xml.gz");
        Scenario scenario0 = ScenarioUtils.loadScenario(config0);
        Network network = scenario0.getNetwork();


        CreateNetworkFromCsv createNetworkFromCsv = new CreateNetworkFromCsv();
        Map<Integer, MyNode> bicycleNodes = createNetworkFromCsv.readNodes();
        Map<Integer, MyLink> bicycleLinks = createNetworkFromCsv.readLinks();

        Map<Integer, Node> matsimNodes = new HashMap<>();
        bicycleNodes.values().forEach(bicycleNode -> {

            Node closestNode = NetworkUtils.getNearestNode(network, bicycleNode.getCoord());
            if (NetworkUtils.getEuclideanDistance(closestNode.getCoord(), bicycleNode.getCoord()) < 20){
                matsimNodes.put(bicycleNode.getId(), closestNode);
            } else {
                Node newNode = NetworkUtils.createNode(Id.createNodeId(bicycleNode.getId() + "b"), bicycleNode.getCoord());
                matsimNodes.put(bicycleNode.getId(), newNode);
                network.addNode(newNode);
            }

        });

        bicycleLinks.values().forEach(bicycleLink -> {
            Node fromNode = matsimNodes.get(bicycleLink.getOrigin());
            Node toNode = matsimNodes.get(bicycleLink.getDestination());

            if (NetworkUtils.getConnectingLink(fromNode, toNode) == null){
                Link link = NetworkUtils.createLink(Id.createLinkId(bicycleLink.getId() + "b"),
                       fromNode, toNode, network,
                        NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()),
                        FREE_SPEED,
                        CAPACITY, LANES );
                link.setAllowedModes(ALLOWED_MODES);
                network.addLink(link);
            }
        });

        new NetworkWriter(network).write("./networks/matsim/muc_all_bicycle_v2.xml.gz");

    }

    public Map<Integer, MyNode> readNodes() {
        Map<Integer, MyNode> nodes = new HashMap<>();
        GenericCSVReader reader = new GenericCSVReader(null, getFields(MyNode.class), "./networks/input/bicycle_network_main/nodes_gk4_with_closest.csv", ",");
        reader.readAndReturnResults().values().forEach(x-> {
            MyNode n = new MyNode(x);
            nodes.put(n.getId(), n);
            });

        return nodes;
    }


    public Map<Integer, MyLink> readLinks() {

        Map<Integer, MyLink> links = new HashMap<>();
        GenericCSVReader reader = new GenericCSVReader(null, getFields(MyLink.class), "./networks/input/bicycle_network_main/links.csv", ",");
        reader.readAndReturnResults().values().forEach(x-> {
            MyLink l = new MyLink(x);
            links.put(l.getId(), l);
        });

        return links;

    }


    public static List<String> getFields(Class thisClass) {
        List<String> myFields = new ArrayList<>();
        for (Field field : thisClass.getDeclaredFields()) {
            myFields.add(field.getName());
        }
        return myFields;
    }




}

class MyNode {
    private int id;
    private double xcoord;
    private double ycoord;

    public MyNode(Map<String, String> valuesAsString) {
        this.id = Integer.parseInt(valuesAsString.get("id"));
        this.xcoord = Double.parseDouble(valuesAsString.get("xcoord"));
        this.ycoord = Double.parseDouble(valuesAsString.get("ycoord"));
    }

    public int getId() {
        return id;
    }

    public double getXcoord() {
        return xcoord;
    }

    public double getYcoord() {
        return ycoord;
    }

    public Coord getCoord(){
        return new Coord(xcoord, ycoord);
    }

}

class MyLink {
    private int id;
    private int origin;
    private int destination;
    private String stree_name;

    public MyLink(Map<String, String> valuesAsString) {
        this.id = Integer.parseInt(valuesAsString.get("id"));
        this.origin = Integer.parseInt(valuesAsString.get("origin"));
        this.destination = Integer.parseInt(valuesAsString.get("destination"));
        this.stree_name = valuesAsString.get("stree_name\n");
    }

    public int getId() {
        return id;
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public String getStree_name() {
        return stree_name;
    }
}

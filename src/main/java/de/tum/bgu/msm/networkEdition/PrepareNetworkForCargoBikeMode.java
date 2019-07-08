package de.tum.bgu.msm.networkEdition;

import de.tum.bgu.msm.util.MitoUtil;
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
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static de.tum.bgu.msm.networkEdition.AssignHbefaRoadTypes.getHbefaType;

public class PrepareNetworkForCargoBikeMode {

    private static final Logger logger = Logger.getLogger(PrepareNetworkForCargoBikeMode.class);

    static String inputFile = "./networks/matsim/final_V8_emissions.xml.gz";
    static String outputFile = "./networks/matsim/final_V9_emissions.xml.gz";

    static String newNodeFileName = "./networks/input/cargo_bike_new_network/new_nodes_cargo_bike_muc.csv";
    static String newLinkFileName = "./networks/input/cargo_bike_new_network/new_links_cargo_bike_muc.csv";

    static int counter = 0;
    static Set<String> modes = new HashSet<>();

    public static void main(String[] args) throws IOException {


        //todo careful with links id and nodes id if this process is done more than once.

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            if (link.getAttributes().getAttribute("onlyCargoBike").equals(null)) {
                link.getAttributes().putAttribute("onlyCargoBike", false);
            }
        }

        modes.add(TransportMode.car);

        BufferedReader br = new BufferedReader(new FileReader(newNodeFileName));
        String[] header =  br.readLine().split(",");
        int idIndex = MitoUtil.findPositionInArray("NODE",header);
        int xIndex = MitoUtil.findPositionInArray("xcoord",header);
        int yIndex = MitoUtil.findPositionInArray("ycoord",header);

        String line;
        while( (line = br.readLine()) != null){
            createAndAddNode(network,
                    line.split(",")[idIndex],
                    Double.parseDouble(line.split(",")[xIndex]),
                    Double.parseDouble(line.split(",")[yIndex]));
        }

        br.close();
        logger.info("Completed the reading of nodes");

        br = new BufferedReader(new FileReader(newLinkFileName));
        header =  br.readLine().split(",");
        int linkIdIndex = MitoUtil.findPositionInArray("LINK",header);
        int nodeIdIndex = MitoUtil.findPositionInArray("NODE",header);

        //todo not tested completely!
        while( (line = br.readLine()) != null){
            String line2 = br.readLine();

            String fromNodeId = line.split(",")[nodeIdIndex];
            String toNodeId = line2.split(",")[nodeIdIndex];

            String linkId;
            if ((linkId = line.split(",")[linkIdIndex]).equals(line2.split(",")[linkIdIndex])){
                createAndAddLinkForCargoBikeOnly(network, linkId, fromNodeId, toNodeId);
            } else {
                throw new RuntimeException("The file of links is not well formatted");
            }

        }

        logger.info("Completed the reading of links");

        new NetworkCleaner().run(network);

        new NetworkWriter(network).write(outputFile);

    }

    private static void createAndAddNode(Network network, String id, double x, double y){
        NetworkUtils.createAndAddNode(network, Id.createNodeId(id), new Coord(x,y));
    }

    private static void createAndAddLinkForCargoBikeOnly(Network network, String linkId, String fromNodeId, String toNodeId){

        counter ++;

        Node fromNode = network.getNodes().get(Id.createNodeId(fromNodeId));
        Node toNode = network.getNodes().get(Id.createNodeId(toNodeId));
        Link link = NetworkUtils.createLink(Id.createLinkId("cb-" + linkId), fromNode,
                toNode, network, NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()),
                30/3.6, 2000, 2);
        link.getAttributes().putAttribute("onlyCargoBike", true );
        link.getAttributes().putAttribute("type", "unclassified" );
        link.setAllowedModes(modes);
        link.getAttributes().putAttribute("hbefa_road_type", getHbefaType(link));

        network.addLink(link);


        Link link_2 = NetworkUtils.createLink(Id.createLinkId("cb-" + linkId + "-2"), toNode,
                fromNode, network, NetworkUtils.getEuclideanDistance(fromNode.getCoord(), toNode.getCoord()),
                30/3.6, 2000, 2);
        link_2.getAttributes().putAttribute("onlyCargoBike", true );
        link_2.getAttributes().putAttribute("type", "unclassified" );
        link_2.setAllowedModes(modes);
        link_2.getAttributes().putAttribute("hbefa_road_type", getHbefaType(link));


        network.addLink(link_2);




    }

}

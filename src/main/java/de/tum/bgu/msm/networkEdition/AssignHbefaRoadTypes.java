package de.tum.bgu.msm.networkEdition;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

public class AssignHbefaRoadTypes {

    private static final Logger logger = Logger.getLogger(AssignHbefaRoadTypes.class);

    static String inputFile = "./networks/matsim/final_V8_emissions.xml.gz";
    static String outputFile = "./networks/matsim/final_V8_emissions.xml.gz";

/**
    * handled OSM road types:
            *    motorway,trunk,primary,secondary, tertiary, unclassified,residential,service
 *    motorway_link, trunk_link,primary_link, secondary_link
 *    tertiary_link, living_street, pedestrian,track,road
 *
            * Hbefa categories and respective speeds
            *    URB/MW-Nat./80 - 130
            *    URB/MW-City/60 - 110
            *    URB/Trunk-Nat./70 - 110
            *    URB/Trunk-City/50 - 90
            *    URB/Distr/50 - 80
            *    URB/Local/50 - 60
            *    URB/Access/30 - 50
            *
            * Conversions from OSM to hbefa types
 *    motorway;MW
 *    primary;Trunk
 *    secondary;Distr
 *    tertiary;Local
 *    residential;Access
 *    living;Access

    **/

    public static void main(String args[]) {


        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            link.getAttributes().putAttribute("hbefa_road_type", getHbefaType(link));
        }

        new NetworkWriter(network).write(outputFile);

    }

    public static String getHbefaType(Link link) {

        double freeFlowSpeed_kmh = link.getFreespeed()*3.6;
        String osmType = link.getAttributes().getAttribute("type").toString().split("_")[0];

        String type = "";
        String speedRange= "";

        if (osmType.equals("motorway")){
            if (freeFlowSpeed_kmh > 80) {
                type = "MW-Nat.";
                speedRange = "80";
            } else if (freeFlowSpeed_kmh > 60){
                type = "MW-City";
                speedRange = "60";
            } else {
                logger.error("Road classified with lower cat: " + osmType + " - " + freeFlowSpeed_kmh);
                type = "Distr";
                speedRange = "50";
            }
        } else if (osmType.equals("primary") || osmType.equals("trunk")){
            if (freeFlowSpeed_kmh >= 70) {
                type = "Trunk-Nat.";
                speedRange = "70";
            } else if (freeFlowSpeed_kmh >= 50){
                type = "Trunk-City";
                speedRange = "50";
            } else {
                logger.error("Road classified with lower cat: " + osmType + " - " + freeFlowSpeed_kmh);
                type = "Distr";
                speedRange = "50";
            }
        } else if (osmType.equals("secondary")){
            type = "Distr";
            speedRange = "50";
        } else if (osmType.equals("tertiary")){
            type = "Local";
            speedRange = "50";
        } else if (osmType.equals("residential") ||
                osmType.equals("access") ||
                osmType.equals("service") ||
                osmType.equals("unclassified")){
            type = "Access";
            speedRange = "30";
        } else {
            logger.error(osmType + " - " + freeFlowSpeed_kmh);
            throw new RuntimeException("road type not known");
        }



        return "URB/" + type + "/" + speedRange;
    }

}

package de.tum.bgu.msm.networkEdition;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashSet;
import java.util.Set;

public class AssignHbefaRoadTypes {

    static String inputFile = "./networks/matsim/regensburg_multimodal.xml";

    static String outputFile = "./networks/matsim/regensburg_multimodal_compatible_emissions.xml";


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

    private static String getHbefaType(Link link) {

        return "URB/Local/50";
    }

}

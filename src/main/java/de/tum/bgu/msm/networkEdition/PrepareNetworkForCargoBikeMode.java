package de.tum.bgu.msm.networkEdition;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class PrepareNetworkForCargoBikeMode {

    private static final Logger logger = Logger.getLogger(PrepareNetworkForCargoBikeMode.class);

    static String inputFile = "./networks/matsim/final_V5_emissions.xml.gz";
    static String outputFile = "./networks/matsim/final_V6_emissions.xml.gz";


    public static void main(String[] args) {

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            link.getAttributes().putAttribute("onlyCargoBike", false );
        }

        new NetworkWriter(network).write(outputFile);




    }

}

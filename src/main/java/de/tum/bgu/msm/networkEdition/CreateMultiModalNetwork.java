package de.tum.bgu.msm.networkEdition;

import de.tum.bgu.msm.freight.data.Commodity;
import de.tum.bgu.msm.freight.data.CommodityGroup;
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

public class CreateMultiModalNetwork {

    static String inputFile = "./networks/matsim/final_v2.xml.gz";;

    static String outputFile = "./networks/matsim/final_v3.xml.gz";;

    static Set<String> modes = new HashSet<>();


    public static void main(String args[]) {

        modes.add(TransportMode.car);
        modes.add(TransportMode.truck);


        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            link.setAllowedModes(modes);
        }

        new NetworkWriter(network).write(outputFile);

    }

}

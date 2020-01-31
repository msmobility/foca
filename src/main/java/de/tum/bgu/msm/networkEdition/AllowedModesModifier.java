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

public class AllowedModesModifier {

    static String inputFile = "./networks/matsim/final_V10_emissions.xml.gz";
    ;

    static String outputFile = "./networks/matsim/final_V11_emissions.xml.gz";
    ;


    public static void main(String args[]) {


        Set<String> carAndTruck = new HashSet<>();
        carAndTruck.add(TransportMode.car);
        carAndTruck.add(TransportMode.truck);

        Set<String> bikeOnly = new HashSet<>();
        bikeOnly.add("TransportMode.bike");

        Set<String> carAndBike = new HashSet<>();
        carAndBike.add("TransportMode.bike");
        carAndBike.add(TransportMode.car);

        Set<String> allModes = new HashSet<>();
        allModes.add("TransportMode.bike");
        allModes.add(TransportMode.car);
        allModes.add(TransportMode.truck);


        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(inputFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Network network = scenario.getNetwork();

        for (Link link : network.getLinks().values()) {
            String type = (String) link.getAttributes().getAttribute("type");
            boolean onlyCargoBike = (boolean) link.getAttributes().getAttribute("onlyCargoBike");

            if (onlyCargoBike) {
                link.setAllowedModes(bikeOnly);
            } else if (type.contains("motorway") || type.contains("trunk")) {
                link.setAllowedModes(carAndTruck);
            } else {
                link.setAllowedModes(allModes);
            }

        }



        new NetworkWriter(network).write(outputFile);

    }


}

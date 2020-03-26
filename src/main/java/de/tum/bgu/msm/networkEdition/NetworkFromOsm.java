package de.tum.bgu.msm.networkEdition;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

public class NetworkFromOsm {

    public static void main (String[] args){

        /*
         * Provide the following arguments:
         * 0: input file from osm
         * 1: outfile name
         * 2: integer with the hierarchy level
         */
        String networkFolder = "./networks/";
        String osm = networkFolder +  args[0];

        String outputFile = "matsim/" + args[1] + ".xml.gz";

        boolean networkCleaning = true;


        /*
         * The coordinate system to use. OpenStreetMap uses WGS84, but for MATSim, we need a projection where distances
         * are (roughly) euclidean distances in meters.
         *
         * UTM 33N is one such possibility (for parts of Europe, at least).
         *
         */
        /*CoordinateTransformation ct =
                TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:25832");*/

        CoordinateTransformation ct =
                TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);


        //31468 is the coordinate system DHDN_3 zone 4
        /*
         * First, create a new Config and a new Scenario. One always has to do this when working with the MATSim
         * data containers.
         *
         */
        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);


        /*
         * Pick the Network from the Scenario for convenience.
         */
        Network network = scenario.getNetwork();

        OsmNetworkReader onr = new OsmNetworkReader(network,ct);
        onr.setHierarchyLayer(Integer.parseInt(args[2]));
        //onr.setKeepPaths(true);
        onr.parse(osm);

        /*
         * Clean the Network. Cleaning means removing disconnected components, so that afterwards there is a route from every link
         * to every other link. This may not be the case in the initial network converted from OpenStreetMap.
         */

        if (networkCleaning){
            new NetworkCleaner().run(network);
        }

        /*
         * Write the Network to a MATSim network file.
         */
        new NetworkWriter(network).write(networkFolder + outputFile);

        System.out.println("MATSIM network created");



    }


}

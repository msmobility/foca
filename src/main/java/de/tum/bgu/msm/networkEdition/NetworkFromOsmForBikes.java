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

public class NetworkFromOsmForBikes {

    public static void main (String[] args){

        /*
         * The input file name.
         */
        String networkFolder = "./networks/";
        String osm = networkFolder +  "output/muc-all.osm";

        String outputFile = "matsim/muc_all_bike.xml.gz";

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


        /*
         * Set highway defaults for the cycle paths
         * experimental!
         */

        onr.setHighwayDefaults(7, "service", 1, 25/3.6, 1,  1000);


        onr.setHighwayDefaults(7, "cycleway", 1, 25/3.6, 1,  1000);
        onr.setHighwayDefaults(7, "path", 1, 15/3.6, 1,  1000);
        //onr.setHighwayDefaults(7, "pedestrian", 1, 15/3.6, 1,  1000);
        //onr.setHighwayDefaults(8, "footway", 1, 15/3.6, 1,  1000);


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

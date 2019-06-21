package de.tum.bgu.msm.freight;

import de.tum.bgu.msm.freight.properties.Properties;
import junitx.framework.FileAssert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;

public class FreightFlowsTest {

    @Test
    public void testRegensburg() {

        Properties properties = new Properties();

        properties.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties.setRunId("testReg");
        //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");
        properties.setSelectedZones(new int[]{9362});
        properties.setFlowsScaleFactor(1.0);
        properties.setTruckScaleFactor(0.1);
        properties.setSampleFactorForParcels(1.0);
        properties.setIterations(5);
        properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");

        //properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.);

        try {
            properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FreightFlows freightFlows = new FreightFlows();
        freightFlows.run(properties);


        String originalFile = properties.getOutputFolder() + properties.getRunId() + "/parcels.csv";
        String finalFile = "./reference/" + properties.getRunId() + "/parcels.csv";

        FileAssert.assertEquals("Files are different", new File(originalFile), new File(finalFile));

    }

}

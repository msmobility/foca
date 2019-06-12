package de.tum.bgu.msm.freight;

import de.tum.bgu.msm.freight.properties.Properties;
import junitx.framework.FileAssert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class FreightFlowsTest {

    @Test
    public void testRegensburg() {

        Properties properties = new Properties();

        properties.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties.setRunId("testReg");
        properties.setNetworkFile("./networks/matsim/regensburg_multimodal.xml.gz");
        properties.setSelectedZones(new int[]{9362});
        properties.setFlowsScaleFactor(1);
        properties.setTruckScaleFactor(0.05);
        properties.setSampleFactorForParcels(0.05);
        properties.setIterations(10);
        properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");

        properties.logProperties();

        FreightFlows freightFlows = new FreightFlows();
        freightFlows.run(properties);


        String originalFile = properties.getOutputFolder() + properties.getRunId() + "/parcels.csv";
        String finalFile = "./reference/" + properties.getRunId() + "/parcels.csv";

        FileAssert.assertEquals("Files are different", new File(originalFile), new File(finalFile));

    }

}

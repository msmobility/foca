package de.tum.bgu.msm.freight;

import de.tum.bgu.msm.freight.properties.Properties;
import junitx.framework.FileAssert;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlowsTest {


    class Scenario {
        public int iterations;
        String runId;
        double shareOfCargoBikes;
        String distributionCenterFile;
        double scaleTrucks;
        double scaleParcels;

        public Scenario(String runId,
                        double shareOfCargoBikes,
                        String distributionCenterFile,
                        double scaleTrucks,
                        double scaleParcels,
                        int iterations) {
            this.runId = runId;
            this.shareOfCargoBikes = shareOfCargoBikes;
            this.distributionCenterFile = distributionCenterFile;
            this.scaleTrucks = scaleTrucks;
            this.scaleParcels = scaleParcels;
            this.iterations = iterations;
        }
    }

    @Test
    public void runVariousScenarios() {

        List<Scenario> scenarios = new ArrayList<>();
        scenarios.add(new Scenario("testReg",
                1.0,
                "./input/distributionCenters/distributionCenters.xml", 1., 1., 50));

        scenarios.add(new Scenario("testReg_2",
                1.0,
                "./input/distributionCenters/distributionCenters_scenario2.xml", 1. ,1., 50));

        scenarios.add(new Scenario("testRegNoCargoBikes",
                0.0,
                "./input/distributionCenters/distributionCenters.xml",1.,1., 50));

        for (Scenario scenario : scenarios) {
            testRegensburg(scenario, false);
        }

    }

    @Test
    public void runSmallScenario() {

        List<Scenario> scenarios = new ArrayList<>();
        scenarios.add(new Scenario("test_small",
                1.0,
                "./input/distributionCenters/distributionCenters.xml", 0.05, 0.05, 1));

        for (Scenario scenario : scenarios) {
            testRegensburg(scenario, true);
        }

    }


    public void testRegensburg(Scenario scenario, boolean check) {

        Properties properties = new Properties();

        properties.setMatrixFileName("./input/matrices/ketten-2010-filtered.csv");
        properties.setRunId(scenario.runId);
        //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");
        properties.setSelectedZones(new int[]{9362});
        properties.setFlowsScaleFactor(1.0);
        properties.setTruckScaleFactor(scenario.scaleTrucks);
        properties.setSampleFactorForParcels(scenario.scaleParcels);
        properties.setIterations(scenario.iterations);
        properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");
        properties.setVehicleFileForParcelDelivery("./input/vehicleTypesForParcelDelivery.xml");

        properties.setDistributionCentersFile(scenario.distributionCenterFile);

        properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(scenario.shareOfCargoBikes);

        try {
            properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FreightFlows freightFlows = new FreightFlows();
        freightFlows.run(properties);

        if(check) {

            String originalFile;
            String finalFile;

            originalFile = properties.getOutputFolder() + properties.getRunId() + "/truckFlows.csv";
            finalFile = "./reference/" + properties.getRunId() + "/truckFlows.csv";

            FileAssert.assertEquals("Flows are different", new File(originalFile), new File(finalFile));

            originalFile = properties.getOutputFolder() + properties.getRunId() + "/ld_trucks.csv";
            finalFile = "./reference/" + properties.getRunId() + "/ld_trucks.csv";

            FileAssert.assertEquals("LD trucks are different", new File(originalFile), new File(finalFile));

            originalFile = properties.getOutputFolder() + properties.getRunId() + "/sd_trucks.csv";
            finalFile = "./reference/" + properties.getRunId() + "/sd_trucks.csv";

            FileAssert.assertEquals("SD trucks are different", new File(originalFile), new File(finalFile));

            originalFile = properties.getOutputFolder() + properties.getRunId() + "/parcels.csv";
            finalFile = "./reference/" + properties.getRunId() + "/parcels.csv";

            FileAssert.assertEquals("Parcel files are different", new File(originalFile), new File(finalFile));


            originalFile = properties.getOutputFolder() + properties.getRunId() + "/counts_multi_day.csv";
            finalFile = "./reference/" + properties.getRunId() + "/counts_multi_day.csv";

            FileAssert.assertEquals("Counts are different", new File(originalFile), new File(finalFile));

        }
    }

}

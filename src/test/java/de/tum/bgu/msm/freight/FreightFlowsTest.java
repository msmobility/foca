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

        List<Properties> listOfProperties = new ArrayList<>();

        {
            Properties properties = new Properties(Properties.initializeResourceBundleFromFile(null));
            properties.flows().setMatrixFolder("./input/matrices/");
            properties.setRunId("testReg");
            //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");
            properties.setAnalysisZones(new int[]{9362});
            properties.flows().setFlowsScaleFactor(1.0);
            properties.setTruckScaleFactor(0.05);
            properties.setSampleFactorForParcels(0.05);
            properties.setIterations(50);
            properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");
            properties.setVehicleFileForParcelDelivery("./input/vehicleTypesForParcelDelivery.xml");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCentersReg.csv");
            //listOfProperties.add(properties);
        }
        {
            Properties properties = new Properties(Properties.initializeResourceBundleFromFile(null));
            properties.flows().setMatrixFolder("./input/matrices/");
            properties.setRunId("testReg_2");
            //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");
            properties.setAnalysisZones(new int[]{9362});
            properties.flows().setFlowsScaleFactor(1.0);
            properties.setTruckScaleFactor(0.05);
            properties.setSampleFactorForParcels(0.05);
            properties.setIterations(50);
            properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");
            properties.setVehicleFileForParcelDelivery("./input/vehicleTypesForParcelDelivery.xml");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCentersReg_2.csv");
            listOfProperties.add(properties);
        }
        {
            Properties properties =new Properties(Properties.initializeResourceBundleFromFile(null));
            properties.flows().setMatrixFolder("./input/matrices/");
            properties.setRunId("testRegNoCargoBikes");
            //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");
            properties.setAnalysisZones(new int[]{9362});
            properties.flows().setFlowsScaleFactor(1.0);
            properties.setTruckScaleFactor(0.05);
            properties.setSampleFactorForParcels(0.05);
            properties.setIterations(50);
            properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");
            properties.setVehicleFileForParcelDelivery("./input/vehicleTypesForParcelDelivery.xml");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCentersReg.csv");
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.);
            listOfProperties.add(properties);
        }

        for (Properties properties : listOfProperties) {
            testScenario(properties, false);
        }

    }

    @Test
    public void runSmallScenario() {


        Properties properties = new Properties(Properties.initializeResourceBundleFromFile(null));
        properties.flows().setMatrixFolder("./input/matrices/");
        properties.setRunId("test_small");
        //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");
        properties.setAnalysisZones(new int[]{9362});
        properties.flows().setFlowsScaleFactor(1.0);
        properties.setTruckScaleFactor(0.05);
        properties.setSampleFactorForParcels(0.05);
        properties.setIterations(1);
        properties.setCountStationLinkListFile("./input/matsim_links_stations_all_regensburg.csv");
        properties.setVehicleFileForParcelDelivery("./input/vehicleTypesForParcelDelivery.xml");
        properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters.xml");

        testScenario(properties, true);

    }


    public void testScenario(Properties properties, boolean check) {

        try {
            properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FreightFlowsMucRunScenarios freightFlows = new FreightFlowsMucRunScenarios();
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

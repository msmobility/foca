package de.tum.bgu.msm.emission;

import de.tum.bgu.msm.freight.properties.Properties;
import org.junit.Test;

import java.io.FileNotFoundException;

public class EmissionMainTest {


    @Test
    public void testRegensburgEmissions(){
        Properties properties = new Properties();
        properties.setRunId("testReg");
        String configFile = "config_average.xml";
        //properties.setNetworkFile("./networks/matsim/regensburg_multimodal_compatible_emissions.xml");

        String outDirectory = "./output/" + properties.getRunId() + "/matsim/";
        String eventFileWithoutEmissions = outDirectory + properties.getRunId() + ".output_events.xml.gz";
        String eventFileWithEmissions = outDirectory + properties.getRunId() + ".output_events_emissions.xml.gz";
        String individualVehicleFile = outDirectory + "vehicleList.xml";
        String populationFile = outDirectory + properties.getRunId() + ".output_plans.xml.gz";
        String networkFile = properties.getNetworkFile();

        String linkWarmEmissionFile = "./output/" + properties.getRunId() + "/linkWarmEmissionFile.csv";
        String vehicleWarmEmissionFile = "./output/" + properties.getRunId() + "/vehicleWarmEmissionFile.csv";
        String vehicleColdEmissionFile = "./output/" + properties.getRunId() + "/vehicleColdEmissionFile.csv";

        CreateVehicles createVehicles = new CreateVehicles();
        createVehicles.run(eventFileWithoutEmissions, individualVehicleFile);

        OfflineEmissionAnalysis offlineEmissionAnalysis = new OfflineEmissionAnalysis();
        offlineEmissionAnalysis.run(configFile,
                outDirectory,
                eventFileWithoutEmissions,
                eventFileWithEmissions,
                individualVehicleFile,
                populationFile,
                networkFile);

        EmissionEventsAnalysis emissionEventsAnalysis = new EmissionEventsAnalysis();
        try {
            emissionEventsAnalysis.run(configFile,
                    outDirectory,
                    eventFileWithEmissions,
                    individualVehicleFile,
                    populationFile,
                    networkFile,
                    linkWarmEmissionFile,
                    vehicleWarmEmissionFile,
                    vehicleColdEmissionFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}

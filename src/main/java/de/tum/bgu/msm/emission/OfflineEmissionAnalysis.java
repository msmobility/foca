package de.tum.bgu.msm.emission;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.emissions.EmissionModule;
import org.matsim.contrib.emissions.utils.EmissionsConfigGroup;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Injector;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.algorithms.EventWriterXML;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;
import java.nio.file.Path;

/**
 * Class to calculate emisions from events (copied from the Emission extension example folder)
 */
public class OfflineEmissionAnalysis {




    private Config config;

    // =======================================================================================================

    public static void main (String[] args){

        String baseDirectory = "c:/models/freightFlows/";
        String outDirectory = baseDirectory + "./output/testReg/matsim/";
        String configFile =  baseDirectory + "config_average.xml";

        String eventsFile =  baseDirectory +  "./output/testReg/matsim/testReg.output_events.xml.gz";
        // (remove dependency of one test/execution path from other. kai/ihab, nov'18)

        String emissionEventOutputFile = baseDirectory +  "./output/testReg/matsim/testReg.output_events_emissions.xml.gz";
        String  vehicleFile = baseDirectory + "./output/testReg/matsim/vehicleList.xml";
        String populationFile = baseDirectory + "./output/testReg/matsim/testReg.output_plans.xml.gz";

        String networkFile = baseDirectory +  "/networks/matsim/regensburg_multimodal_compatible_emissions.xml";

        OfflineEmissionAnalysis offlineEmissionAnalysis = new OfflineEmissionAnalysis();
        offlineEmissionAnalysis.run(configFile, outDirectory, eventsFile, emissionEventOutputFile, vehicleFile, populationFile, networkFile);
    }


    public void run(String configFile, String outDirectory, String eventsFileWithoutEmissions, String eventsFileWithEmission,
                    String individualVehicleFile, String populationFile, String networkFile) {
        if ( config==null ) {
            this.prepareConfig(configFile, outDirectory, individualVehicleFile, networkFile, populationFile) ;
        }
        Scenario scenario = ScenarioUtils.loadScenario(config);
        EventsManager eventsManager = EventsUtils.createEventsManager();

        AbstractModule module = new AbstractModule(){
            @Override
            public void install(){
                bind( Scenario.class ).toInstance( scenario );
                bind( EventsManager.class ).toInstance( eventsManager );
                bind( EmissionModule.class ) ;
            }
        };

        com.google.inject.Injector injector = Injector.createInjector(config, module);

        EmissionModule emissionModule = injector.getInstance(EmissionModule.class);

        EventWriterXML emissionEventWriter = new EventWriterXML(eventsFileWithEmission);
        emissionModule.getEmissionEventsManager().addHandler(emissionEventWriter);

        MatsimEventsReader matsimEventsReader = new MatsimEventsReader(eventsManager);
        matsimEventsReader.readFile(eventsFileWithoutEmissions);

        emissionEventWriter.closeFile();

    }




    public Config prepareConfig(String configFile, String outDirectory, String vehicleFile, String networkFile, String populationFile ) {
        config = ConfigUtils.loadConfig(configFile, new EmissionsConfigGroup());
        config.controler().setOutputDirectory(outDirectory);
        config.vehicles().setVehiclesFile(vehicleFile);
        config.network().setInputFile(networkFile);
        config.plans().setInputFile(populationFile);
        return config;
    }



}

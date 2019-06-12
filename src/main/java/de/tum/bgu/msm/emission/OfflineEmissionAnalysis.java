package de.tum.bgu.msm.emission;

import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.emissions.EmissionModule;
import org.matsim.contrib.emissions.example.RunAverageEmissionToolOfflineExample;
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

/**
 * Class to calculate emisions from events (copied from the Emission extension example folder)
 */
public class OfflineEmissionAnalysis {


    private final static String baseDirectory = "c:/models/freightFlows/";
    private final static String outDirectory = "./output/testReg/matsim/";
    private static final String configFile =  baseDirectory + "input/emissions/config_average.xml";

    private static final String eventsFile =  baseDirectory +  "./output/testReg/matsim/testReg.output_events.xml.gz";
    // (remove dependency of one test/execution path from other. kai/ihab, nov'18)

    private static final String emissionEventOutputFile = baseDirectory +  "./output/testReg/matsim/testReg.output_events_emissions.xml.gz";
    private static final String vehicleFile = baseDirectory + "./output/testReg/matsim/vehicleList.xml";
    private static final String populationFile = baseDirectory + "./output/testReg/matsim/testReg.output_plans.xml.gz";

    private static final String networkFile = baseDirectory +  "/networks/matsim/regensburg_multimodal_compatible_emissions.xml";

    private Config config;

    // =======================================================================================================

    public static void main (String[] args){
        OfflineEmissionAnalysis offlineEmissionAnalysis = new OfflineEmissionAnalysis();
        offlineEmissionAnalysis.run();
    }

    public Config prepareConfig() {
        config = ConfigUtils.loadConfig(configFile, new EmissionsConfigGroup());
        config.controler().setOutputDirectory(baseDirectory + outDirectory);
        config.vehicles().setVehiclesFile(vehicleFile);
        config.network().setInputFile(networkFile);
        config.plans().setInputFile(populationFile);
        return config;
    }

    public void run() {
        if ( config==null ) {
            this.prepareConfig() ;
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

        EventWriterXML emissionEventWriter = new EventWriterXML(emissionEventOutputFile);
        emissionModule.getEmissionEventsManager().addHandler(emissionEventWriter);

        MatsimEventsReader matsimEventsReader = new MatsimEventsReader(eventsManager);
        matsimEventsReader.readFile(eventsFile);

        emissionEventWriter.closeFile();

    }

}

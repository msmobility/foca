package de.tum.bgu.msm.emission;

import de.tum.bgu.msm.emission.data.AnalyzedLink;
import de.tum.bgu.msm.emission.data.AnalyzedVehicle;
import de.tum.bgu.msm.emission.data.Pollutant;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
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
import org.matsim.vehicles.Vehicle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Map;

public class EmissionAnalysis {

    public static Config config;
    private final static String baseDirectory = "c:/models/freightFlows/";
    private final static String outDirectory = "./output/testReg/matsim/";
    private static final String configFile = baseDirectory + "input/emissions/config_average.xml";

    private static final String eventsFile = baseDirectory + "./output/testReg/matsim/testReg.output_events.xml.gz";
    // (remove dependency of one test/execution path from other. kai/ihab, nov'18)

    private static final String emissionEventOutputFile = baseDirectory + "./output/testReg/matsim/testReg.output_events_emissions.xml.gz";
    private static final String vehicleFile = baseDirectory + "./output/testReg/matsim/vehicleList.xml";
    private static final String populationFile = baseDirectory + "./output/testReg/matsim/testReg.output_plans.xml.gz";

    private static final String networkFile = baseDirectory + "/networks/matsim/regensburg_multimodal_compatible_emissions.xml";

    private static final String linkWarmEmissionFile = baseDirectory + "/output/testReg/linkWarmEmissions.csv";
    private static final String vehicleWarmEmissionFile = baseDirectory + "/output/testReg/vehicleWarmEmissions.csv";


    public static void main(String[] args) throws FileNotFoundException {

        if (config == null) {
            prepareConfig();
        }

        Scenario scenario = ScenarioUtils.loadScenario(config);

        EventsManager eventsManager = EventsUtils.createEventsManager();


        AbstractModule module = new AbstractModule() {
            @Override
            public void install() {
                bind(Scenario.class).toInstance(scenario);
                bind(EventsManager.class).toInstance(eventsManager);
                bind(EmissionModule.class);
            }
        };

        com.google.inject.Injector injector = Injector.createInjector(config, module);

        EmissionModule emissionModule = injector.getInstance(EmissionModule.class);


        LinkEmissionHandler linkEmissionHandler = new LinkEmissionHandler(scenario.getNetwork());
        emissionModule.getEmissionEventsManager().addHandler(linkEmissionHandler);

        MatsimEventsReader matsimEventsReader = new MatsimEventsReader(eventsManager);
        matsimEventsReader.readFile(args[0]);

        Map<Id<Link>, AnalyzedLink> analyzedLinks = linkEmissionHandler.getEmmisionsByLink();
        Map<Id<Vehicle>, AnalyzedVehicle> analyzedVehicles = linkEmissionHandler.getEmmisionsByVehicle();


        printOutLinkWarmEmissions(linkWarmEmissionFile, analyzedLinks, true);
        printOutVehicleWarmEmissions(vehicleWarmEmissionFile, analyzedVehicles, true);


    }

    private static void printOutVehicleWarmEmissions(String fileName,
                                                     Map<Id<Vehicle>, AnalyzedVehicle> analyzedVehicles,
                                                     boolean warm) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(new File(fileName));

        StringBuilder header = new StringBuilder();
        header.append("link,distance");
        for (Pollutant pollutant : Pollutant.values()) {
            header.append(",").append(pollutant.toString());
        }
        pw.println(header);

        for (AnalyzedVehicle vehicle : analyzedVehicles.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(vehicle.getId().toString()).append(",");
            sb.append(vehicle.getDistanceTravelled());

            for (Pollutant pollutant : Pollutant.values()) {
                if (warm) {
                    sb.append(",").append(vehicle.getWarmEmissions().get(pollutant.toString()));
                } else {
                    sb.append(",").append(vehicle.getColdEmissions().get(pollutant.toString()));
                }

            }

            pw.println(sb);
        }

        pw.close();

    }

    private static void printOutLinkWarmEmissions(String fileName,
                                                  Map<Id<Link>, AnalyzedLink> analyzedLinks,
                                                  boolean warm) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(new File(fileName));

        StringBuilder header = new StringBuilder();
        header.append("link,length");
        for (Pollutant pollutant : Pollutant.values()) {
            header.append(",").append(pollutant.toString());
        }
        pw.println(header);

        for (AnalyzedLink link : analyzedLinks.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(link.getId().toString()).append(",");
            sb.append(link.getMatsimLink().getLength());

            for (Pollutant pollutant : Pollutant.values()) {
                if (warm){
                    sb.append(",").append(link.getWarmEmissions().get(pollutant.toString()));
                } else {
                    sb.append(",").append(link.getColdEmissions().get(pollutant.toString()));
                }
            }

            pw.println(sb);
        }

        pw.close();

    }


    public static Config prepareConfig() {
        config = ConfigUtils.loadConfig(configFile, new EmissionsConfigGroup());
        config.controler().setOutputDirectory(baseDirectory + outDirectory);
        config.vehicles().setVehiclesFile(vehicleFile);
        config.network().setInputFile(networkFile);
        config.plans().setInputFile(populationFile);
        return config;
    }

}

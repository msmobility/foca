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

public class EmissionEventsAnalysis {

    public static Config config;


    public static void main(String[] args) throws FileNotFoundException {

        String baseDirectory = "c:/models/freightFlows/";
        String outDirectory = baseDirectory + "./output/testReg/matsim/";
        String configFile = baseDirectory + "config_average.xml";

        String eventsFile = baseDirectory + "./output/testReg/matsim/testReg.output_events.xml.gz";


        String emissionEventOutputFile = baseDirectory + "./output/testReg/matsim/testReg.output_events_emissions.xml.gz";
        String vehicleFile = baseDirectory + "./output/testReg/matsim/vehicleList.xml";
        String populationFile = baseDirectory + "./output/testReg/matsim/testReg.output_plans.xml.gz";

        String networkFile = baseDirectory + "/networks/matsim/regensburg_multimodal_compatible_emissions.xml";

        String linkWarmEmissionFile = baseDirectory + "/output/testReg/linkWarmEmissions.csv";
        String vehicleWarmEmissionFile = baseDirectory + "/output/testReg/vehicleWarmEmissions.csv";


        EmissionEventsAnalysis emissionEventsAnalysis = new EmissionEventsAnalysis();
        emissionEventsAnalysis.run(configFile,
                outDirectory,
                emissionEventOutputFile,
                vehicleFile,
                populationFile,
                networkFile,
                linkWarmEmissionFile,
                vehicleWarmEmissionFile);


    }


    public void run(String configFile,
                    String outDirectory,
                    String eventsFileWithEmission,
                    String individualVehicleFile,
                    String populationFile,
                    String networkFile,
                    String linkWarmEmissionFile,
                    String vehicleWarmEmissionFile) throws FileNotFoundException {
        if (config == null) {
            this.prepareConfig(configFile, outDirectory, individualVehicleFile, networkFile, populationFile) ;
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
        emissionModule.getEmissionEventsManager().

                addHandler(linkEmissionHandler);

        MatsimEventsReader matsimEventsReader = new MatsimEventsReader(eventsManager);
        matsimEventsReader.readFile(eventsFileWithEmission);

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
        header.append("id,distance");
        for (Pollutant pollutant : Pollutant.values()) {
            header.append(",").append(pollutant.toString());
        }
        pw.println(header);

        for (AnalyzedVehicle vehicle : analyzedVehicles.values()) {
            StringBuilder sb = new StringBuilder();
            sb.append(vehicle.getId().toString().replace("_truck", "")).append(",");
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
                if (warm) {
                    sb.append(",").append(link.getWarmEmissions().get(pollutant.toString()));
                } else {
                    sb.append(",").append(link.getColdEmissions().get(pollutant.toString()));
                }
            }

            pw.println(sb);
        }

        pw.close();

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

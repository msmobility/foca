package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.input.LinksFileReader;
import de.tum.bgu.msm.freight.io.output.CountEventHandler;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import java.io.IOException;

public class MultiDayCounts {

    public static void main (String[] args) throws IOException {

        DataSet dataSet = new DataSet();

        String linksFile = args[0];

        for (int i = 1; i < args.length; i++){

            String scenario = args [i];

            String eventsFile = "./output/" + scenario + "/matsim/" + scenario  + ".output_events.xml.gz";
            String countsFile = "./output/" + scenario + "/matsim/counts.csv";


            Properties propertiesForStandAloneEventManager = new Properties(Properties.initializeResourceBundleFromFile(args[0]));
            propertiesForStandAloneEventManager.setIterations(0);

            EventsManager eventsManager = EventsUtils.createEventsManager();
            CountEventHandler countEventHandler = new CountEventHandler(propertiesForStandAloneEventManager);
            LinksFileReader linksFileReader = new LinksFileReader(dataSet, linksFile);
            linksFileReader.read();

            for (Id linkId : dataSet.getObservedCounts().keySet()) {
                countEventHandler.addLinkById(linkId);
            }

            eventsManager.addHandler(countEventHandler);
            new MatsimEventsReader(eventsManager).readFile(eventsFile);
            countEventHandler.printOutCounts(countsFile);
        }




    }


}

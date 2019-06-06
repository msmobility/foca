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

        String eventsFile = args[0];
        String linksFile = args[1];
        String countsFile = args[2];

        Properties propertiesForStandAloneEventManager = new Properties();
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

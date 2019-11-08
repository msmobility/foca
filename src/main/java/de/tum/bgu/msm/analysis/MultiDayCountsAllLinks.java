package de.tum.bgu.msm.analysis;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.input.LinksFileReader;
import de.tum.bgu.msm.freight.io.output.CountEventHandler;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.network.NetworkUtils;

import java.io.IOException;

public class MultiDayCountsAllLinks {

    public static void main (String[] args) throws IOException {

        String eventsFile = args[0];
        String netwtorkFile = args[1];
        String countsFile = args[2];

        Properties propertiesForStandAloneEventManager = new Properties();
        propertiesForStandAloneEventManager.setIterations(0);

        Network network = NetworkUtils.readNetwork(netwtorkFile);

        EventsManager eventsManager = EventsUtils.createEventsManager();
        CountEventHandler countEventHandler = new CountEventHandler(propertiesForStandAloneEventManager);


        for (Link link : network.getLinks().values()) {
            if (link.getId().toString().contains("uam")) {
                countEventHandler.addLinkById(link.getId());
            }
        }

        eventsManager.addHandler(countEventHandler);
        new MatsimEventsReader(eventsManager).readFile(eventsFile);
        countEventHandler.printOutCounts(countsFile);

    }


}

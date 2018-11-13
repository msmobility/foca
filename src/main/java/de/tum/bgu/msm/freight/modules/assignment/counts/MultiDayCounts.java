package de.tum.bgu.msm.freight.modules.assignment.counts;

import org.matsim.api.core.v01.Id;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class MultiDayCounts {

    public static void main (String[] args) throws IOException {

        String eventsFile = args[0];
        String linksFile = args[1];
        String countsFile = args[2];

        EventsManager eventsManager = EventsUtils.createEventsManager();
        CountEventHandler countEventHandler = new CountEventHandler();
        LinksFileReader linksFileReader = new LinksFileReader(null, linksFile);
        linksFileReader.read();

        for (String linkId : linksFileReader.getListOfIds()) {
            countEventHandler.addLinkById(linkId);
        }

        eventsManager.addHandler(countEventHandler);
        new MatsimEventsReader(eventsManager).readFile(eventsFile);

        Map<Id,Integer> counts = countEventHandler.getMapOfCOunts();

        printOutCounts(countsFile, counts);



    }

    public static void printOutCounts(String countsFile, Map<Id, Integer> counts) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(countsFile));

        pw.println("link,count");

        for (Id id : counts.keySet()){
            pw.println(id.toString() + "," + counts.get(id));
        }

        pw.close();

    }
}

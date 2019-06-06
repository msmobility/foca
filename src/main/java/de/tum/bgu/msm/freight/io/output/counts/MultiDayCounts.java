package de.tum.bgu.msm.freight.io.output.counts;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.input.LinksFileReader;
import de.tum.bgu.msm.freight.properties.Properties;
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

        Map<Id, Map<Integer, Integer>>  counts = countEventHandler.getMapOfCOunts();

        printOutCounts(countsFile, counts);



    }

    public static void printOutCounts(String countsFile, Map<Id, Map<Integer, Integer>>  counts) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(countsFile));

        pw.println("link,hour,count");

        for (Id id : counts.keySet()){
            Map<Integer, Integer> countsByHour = counts.get(id);
            for (int hour : countsByHour.keySet()){
                pw.println(id.toString() + "," +
                        hour + "," +
                        countsByHour.get(hour));
            }
        }

        pw.close();

    }
}

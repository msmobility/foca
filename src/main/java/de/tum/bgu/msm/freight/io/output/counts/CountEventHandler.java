package de.tum.bgu.msm.freight.io.output.counts;

import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;

import java.util.HashMap;
import java.util.Map;


public class CountEventHandler implements LinkEnterEventHandler {

    private final int LAST_HOUR = 49;
    private Properties properties;

    private static Logger logger = Logger.getLogger(CountEventHandler.class);

    public CountEventHandler(Properties properties){
        this.properties = properties;
    }

    private int thisIteration;

    private Map<Id, Map<Integer, Integer>> listOfSelectedLinks = new HashMap<>();

    private int getHourFromTime(double time_s){
        return (int) (time_s / 3600) > (LAST_HOUR-1) ? LAST_HOUR : (int) Math.floor(time_s / 3600) ;
    }

    public void addLinkById(Id linkId){
        Map<Integer, Integer> countsByHour = new HashMap<>();
        for (int i = 0; i < LAST_HOUR+1; i++){
            countsByHour.put(i, 0);
        }
        listOfSelectedLinks.put(linkId,countsByHour);
    }

    public Map<Id, Map<Integer, Integer>>  getMapOfCOunts(){
        return listOfSelectedLinks;
    }

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        Id id  = linkEnterEvent.getLinkId();
        int hour = getHourFromTime(linkEnterEvent.getTime());
        if (listOfSelectedLinks.containsKey(id) && thisIteration == properties.getIterations()){
            listOfSelectedLinks.get(id).put(hour, listOfSelectedLinks.get(id).get(hour) + 1);
        }
    }

    @Override
    public void reset(int iteration) {
        this.thisIteration = iteration;
        for (Id id : listOfSelectedLinks.keySet()){
            addLinkById(id);
        }
        logger.info("Reset event handler at iteration " + thisIteration);
    }
}

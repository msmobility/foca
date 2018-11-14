package de.tum.bgu.msm.freight.modules.assignment.counts;

import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;

import javax.validation.constraints.Null;
import java.util.HashMap;
import java.util.Map;


public class CountEventHandler implements LinkEnterEventHandler {

    private Properties properties;

    private static Logger logger = Logger.getLogger(CountEventHandler.class);

    public CountEventHandler(Properties properties){
        this.properties = properties;
    }

    private int thisIteration;

    private Map<Id, Integer> listOfSelectedLinks = new HashMap<>();

    public void addLinkById(Id linkId){
        listOfSelectedLinks.put(linkId,0);
    }

    public Map<Id, Integer> getMapOfCOunts(){
        return listOfSelectedLinks;
    }

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        Id id  = linkEnterEvent.getLinkId();
        if (listOfSelectedLinks.containsKey(id) && thisIteration == properties.getIterations()){
            listOfSelectedLinks.put(id, listOfSelectedLinks.get(id) + 1);
        }
    }

    @Override
    public void reset(int iteration) {
        this.thisIteration = iteration;
        for (Id id : listOfSelectedLinks.keySet()){
            listOfSelectedLinks.put(id,0);
        }
        logger.info("Reset event handler at iteration " + thisIteration);
    }
}

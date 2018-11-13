package de.tum.bgu.msm.freight.modules.assignment.counts;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.network.Link;

import javax.validation.constraints.Null;
import java.util.HashMap;
import java.util.Map;

public class CountEventHandler implements LinkEnterEventHandler {

    private Map<Id, Integer> listOfSelectedLinks = new HashMap<>();

    public void addLinkById(String linkId){
        listOfSelectedLinks.put(Id.createLinkId(linkId),0);
    }

    public Map<Id, Integer> getMapOfCOunts(){
        return listOfSelectedLinks;
    }

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        Id id  = linkEnterEvent.getLinkId();
        if (listOfSelectedLinks.containsKey(id)){
            listOfSelectedLinks.put(id, listOfSelectedLinks.get(id) + 1);
        }
    }

    @Override
    public void reset(int iteration) {
        listOfSelectedLinks.values().parallelStream().forEach(x -> x = 0);
    }
}

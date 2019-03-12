package de.tum.bgu.msm.freight.data.freight;

import java.util.HashMap;
import java.util.Map;


public class OriginDestinationPair {

    private int origin;
    private int destination;

    private Map<Segment,Flow> flows;

    public OriginDestinationPair(int origin, int destination) {
        this.origin = origin;
        this.destination = destination;
        this.flows = new HashMap<>();
    }

    public void addFlow(Flow Flow){
        this.flows.put(Flow.getSegment(), Flow);
    }

    public Map<Segment, Flow> getFlows(){
        return flows;
    }
}

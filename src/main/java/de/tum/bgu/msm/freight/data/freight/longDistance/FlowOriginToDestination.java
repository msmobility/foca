package de.tum.bgu.msm.freight.data.freight.longDistance;

import java.util.HashMap;
import java.util.Map;

/**
 * A origin destination pair of a certain commodity from first origin to last destination
 */
public class FlowOriginToDestination {

    private int origin;
    private int destination;

    private Map<SegmentType,FlowSegment> flows;

    public FlowOriginToDestination(int origin, int destination) {
        this.origin = origin;
        this.destination = destination;
        this.flows = new HashMap<>();
    }

    public void addFlow(FlowSegment FlowSegment){
        this.flows.put(FlowSegment.getSegmentType(), FlowSegment);
    }

    public Map<SegmentType, FlowSegment> getFlowSegments(){
        return flows;
    }
}

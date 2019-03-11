package de.tum.bgu.msm.freight.data;

import java.util.HashMap;
import java.util.Map;


public class OriginDestinationPair {

    private int origin;
    private int destination;

    private Map<Segment,Flow> trips;

    public OriginDestinationPair(int origin, int destination) {
        this.origin = origin;
        this.destination = destination;
        this.trips = new HashMap<>();
    }

    public void addTrip(Flow Flow){
        this.trips.put(Flow.getSegment(), Flow);
    }

    public Map<Segment, Flow> getTrips(){
        return trips;
    }
}

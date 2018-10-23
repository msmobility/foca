package de.tum.bgu.msm.freight.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OrigDestFlow {

    private int year;
    private int origin;
    private int destination;

    private Map<Segment,Trip> trips;

    public OrigDestFlow(int year, int origin, int destination) {
        this.year = year;
        this.origin = origin;
        this.destination = destination;
        this.trips = new HashMap<>();
    }

    public void addTrip(Trip trip){
        this.trips.put(trip.getSegment(),trip);
    }

    public Map<Segment, Trip> getTrips(){
        return trips;
    }
}

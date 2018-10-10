package de.tum.bgu.msm.freight.data;

import java.util.ArrayList;


public class OrigDestFlow {

    private int year;
    private int origin;
    private int destination;
    private Mode mode;
    private Commodity commodity;
    private double volume_tn;
    private ArrayList<Trip> trips;

    public OrigDestFlow(int year, int origin, int destination, Mode mode, Commodity commodity, double volume_tn) {
        this.year = year;
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
        this.commodity = commodity;
        this.volume_tn = volume_tn;
        this.trips = new ArrayList<>();
    }

    public void addTrip(Trip trip){
        this.trips.add(trip);
    }

    public ArrayList<Trip> getTrips(){
        return trips;
    }
}

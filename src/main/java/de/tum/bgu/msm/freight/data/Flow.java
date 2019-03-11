package de.tum.bgu.msm.freight.data;

import org.matsim.api.core.v01.Coord;

import java.util.ArrayList;
import java.util.Map;

public class Flow {

    private int origin;
    private int destination;
    private Mode mode;
    private Commodity commodity;
    private double volume_tn;
    private Segment segment;
    private FlowType flowType;

    private ArrayList<TruckTrip> truckTrips = new ArrayList<TruckTrip>();

    //these attributes are optional only for the trips that are converted to trucks
    private double distance_km;
    private int loadedTrucks;
    private int emptyTrucks;
    private double tt_s;


    public Flow(int origin, int destination, Mode mode, Commodity commodity, double volume_tn, Segment segment, FlowType flowType) {
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
        this.commodity = commodity;
        this.volume_tn = volume_tn;
        this.segment = segment;
        this.flowType = flowType;
    }

    public int getOrigin() {
        return origin;
    }

    public int getDestination() {
        return destination;
    }

    public Mode getMode() {
        return mode;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public double getVolume_tn() {
        return volume_tn;
    }

    public Segment getSegment() {
        return segment;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public double getDistance_km() {
        return distance_km;
    }

    public void setDistance_km(double distance_km) {
        this.distance_km = distance_km;
    }

    public int getLoadedTrucks() {
        return loadedTrucks;
    }

    public void setLoadedTrucks(int loadedTrucks) {
        this.loadedTrucks = loadedTrucks;
    }

    public int getEmptyTrucks() {
        return emptyTrucks;
    }

    public void setEmptyTrucks(int emptyTrucks) {
        this.emptyTrucks = emptyTrucks;
    }

    public double getTt_s() {
        return tt_s;
    }

    public void setTt_s(double tt_s) {
        this.tt_s = tt_s;
    }

    public ArrayList<TruckTrip> getTruckTrips() {
        return truckTrips;
    }

    public void addTruckTrip(TruckTrip truckTrip) {
        this.truckTrips.add(truckTrip);
    }
}

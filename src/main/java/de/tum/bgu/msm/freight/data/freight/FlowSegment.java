package de.tum.bgu.msm.freight.data.freight;

import java.util.ArrayList;

/**
 * A flow segment is a segment of a flowOriginToDestination
 */
public class FlowSegment {

    private int origin;
    private int destination;
    private int originTerminal = -1;
    private int destinationTerminal = -1;
    private Mode mode;
    private Commodity commodity;
    private double volume_tn;
    private SegmentType segmentType;
    private FlowType flowType;

    private ArrayList<LongDistanceTruckTrip> longDistanceTruckTrips = new ArrayList<LongDistanceTruckTrip>();

    //these attributes are optional only for the trips that are converted to trucks
    private double distance_km;
    private int loadedTrucks;
    private int emptyTrucks;
    private double tt_s;


    public FlowSegment(int origin, int destination, Mode mode, Commodity commodity, double volume_tn, SegmentType segmentType, FlowType flowType) {
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
        this.commodity = commodity;
        this.volume_tn = volume_tn;
        this.segmentType = segmentType;
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

    public SegmentType getSegmentType() {
        return segmentType;
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

    public ArrayList<LongDistanceTruckTrip> getLongDistanceTruckTrips() {
        return longDistanceTruckTrips;
    }

    public void addTruckTrip(LongDistanceTruckTrip longDistanceTruckTrip) {
        this.longDistanceTruckTrips.add(longDistanceTruckTrip);
    }

    public int getOriginTerminal() {
        return originTerminal;
    }

    public void setOriginTerminal(int originTerminal) {
        this.originTerminal = originTerminal;
    }

    public int getDestinationTerminal() {
        return destinationTerminal;
    }

    public void setDestinationTerminal(int destinationTerminal) {
        this.destinationTerminal = destinationTerminal;
    }
}

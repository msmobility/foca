package de.tum.bgu.msm.freight.data;

import java.util.Map;

public class Trip {

    private int origin;
    private int destination;
    private Mode mode;
    private Commodity commodity;
    private double volume_tn;
    private Segment segment;

    public Trip(int origin, int destination, Mode mode, Commodity commodity, double volume_tn, Segment segment) {
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
        this.commodity = commodity;
        this.volume_tn = volume_tn;
        this.segment = segment;
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
}

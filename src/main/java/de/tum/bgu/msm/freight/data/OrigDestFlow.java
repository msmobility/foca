package de.tum.bgu.msm.freight.data;

public class OrigDestFlow {

    private int year;
    private int origin;
    private int destination;
    private Mode mode;
    private Commodity commodity;
    private double volume_tn;

    public OrigDestFlow(int year, int origin, int destination, Mode mode, Commodity commodity, double volume_tn) {
        this.year = year;
        this.origin = origin;
        this.destination = destination;
        this.mode = mode;
        this.commodity = commodity;
        this.volume_tn = volume_tn;
    }

    public int getYear() {
        return year;
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

    public double getVolume_tn() {
        return volume_tn;
    }

    public Commodity getCommodity() {
        return commodity;
    }
}

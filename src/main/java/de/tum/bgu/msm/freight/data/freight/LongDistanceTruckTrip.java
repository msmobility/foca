package de.tum.bgu.msm.freight.data.freight;

import org.matsim.api.core.v01.Coord;

public class LongDistanceTruckTrip {
    private Coord origCoord;
    private Coord destCoord;
    private FlowSegment FlowSegment;
    private boolean load;

    public LongDistanceTruckTrip(Coord origCoord, Coord destCoord, FlowSegment FlowSegment, boolean load) {
        this.origCoord = origCoord;
        this.destCoord = destCoord;
        this.FlowSegment = FlowSegment;
        this.load = load;
    }

    public Coord getOrigCoord() {
        return origCoord;
    }

    public Coord getDestCoord() {
        return destCoord;
    }

}

package de.tum.bgu.msm.freight.data.freight;

import org.matsim.api.core.v01.Coord;

public class LongDistanceTruckTrip {
    private Coord origCoord;
    private Coord destCoord;
    private Flow Flow;
    private boolean load;

    public LongDistanceTruckTrip(Coord origCoord, Coord destCoord, Flow Flow, boolean load) {
        this.origCoord = origCoord;
        this.destCoord = destCoord;
        this.Flow = Flow;
        this.load = load;
    }

    public Coord getOrigCoord() {
        return origCoord;
    }

    public Coord getDestCoord() {
        return destCoord;
    }

}

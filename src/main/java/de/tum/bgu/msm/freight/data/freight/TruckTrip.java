package de.tum.bgu.msm.freight.data.freight;

import org.matsim.api.core.v01.Coord;

public interface TruckTrip {
    Coord getOrigCoord();

    Coord getDestCoord();
}

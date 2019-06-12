package de.tum.bgu.msm.freight.data.freight;

import org.locationtech.jts.geom.Coordinate;

public interface TruckTrip {
    Coordinate getOrigCoord();

    Coordinate getDestCoord();
}

package de.tum.bgu.msm.freight.data.freight;

import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Id;

public interface TruckTrip {

    Id<TruckTrip> getId();

    Coordinate getOrigCoord();

    Coordinate getDestCoord();

    void setAssigned(boolean assigned);
}

package de.tum.bgu.msm.freight.data;

import org.matsim.api.core.v01.Coord;

public interface Zone {

    /**
     * get random coordinate whithin a zone. If the zone has internal zones, there is a disaggregation
     * to microzones and then a random coordinate. If the commodity is null the disaggregation is completely random.
     * @param commodity
     * @return
     */
    Coord getCoordinates(Commodity commodity);

    String getName();

    int getId();

    boolean isInStudyArea();
}

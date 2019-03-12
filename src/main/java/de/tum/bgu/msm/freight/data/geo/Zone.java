package de.tum.bgu.msm.freight.data.geo;

import org.matsim.api.core.v01.Coord;
import sun.security.x509.AlgorithmId;

public interface Zone {

    /**
     * get random coordinate whithin a zone.
     * @return
     */
    Coord getCoordinates();

    String getName();

    int getId();

    /**
     * @return true if the zone is in the study area - destination or origin is at one of the selected cities
     */
    boolean isInStudyArea();

}

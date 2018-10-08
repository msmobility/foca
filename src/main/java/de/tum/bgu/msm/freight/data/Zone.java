package de.tum.bgu.msm.freight.data;

import org.matsim.api.core.v01.Coord;

public interface Zone {

    Coord getRandomCoord();

    String getName();
}

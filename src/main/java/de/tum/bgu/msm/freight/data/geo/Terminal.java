package de.tum.bgu.msm.freight.data.geo;

import org.matsim.api.core.v01.Coord;

/**
 * A terminal is a distribution center where the goods end or start the pre-carriage or on-carriage to be transferred from/to road to other modes
 */
public class Terminal {

    private int id;
    private String name;
    private Coord coord_gk4;
    private boolean isInStudyArea;

    public Terminal(int id, String name, Coord coord_gk4, boolean isInStudyArea) {
        this.id = id;
        this.name = name;
        this.coord_gk4 = coord_gk4;
        this.isInStudyArea = isInStudyArea;
    }

    public Coord getCoordinates() {
        return coord_gk4;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }


}

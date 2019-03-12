package de.tum.bgu.msm.freight.data.geo;

import org.matsim.api.core.v01.Coord;

/**
 * A terminal is a distribution center where the goods end or start the pre-carriage or on-carriage to be transferred from/to road to other modes
 */
public class Terminal implements Zone {

    private int id;
    private String name;
    private Coord coord;
    private boolean isInStudyArea;

    public Terminal(int id, String name, Coord coord, boolean isInStudyArea) {
        this.id = id;
        this.name = name;
        this.coord = coord;
        this.isInStudyArea = isInStudyArea;
    }

    @Override
    public Coord getCoordinates() {
        return coord;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isInStudyArea() {
        return isInStudyArea;
    }
}

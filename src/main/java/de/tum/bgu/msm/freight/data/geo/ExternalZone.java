package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.locationtech.jts.geom.Coordinate;

public class ExternalZone implements Zone {

    private int id;
    private String name;
    private Coordinate coord_gk4;

    public ExternalZone(int id, String name, double x, double y) {
        this.id = id;
        this.name = name;
        this.coord_gk4 =  new Coordinate(x, y);
    }


    @Override
    public Coordinate getCoordinates() {
        return coord_gk4;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isInStudyArea() {
        return false;
    }



}

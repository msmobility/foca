package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.matsim.api.core.v01.Coord;

public class ExternalZone implements Zone {

    private int id;
    private String name;
    private Coord coord_gk4;

    public ExternalZone(int id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        Coord coord = new Coord(lon, lat);
        this.coord_gk4 = FreightFlowUtils.convertWGS84toGK4(coord);
    }


    @Override
    public Coord getCoordinates() {
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

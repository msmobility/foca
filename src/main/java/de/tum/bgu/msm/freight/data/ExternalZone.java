package de.tum.bgu.msm.freight.data;

import de.tum.bgu.msm.data.Region;
import de.tum.bgu.msm.freight.data.Zone;
import org.matsim.api.core.v01.Coord;

public class ExternalZone implements Zone {

    private int id;
    private String name;
    private double lat;
    private double lon;

    public ExternalZone(int id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }


    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public Coord getRandomCoord() {
        return new Coord(lat, lon);
    }
}

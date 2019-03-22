package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import org.matsim.api.core.v01.Coord;

import java.util.Objects;

/**
 * A distribution center is a location where goods are transferred between one truck to other trucks
 */
public class DistributionCenter implements Zone{

    private final int id;
    private final String name;
    private final Coord coord_gk4;
    private final CommodityGroup commodityGroup;
    private final int zoneId;

    public DistributionCenter(int id, String name, Coord coord_gk4, CommodityGroup commodityGroup, int zoneId) {
        this.coord_gk4 = coord_gk4;
        this.id = id;
        this.name = name;
        this.commodityGroup = commodityGroup;
        this.zoneId = zoneId;
    }

    @Override
    public Coord getCoordinates() {
        return Objects.requireNonNull(coord_gk4);
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
        return true;
    }

    public int getZoneId(){
        return zoneId;
    }
}

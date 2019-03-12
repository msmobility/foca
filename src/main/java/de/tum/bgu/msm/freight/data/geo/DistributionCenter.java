package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import org.matsim.api.core.v01.Coord;

import java.util.Objects;

/**
 * A distribution center is a location where goods are transferred between one truck to other trucks
 */
public class DistributionCenter implements Zone{

    private int id;
    private String name;
    private Coord coord;
    private CommodityGroup commodityGroup;

    public DistributionCenter(int id, String name, Coord coord, CommodityGroup commodityGroup) {
        this.coord = coord;
        this.id = id;
        this.name = name;
        this.commodityGroup = commodityGroup;
    }

    @Override
    public Coord getCoordinates() {
        return Objects.requireNonNull(coord);
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
}

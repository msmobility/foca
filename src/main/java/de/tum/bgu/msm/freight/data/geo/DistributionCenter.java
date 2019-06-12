package de.tum.bgu.msm.freight.data.geo;

import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A distribution center is a location where goods are transferred between one truck to other trucks
 */
public class DistributionCenter {

    private final int id;
    private final String name;
    private final Coordinate coord_gk4;
    private final CommodityGroup commodityGroup;
    private final int zoneId;
    private final List<InternalMicroZone> zonesServedByThis;
    private final List<MicroDepot> microDeportsServedByThis;
    private final List<ParcelShop> parcelShopsServedByThis;

    public DistributionCenter(int id, String name, Coordinate coord_gk4, CommodityGroup commodityGroup, int zoneId) {
        this.coord_gk4 = coord_gk4;
        this.id = id;
        this.name = name;
        this.commodityGroup = commodityGroup;
        this.zoneId = zoneId;
        this.zonesServedByThis = new ArrayList<>();
        microDeportsServedByThis = new ArrayList<>();
        parcelShopsServedByThis = new ArrayList<>();
    }

    public Coordinate getCoordinates() {
        return Objects.requireNonNull(coord_gk4);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getZoneId(){
        return zoneId;
    }

    public List<InternalMicroZone> getZonesServedByThis() {
        return zonesServedByThis;
    }

    public List<MicroDepot> getMicroDeportsServedByThis() {
        return microDeportsServedByThis;
    }

    public List<ParcelShop> getParcelShopsServedByThis() {
        return parcelShopsServedByThis;
    }

    public CommodityGroup getCommodityGroup() {
        return commodityGroup;
    }
}

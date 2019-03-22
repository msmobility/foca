package de.tum.bgu.msm.freight.data.freight;

import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import org.matsim.api.core.v01.Coord;

public class ShortDistanceTruckTrip {

    private final int id;
    private final DistributionCenter distributionCenter;
    private final Commodity commodity;
    private Coord origCoord;
    private Coord destCoord;
    private final boolean toCustomer;
    private final double weight_tn;

    public ShortDistanceTruckTrip(int id, Coord origCoord, Coord destCoord, Commodity commodity, DistributionCenter distributionCenter, boolean toCustomer, double weight_tn) {
        this.id = id;
        this.origCoord = origCoord;
        this.destCoord = destCoord;
        this.commodity = commodity;
        this.distributionCenter = distributionCenter;
        this.toCustomer = toCustomer;
        this.weight_tn = weight_tn;
    }

    public static String getHeader() {
        StringBuilder builder = new StringBuilder();

        builder.append("id").append(",").
                append("toCustomer").append(",").
                append("weight_tn").append(",").
                append("commodity()").append(",").
                append("originX").append(",").
                append("originY").append(",").
                append("destX").append(",").
                append("destY").append(",").
                append("distributionCenter");

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(id).append(",").
                append(toCustomer).append(",").
                append(weight_tn).append(",").
                append(commodity).append(",").
                append(origCoord.getX()).append(",").
                append(origCoord.getY()).append(",").
                append(destCoord.getX()).append(",").
                append(destCoord.getY()).append(",").
                append(distributionCenter.getId());

        return builder.toString();


    }
    public Coord getOrigCoord() {
        return origCoord;
    }

    public Coord getDestCoord() {
        return destCoord;
    }

    public DistributionCenter getDistributionCenter() {
        return distributionCenter;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public int getId() {
        return id;
    }
}

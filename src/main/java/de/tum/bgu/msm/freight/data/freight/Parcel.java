package de.tum.bgu.msm.freight.data.freight;

import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.Zone;
import org.matsim.api.core.v01.Coord;

public class Parcel {

    private final int id;
    private Transaction transaction;
    private final boolean toDestination;
    private final double volume_m3;
    private final double weight_kg;
    private final FlowSegment flowSegment;

    private DistributionCenter distributionCenter;
    private Coord originCoord;
    private Coord destCoord;

    public Parcel(int id, boolean toDestination, double volume_m3, double weight_kg,
                  DistributionCenter distributionCenter, FlowSegment flowSegment) {
        this.id = id;
        this.toDestination = toDestination;
        this.volume_m3 = volume_m3;
        this.weight_kg = weight_kg;
        this.distributionCenter = distributionCenter;
        this.flowSegment = flowSegment;
    }

    public static String getHeader() {

        StringBuilder builder = new StringBuilder();

        builder.append("id").append(",").
                append("toDestination").append(",").
                append("weight_kg").append(",").
                append("commodity").append(",").
                append("originX()").append(",").
                append("originY()").append(",").
                append("destX()").append(",").
                append("destY").append(",").
                append("distributionCenter");

        return builder.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(id).append(",").
                append(toDestination).append(",").
                append(weight_kg).append(",").
                append(flowSegment.getCommodity().toString()).append(",");

        try {
            builder.append(originCoord.getX()).append(",").
                    append(originCoord.getY()).append(",").
                    append(destCoord.getX()).append(",").
                    append(destCoord.getY()).append(",");
        } catch (NullPointerException e) {
            builder.append("null").append(",").
                    append("null").append(",").
                    append("null").append(",").
                    append("null").append(",");
        }
        builder.append(distributionCenter.getId());

        return builder.toString();

    }

    public int getId() {
        return id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public boolean isToDestination() {
        return toDestination;
    }

    public double getVolume_m3() {
        return volume_m3;
    }

    public double getWeight_kg() {
        return weight_kg;
    }

    public DistributionCenter getDistributionCenter() {
        return distributionCenter;
    }

    public Coord getOriginCoord() {
        return originCoord;
    }

    public void setOriginCoord(Coord originCoord) {
        this.originCoord = originCoord;
    }

    public Coord getDestCoord() {
        return destCoord;
    }

    public void setDestCoord(Coord destCoord) {
        this.destCoord = destCoord;
    }

    public FlowSegment getFlowSegment() {
        return flowSegment;
    }
}

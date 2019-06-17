package de.tum.bgu.msm.freight.data.freight.longDistance;

import de.tum.bgu.msm.freight.data.freight.TruckTrip;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;

public class LDTruckTrip implements TruckTrip {
    private final Id<TruckTrip> id;
    private Coordinate origCoord;
    private Coordinate destCoord;
    private FlowSegment flowSegment;
    private double load_tn;

    private DistributionCenter originDistributionCenter = null;
    private DistributionCenter destinationDistributionCenter = null;

    public LDTruckTrip(Id<TruckTrip> id, FlowSegment FlowSegment, double load_tn) {
        this.id = id;
        this.flowSegment = FlowSegment;
        this.load_tn = load_tn;
    }


    public static String getHeader() {
        StringBuilder builder = new StringBuilder();

        builder.append("id").append(",").
                append("toDestination").append(",").
                append("weight_tn").append(",").
                append("commodity").append(",").
                append("commodityGroup").append(",").
                append("distributionType").append(",").
                append("originX").append(",").
                append("originY").append(",").
                append("destX").append(",").
                append("destY").append(",").
                append("originDistributionCenter").append(",").
                append("destinationDistributionCenter").append(",").
                append("segment").append(",").
                append("segmentOrigin").append(",").
                append("segmentDestination");

        return builder.toString();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(id).append(",").
                append("null").append(",").
                append(load_tn).append(",").
                append(flowSegment.getCommodity()).append(",").
                append(flowSegment.getCommodity().getCommodityGroup()).append(",").
                append(flowSegment.getCommodity().getCommodityGroup().getLongDistanceGoodDistribution()).append(",").
                append(origCoord.getX()).append(",").
                append(origCoord.getY()).append(",").
                append(destCoord.getX()).append(",").
                append(destCoord.getY()).append(",");

        if (originDistributionCenter != null) {
            builder.append(originDistributionCenter.getId()).append(",");
        } else {
            builder.append("null").append(",");
        }

        if (destinationDistributionCenter != null) {
            builder.append(destinationDistributionCenter.getId()).append(",");
        } else {
            builder.append("null").append(",");
        }

        builder.append(flowSegment.getSegmentType()).append(",").
                append(flowSegment.getSegmentOrigin()).append(",").
                append(flowSegment.getSegmentDestination());

        return builder.toString();


    }

    @Override
    public Coordinate getOrigCoord() {
        return origCoord;
    }

    @Override
    public Coordinate getDestCoord() {
        return destCoord;
    }

    public void setOrigCoord(Coordinate origCoord) {
        this.origCoord = origCoord;
    }

    public void setDestCoord(Coordinate destCoord) {
        this.destCoord = destCoord;
    }

    public FlowSegment getFlowSegment() {
        return flowSegment;
    }

    public double getLoad_tn() {
        return load_tn;
    }

    public void setOriginDistributionCenter(DistributionCenter originDistributionCenter) {
        this.originDistributionCenter = originDistributionCenter;
    }

    public void setDestinationDistributionCenter(DistributionCenter destinationDistributionCenter) {
        this.destinationDistributionCenter = destinationDistributionCenter;
    }

    @Override
    public Id<TruckTrip> getId(){
        return id;
    }
}

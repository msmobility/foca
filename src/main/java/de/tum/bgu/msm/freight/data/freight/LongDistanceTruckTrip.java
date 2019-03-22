package de.tum.bgu.msm.freight.data.freight;

import org.matsim.api.core.v01.Coord;

public class LongDistanceTruckTrip implements TruckTrip {
    private final int id;
    private Coord origCoord;
    private Coord destCoord;
    private FlowSegment flowSegment;
    private double load_tn;

    public LongDistanceTruckTrip(int id, FlowSegment FlowSegment, double load_tn) {
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
                append("distributionCenter");

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
                append(flowSegment.getCommodity().getCommodityGroup().getGoodDistribution()).append(",").
                append(origCoord.getX()).append(",").
                append(origCoord.getY()).append(",").
                append(destCoord.getX()).append(",").
                append(destCoord.getY()).append(",").
                append("null");

        return builder.toString();


    }

    @Override
    public Coord getOrigCoord() {
        return origCoord;
    }

    @Override
    public Coord getDestCoord() {
        return destCoord;
    }

    public void setOrigCoord(Coord origCoord) {
        this.origCoord = origCoord;
    }

    public void setDestCoord(Coord destCoord) {
        this.destCoord = destCoord;
    }

    public de.tum.bgu.msm.freight.data.freight.FlowSegment getFlowSegment() {
        return flowSegment;
    }

    public double getLoad_tn() {
        return load_tn;
    }
}

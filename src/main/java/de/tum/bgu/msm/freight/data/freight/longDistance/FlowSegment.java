package de.tum.bgu.msm.freight.data.freight.longDistance;

import de.tum.bgu.msm.freight.data.freight.Commodity;

import java.util.ArrayList;
import java.util.List;

/**
 * A flow segment is a segment of a flowOriginToDestination
 */
public class FlowSegment {

    private final int segmentOrigin;
    private final int segmentDestination;
    private final int flowOrigin;
    private final int flowDestination;
    private int originTerminal = -1;
    private int destinationTerminal = -1;
    private LDMode LDMode;
    private Commodity commodity;
    private double volume_tn;
    private SegmentType segmentType;
    private FlowType flowType;
    private final List<LDTruckTrip> LDTruckTrips;

    //these attributes are optional only for the trips that are converted to trucks
    private double distance_km;
    private double tt_s;


    public FlowSegment(int segmentOrigin, int segmentDestination, LDMode LDMode, Commodity commodity, double volume_tn, SegmentType segmentType, FlowType flowType, int flowOrigin, int flowDestination) {
        this.segmentOrigin = segmentOrigin;
        this.segmentDestination = segmentDestination;
        this.LDMode = LDMode;
        this.commodity = commodity;
        this.volume_tn = volume_tn;
        this.segmentType = segmentType;
        this.flowType = flowType;
        this.LDTruckTrips = new ArrayList<>();
        this.flowOrigin = flowOrigin;
        this.flowDestination = flowDestination;
    }

    public static String getHeader() {
        StringBuilder builder = new StringBuilder();

        builder.append("origin").append(",");
        builder.append("destination").append(",");
        builder.append("flowOrigin").append(",");
        builder.append("flowDestination").append(",");
        builder.append("commodity").append(",");
        builder.append("commodityGroup").append(",");
        builder.append("goodDistribution").append(",");
        builder.append("volume_tn").append(",");
        builder.append("segmentType").append(",");
        builder.append("mode").append(",");
        builder.append("ld_trucks");


        return builder.toString();
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(segmentOrigin).append(",");
        builder.append(segmentDestination).append(",");
        builder.append(flowOrigin).append(",");
        builder.append(flowDestination).append(",");
        builder.append(commodity).append(",");
        builder.append(commodity.getCommodityGroup()).append(",");
        builder.append(commodity.getCommodityGroup().getLongDistanceGoodDistribution()).append(",");
        builder.append(volume_tn).append(",");
        builder.append(segmentType).append(",");
        builder.append(LDMode).append(",");
        builder.append(LDTruckTrips.size());


        return builder.toString();

    }

    public int getSegmentOrigin() {
        return segmentOrigin;
    }

    public int getSegmentDestination() {
        return segmentDestination;
    }

    public LDMode getLDMode() {
        return LDMode;
    }

    public Commodity getCommodity() {
        return commodity;
    }

    public double getVolume_tn() {
        return volume_tn;
    }

    public SegmentType getSegmentType() {
        return segmentType;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public double getDistance_km() {
        return distance_km;
    }

    public void setDistance_km(double distance_km) {
        this.distance_km = distance_km;
    }

    public double getTt_s() {
        return tt_s;
    }

    public void setTt_s(double tt_s) {
        this.tt_s = tt_s;
    }

    public List<LDTruckTrip> getTruckTrips() {
        return LDTruckTrips;
    }

    public int getOriginTerminal() {
        return originTerminal;
    }

    public void setOriginTerminal(int originTerminal) {
        this.originTerminal = originTerminal;
    }

    public int getDestinationTerminal() {
        return destinationTerminal;
    }

    public void setDestinationTerminal(int destinationTerminal) {
        this.destinationTerminal = destinationTerminal;
    }

    public int getFlowOrigin() {
        return flowOrigin;
    }

    public int getFlowDestination() {
        return flowDestination;
    }
}

package de.tum.bgu.msm.freight.io.input;

import com.sun.org.apache.xpath.internal.operations.Bool;
import de.tum.bgu.msm.data.accessibility.CommutingTimeProbability;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Bound;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.TruckTrip;
import de.tum.bgu.msm.freight.data.freight.longDistance.*;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.modules.common.DistributionCenterUtils;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Id;


public class LongDistanceTruckReader extends CSVReader {

    private Properties properties;
    private int idIndex;
    private int toDestinationIndex;
    private int weight_tnIndex;
    private int commodityIndex;
    private int origXIndex;
    private int origYIndex;
    private int destXIndex;
    private int destYIndex;
    private int origDCIndex;
    private int destDCIndex;
    private int segmentIndex;
    private int origZoneIndex;
    private int destZoneIndex;

    public LongDistanceTruckReader(DataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }


    @Override
    protected void processHeader(String[] header) {
        idIndex = MitoUtil.findPositionInArray("id", header);
        //toDestinationIndex = MitoUtil.findPositionInArray("toDestination", header);
        weight_tnIndex = MitoUtil.findPositionInArray("weight_tn", header);
        commodityIndex = MitoUtil.findPositionInArray("commodity", header);
        origXIndex = MitoUtil.findPositionInArray("originX", header);
        origYIndex = MitoUtil.findPositionInArray("originY", header);
        destXIndex = MitoUtil.findPositionInArray("destX", header);
        destYIndex = MitoUtil.findPositionInArray("destY", header);
        origDCIndex = MitoUtil.findPositionInArray("originDistributionCenter", header);
        destDCIndex = MitoUtil.findPositionInArray("destinationDistributionCenter", header);
        segmentIndex = MitoUtil.findPositionInArray("segment", header);
        origZoneIndex = MitoUtil.findPositionInArray("segmentOrigin", header);
        destZoneIndex = MitoUtil.findPositionInArray("segmentDestination", header);

    }

    @Override
    protected void processRecord(String[] record) {

        Id<TruckTrip> id = Id.create(record[idIndex], TruckTrip.class);
        Commodity commodity = Commodity.valueOf(record[commodityIndex]);
        //boolean toDestination = Boolean.parseBoolean(record[toDestinationIndex]);
        Coordinate origCoord = new Coordinate(Double.parseDouble(record[origXIndex]), Double.parseDouble(record[origYIndex]));
        Coordinate destCoord = new Coordinate(Double.parseDouble(record[destXIndex]), Double.parseDouble(record[destYIndex]));
        double load_tn = Double.parseDouble(record[weight_tnIndex]);
        SegmentType segmentType = SegmentType.valueOf(record[segmentIndex]);
        int origZone = Integer.parseInt(record[origZoneIndex]);
        int destZone = Integer.parseInt(record[destZoneIndex]);
        FlowSegment segment = new FlowSegment(origZone, destZone, LDMode.ROAD, commodity, -1, segmentType, null, origZone, destZone);


        String origDcName = record[origDCIndex];
        String destDcName = record[destDCIndex];

        int[] analysisZones = properties.getAnalysisZones();

        for (int zoneCandidate : analysisZones) {
            Bound bound;
            if (zoneCandidate == origZone) {
                if (zoneCandidate == destZone) {
                    bound = Bound.INTRAZONAL;
                } else {
                    bound = Bound.OUTBOUND;
                }
            } else if (zoneCandidate == destZone) {
                bound = Bound.INBOUND;
            } else {
                bound = Bound.EXTRAZONAL;
            }

            LDTruckTrip ldTruckTrip = new LDTruckTrip(id, segment, load_tn);
            ldTruckTrip.setOrigCoord(origCoord);
            ldTruckTrip.setDestCoord(destCoord);
            if (zoneCandidate == origZone) {
                //the trip starts at the analysis zone
                if (!origDcName.equals("null")) {
                    int origDcId = Integer.parseInt(origDcName);
                    DistributionCenter distributionCenter = dataSet.getDistributionCenters().get(origZone).get(commodity.getCommodityGroup()).get(origDcId);
                    ldTruckTrip.setDestinationDistributionCenter(distributionCenter);
                    if (commodity.getCommodityGroup().getLongDistanceGoodDistribution().equals(LDDistributionType.PARCEL_DELIVERY)) {
                        DistributionCenterUtils.addVolumeForParcelDelivery(distributionCenter, commodity, bound, load_tn, dataSet);
                    } else {
                        DistributionCenterUtils.addVolumeForSmallTruckDelivery(distributionCenter, commodity, bound, load_tn, dataSet);
                    }
                } else {
                    ldTruckTrip.setDestinationDistributionCenter(null);
                }
            }
            if (zoneCandidate == destZone) {
                //the trip ends at the analysis zone
                if (!destDcName.equals("null")) {
                    int destDcId = Integer.parseInt(destDcName);
                    DistributionCenter distributionCenter = dataSet.getDistributionCenters().get(destZone).get(commodity.getCommodityGroup()).get(destDcId);
                    ldTruckTrip.setOriginDistributionCenter(distributionCenter);
                    if (commodity.getCommodityGroup().getLongDistanceGoodDistribution().equals(LDDistributionType.PARCEL_DELIVERY)) {
                        DistributionCenterUtils.addVolumeForParcelDelivery(distributionCenter, commodity, bound, load_tn, dataSet);
                    } else {
                        DistributionCenterUtils.addVolumeForSmallTruckDelivery(distributionCenter, commodity, bound, load_tn, dataSet);
                    }
                } else {
                    ldTruckTrip.setOriginDistributionCenter(null);
                }
            }
            if (!bound.equals(Bound.EXTRAZONAL)) {
                dataSet.getLDTruckTrips().add(ldTruckTrip);
            }
        }
    }


    @Override
    public void read() {
        super.read("./input/preProcessedInput/ld_trucks_muc.csv", ",");
    }


}

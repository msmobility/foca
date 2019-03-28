package de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.geo.Bound;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

import java.util.*;
import java.util.stream.Collectors;

public class OriginDestinationAllocation implements Module {

    private Properties properties;
    private DataSet dataSet;
    private double cumulatedV = 0; //debug-only variable

    private static final Logger logger = Logger.getLogger(OriginDestinationAllocation.class);


    @Override
    public void setup(DataSet dataset, Properties properties) {
        this.dataSet = dataset;
        this.properties = properties;

    }

    @Override
    public void run() {

        subsampleTrucksAndAssignCoordinates();
        logger.warn(cumulatedV);
    }


    private void subsampleTrucksAndAssignCoordinates() {

        for (FlowSegment flowSegment : dataSet.getAssignedFlowSegments()) {
            for (LongDistanceTruckTrip longDistanceTruckTrip : flowSegment.getTruckTrips()) {
                //todo this is not correct for extra-zonal trips!!
                //if none of the zones are in study area --> random zone
                //if only one of the zones is in study area --> detailed assignment in such zone like inbound/outbound
                //if both zones are in the same study area zone --> intrazonal
                //if both are in study area but different zones --> interzonal --> do twice!
                setOrigin(longDistanceTruckTrip);
                setDestination(longDistanceTruckTrip);

                //

                dataSet.getLongDistanceTruckTrips().add(longDistanceTruckTrip);

            }
        }

    }

    private boolean setOrigin(LongDistanceTruckTrip longDistanceTruckTrip) {

        FlowSegment flowSegment = longDistanceTruckTrip.getFlowSegment();

        Zone originZone = dataSet.getZones().get(flowSegment.getSegmentOrigin());
        Zone destinationZone = dataSet.getZones().get(flowSegment.getSegmentDestination());

        Coord origCoord;
        Commodity commodity = flowSegment.getCommodity();

        Bound bound;

        double thisTruckEffectiveLoad = longDistanceTruckTrip.getLoad_tn();

        if (dataSet.getZones().get(flowSegment.getFlowOrigin()).isInStudyArea()) {
            if (originZone.equals(destinationZone)) {
                bound = Bound.INTRAZONAL;
            } else {
                bound = Bound.OUTBOUND;
            }
        } else {
            bound = Bound.EXTRAZONAL;
        }


        if (flowSegment.getSegmentType().equals(SegmentType.POST)) {
            origCoord = dataSet.getTerminals().get(flowSegment.getOriginTerminal()).getCoordinates();
        } else {
            switch (commodity.getCommodityGroup().getGoodDistribution()) {
                case DOOR_TO_DOOR:
                    if (!originZone.isInStudyArea()) {
                        origCoord = originZone.getCoordinates();
                    } else {
                        InternalZone internalZone = (InternalZone) originZone;
                        int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone, dataSet.getMakeTable());
                        origCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
                    }
                    break;
                case SINGLE_VEHICLE:
                    if (!originZone.isInStudyArea()) {
                        origCoord = originZone.getCoordinates();
                    } else {
                        DistributionCenter originDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentOrigin(), commodity.getCommodityGroup());
                        longDistanceTruckTrip.setOriginDistributionCenter(originDistributionCenter);
                        origCoord = originDistributionCenter.getCoordinates();
                        addVolumeForSmallTruckDelivery(originDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                case PARCEL_DELIVERY:
                    if (!originZone.isInStudyArea()) {
                        //if zone does not have microzone
                        origCoord = originZone.getCoordinates();
                    } else {
                        DistributionCenter originDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentOrigin(), commodity.getCommodityGroup());
                        origCoord = originDistributionCenter.getCoordinates();
                        longDistanceTruckTrip.setOriginDistributionCenter(originDistributionCenter);
                        addVolumeForParcelDelivery(originDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                default:
                    throw new RuntimeException("Unaccepted good distribution type");
            }
        }


        if (origCoord != null) {
            longDistanceTruckTrip.setOrigCoord(origCoord);
            return true;
        } else {
            logger.warn("Cannot assign origin coordinates to flow with id: " + flowSegment.toString());
            return false;
        }
    }

    private boolean setDestination(LongDistanceTruckTrip longDistanceTruckTrip) {

        FlowSegment flowSegment = longDistanceTruckTrip.getFlowSegment();

        Zone originZone = dataSet.getZones().get(flowSegment.getSegmentOrigin());
        Zone destinationZone = dataSet.getZones().get(flowSegment.getSegmentDestination());

        Coord destCoord;
        Commodity commodity = flowSegment.getCommodity();

        Bound bound;

        double thisTruckEffectiveLoad = longDistanceTruckTrip.getLoad_tn();

        if (dataSet.getZones().get(flowSegment.getFlowDestination()).isInStudyArea()) {
            if (originZone.equals(destinationZone)) {
                bound = Bound.INTRAZONAL;
            } else {
                bound = Bound.INBOUND;
            }
        } else {
            bound = Bound.EXTRAZONAL;
        }



        if (flowSegment.getSegmentType().equals(SegmentType.PRE)) {
            destCoord = dataSet.getTerminals().get(flowSegment.getDestinationTerminal()).getCoordinates();
        } else {
            switch (commodity.getCommodityGroup().getGoodDistribution()) {
                case DOOR_TO_DOOR:
                    if (!destinationZone.isInStudyArea()) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        InternalZone internalZone = (InternalZone) destinationZone;
                        int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone, dataSet.getUseTable());
                        destCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
                    }
                    break;
                case SINGLE_VEHICLE:
                    if (!destinationZone.isInStudyArea()) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        DistributionCenter destinationDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentDestination(), commodity.getCommodityGroup());
                        destCoord = destinationDistributionCenter.getCoordinates();
                        longDistanceTruckTrip.setDestinationDistributionCenter(destinationDistributionCenter);
                        addVolumeForSmallTruckDelivery(destinationDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                case PARCEL_DELIVERY:
                    if (!destinationZone.isInStudyArea()) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        DistributionCenter destinationDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentDestination(), commodity.getCommodityGroup());
                        destCoord = destinationDistributionCenter.getCoordinates();
                        longDistanceTruckTrip.setDestinationDistributionCenter(destinationDistributionCenter);
                        addVolumeForParcelDelivery(destinationDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                default:
                    throw new RuntimeException("Unaccepted good distribution type");

            }
        }

        if (destCoord != null) {
            longDistanceTruckTrip.setDestCoord(destCoord);
            return true;
        } else {
            logger.warn("Cannot assign destination coordinates to flow with id: " + flowSegment.toString());
            return false;
        }
    }

    private DistributionCenter chooseDistributionCenter(int zoneId, CommodityGroup commodityGroup) {
        ArrayList<DistributionCenter> distributionCenters = dataSet.getDistributionCenterForZoneAndCommodityGroup(zoneId, commodityGroup);
        Collections.shuffle(distributionCenters, properties.getRand());
        return distributionCenters.get(0);
    }

    private void addVolumeForSmallTruckDelivery(DistributionCenter distributionCenter, Commodity commodity, Bound bound, double load_tn) {
        cumulatedV += load_tn;
        if (dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().contains(distributionCenter, commodity)) {
            if (dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().get(distributionCenter, commodity).containsKey(bound)) {
                double current_load = dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().get(distributionCenter, commodity).get(bound);
                dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().get(distributionCenter, commodity).put(bound, load_tn + current_load);
            } else {
                dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().get(distributionCenter, commodity).put(bound, load_tn);
            }

        } else {
            Map<Bound, Double> map = new HashMap<>();
            map.put(bound, load_tn);
            dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().put(distributionCenter, commodity, map);
        }

    }

    private void addVolumeForParcelDelivery(DistributionCenter distributionCenter, Commodity commodity, Bound bound, double load_tn) {
        if (dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().contains(distributionCenter, commodity)) {
            if (dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().get(distributionCenter, commodity).containsKey(bound)) {
                double current_load = dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().get(distributionCenter, commodity).get(bound);
                dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().get(distributionCenter, commodity).put(bound, load_tn + current_load);
            } else {
                dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().get(distributionCenter, commodity).put(bound, load_tn);
            }
        } else {
            Map<Bound, Double> map = new HashMap<>();
            map.put(bound, load_tn);
            dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().put(distributionCenter, commodity, map);
        }

    }


}

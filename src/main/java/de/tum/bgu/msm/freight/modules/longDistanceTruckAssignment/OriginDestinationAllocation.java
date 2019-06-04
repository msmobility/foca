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
import java.util.concurrent.atomic.AtomicInteger;

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

        AtomicInteger counter = new AtomicInteger(0);
        for (FlowSegment flowSegment : dataSet.getAssignedFlowSegments()) {
            for (LongDistanceTruckTrip longDistanceTruckTrip : flowSegment.getTruckTrips()) {
                setOrigin(longDistanceTruckTrip);
                setDestination(longDistanceTruckTrip);
                dataSet.getLongDistanceTruckTrips().add(longDistanceTruckTrip);

                if (counter.incrementAndGet() % 100000 == 0){
                    logger.info("Assigned o/d to " + counter.get() + " LD trucks");
                }
            }
        }
        logger.info("Assigned o/d to " + counter.get() + " LD trucks");

    }

    private boolean setOrigin(LongDistanceTruckTrip longDistanceTruckTrip) {

        FlowSegment flowSegment = longDistanceTruckTrip.getFlowSegment();

        Zone originZone = dataSet.getZones().get(flowSegment.getSegmentOrigin());
        Zone destinationZone = dataSet.getZones().get(flowSegment.getSegmentDestination());

        Coord origCoord;
        Commodity commodity = flowSegment.getCommodity();

        Bound bound;

        double thisTruckEffectiveLoad = longDistanceTruckTrip.getLoad_tn();

        if (originZone.isInStudyArea()) {
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
                        int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone.getMicroZones().values(), dataSet.getMakeTable());
                        origCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
                    }
                    break;
                case SINGLE_VEHICLE:
                    if (!originZone.isInStudyArea()) {
                        origCoord = originZone.getCoordinates();
                    } else {
                        DistributionCenter originDistributionCenter = chooseDistributionCenter(originZone.getId(), commodity.getCommodityGroup());
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
                        DistributionCenter originDistributionCenter = chooseDistributionCenter(originZone.getId(), commodity.getCommodityGroup());
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

        if (destinationZone.isInStudyArea()) {
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
                        int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone.getMicroZones().values(), dataSet.getUseTable());
                        destCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
                    }
                    break;
                case SINGLE_VEHICLE:
                    if (!destinationZone.isInStudyArea()) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        DistributionCenter destinationDistributionCenter = chooseDistributionCenter(destinationZone.getId(), commodity.getCommodityGroup());
                        destCoord = destinationDistributionCenter.getCoordinates();
                        longDistanceTruckTrip.setDestinationDistributionCenter(destinationDistributionCenter);
                        addVolumeForSmallTruckDelivery(destinationDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                case PARCEL_DELIVERY:
                    if (!destinationZone.isInStudyArea()) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        DistributionCenter destinationDistributionCenter = chooseDistributionCenter(destinationZone.getId(), commodity.getCommodityGroup());
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
        //todo not random distribution center
        ArrayList<DistributionCenter> distributionCenters = new ArrayList<>();
        distributionCenters.addAll(dataSet.getDistributionCentersForZoneAndCommodityGroup(zoneId, commodityGroup).values());
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

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

        List<Zone> zonesInStudyArea = dataSet.getZones().values().stream().filter(z -> z.isInStudyArea()).collect(Collectors.toList());

        for (FlowSegment flowSegment : dataSet.getAssignedFlowSegments()) {
            for (LongDistanceTruckTrip longDistanceTruckTrip : flowSegment.getTruckTrips()) {
                if (properties.getRand().nextDouble() < properties.getScaleFactor()) {
                    for (Zone zone : zonesInStudyArea){
                        setOriginAndDestination(longDistanceTruckTrip, zone);
                    }

                }
            }
        }

    }

    private boolean setOriginAndDestination(LongDistanceTruckTrip longDistanceTruckTrip, Zone zone) {

        FlowSegment flowSegment = longDistanceTruckTrip.getFlowSegment();

        Zone originZone = dataSet.getZones().get(flowSegment.getSegmentOrigin());
        Zone destinationZone = dataSet.getZones().get(flowSegment.getSegmentDestination());

        Coord origCoord;
        Coord destCoord;
        Commodity commodity = flowSegment.getCommodity();

        Bound bound;

        double thisTruckEffectiveLoad = longDistanceTruckTrip.getLoad_tn();

        if (dataSet.getZones().get(flowSegment.getFlowOrigin()).equals(zone)) {
            if (dataSet.getZones().get(flowSegment.getFlowDestination()).equals(zone)) {
                bound = Bound.INTRAZONAL;
            } else {
                bound = Bound.OUTBOUND;
            }
        } else if (dataSet.getZones().get(flowSegment.getFlowDestination()).equals(zone)) {
            bound = Bound.INBOUND;
        } else {
            return false;
        }


        if (flowSegment.getSegmentType().equals(SegmentType.POST)) {
            origCoord = dataSet.getTerminals().get(flowSegment.getOriginTerminal()).getCoordinates();
        } else {
            switch (commodity.getCommodityGroup().getGoodDistribution()) {
                case DOOR_TO_DOOR:
                    if (!originZone.equals(zone)) {
                        origCoord = originZone.getCoordinates();
                    } else {
                        InternalZone internalZone = (InternalZone) originZone;
                        int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone, dataSet.getMakeTable());
                        origCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
                    }
                    break;
                case SINGLE_VEHICLE:
                    if (!originZone.equals(zone)) {
                        origCoord = originZone.getCoordinates();
                    } else {
                        DistributionCenter originDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentOrigin(), commodity.getCommodityGroup());
                        longDistanceTruckTrip.setOriginDistributionCenter(originDistributionCenter);
                        origCoord = originDistributionCenter.getCoordinates();
                        addVolumeForSmallTruckDelivery(originDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                case PARCEL_DELIVERY:
                    if (!originZone.equals(zone)) {
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


        if (flowSegment.getSegmentType().equals(SegmentType.PRE)) {
            destCoord = dataSet.getTerminals().get(flowSegment.getDestinationTerminal()).getCoordinates();
        } else {
            switch (commodity.getCommodityGroup().getGoodDistribution()) {
                case DOOR_TO_DOOR:
                    if (!destinationZone.equals(zone)) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        InternalZone internalZone = (InternalZone) destinationZone;
                        int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone, dataSet.getUseTable());
                        destCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
                    }
                    break;
                case SINGLE_VEHICLE:
                    if (!destinationZone.equals(zone)) {
                        destCoord = destinationZone.getCoordinates();
                    } else {
                        DistributionCenter destinationDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentDestination(), commodity.getCommodityGroup());
                        destCoord = destinationDistributionCenter.getCoordinates();
                        longDistanceTruckTrip.setDestinationDistributionCenter(destinationDistributionCenter);
                        addVolumeForSmallTruckDelivery(destinationDistributionCenter, commodity, bound, thisTruckEffectiveLoad);
                    }
                    break;
                case PARCEL_DELIVERY:
                    if (!destinationZone.equals(zone)) {
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

        if (origCoord != null && destCoord != null) {
            longDistanceTruckTrip.setOrigCoord(origCoord);
            longDistanceTruckTrip.setDestCoord(destCoord);
            dataSet.getLongDistanceTruckTrips().add(longDistanceTruckTrip);
            return true;
        } else {
            logger.warn("Cannot assign coordinates to flow with id: " + flowSegment.toString());
            return false;
        }
    }

    private DistributionCenter chooseDistributionCenter(int zoneId, CommodityGroup commodityGroup) {
        ArrayList<DistributionCenter> distributionCenters = dataSet.getDistributionCenterForZoneAndCommodityGroup(zoneId, commodityGroup);
        Collections.shuffle(distributionCenters, properties.getRand());
        return distributionCenters.get(0);
    }

    private void addVolumeForSmallTruckDelivery(DistributionCenter distributionCenter, Commodity commodity, Bound bound, double load_tn) {
        cumulatedV +=load_tn;
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

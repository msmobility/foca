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
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OriginDestinationAllocation implements Module {

    private Properties properties;
    private DataSet dataSet;


    @Override
    public void setup(DataSet dataset, Properties properties) {
        this.dataSet = dataset;
        this.properties = properties;

    }

    @Override
    public void run() {
        subsampleTrucksAndAssignCoordinates();
    }


    private void subsampleTrucksAndAssignCoordinates() {
        for (FlowSegment flowSegment : dataSet.getAssignedFlowSegments()) {
            for (LongDistanceTruckTrip longDistanceTruckTrip : flowSegment.getTruckTrips()) {
                if (properties.getRand().nextDouble() < properties.getScaleFactor()) {
                    setOriginAndDestination(longDistanceTruckTrip);
                }
            }
        }

    }

    private boolean setOriginAndDestination(LongDistanceTruckTrip longDistanceTruckTrip) {

        FlowSegment flowSegment = longDistanceTruckTrip.getFlowSegment();

        Zone originZone = dataSet.getZones().get(flowSegment.getSegmentOrigin());
        Zone destinationZone = dataSet.getZones().get(flowSegment.getSegmentDestination());

        Coord origCoord;
        Coord destCoord;
        Commodity commodity = flowSegment.getCommodity();

        Bound bound;
        if (dataSet.getZones().get(flowSegment.getFlowOrigin()).isInStudyArea()) {
            if (dataSet.getZones().get(flowSegment.getFlowDestination()).isInStudyArea()) {
                bound = Bound.INTRAZONAL;
            } else {
                bound = Bound.OUTBOUND;
            }
        } else if (dataSet.getZones().get(flowSegment.getFlowDestination()).isInStudyArea()) {
            bound = Bound.INBOUND;
        } else {
            return false;
        }

        if (flowSegment.getSegmentType().equals(SegmentType.POST)) {
            origCoord = dataSet.getTerminals().get(flowSegment.getOriginTerminal()).getCoordinates();
        } else if (!commodity.getCommodityGroup().getGoodDistribution().equals(GoodDistribution.DOOR_TO_DOOR) &&
                originZone.isInStudyArea()) {
            //pick up a distribution center
            DistributionCenter originDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentOrigin(), commodity.getCommodityGroup());
            origCoord = originDistributionCenter.getCoordinates();
            //further disaggregate the flows, including the destination microzones if needed
            if (commodity.equals(Commodity.POST_PACKET)) {
                addVolumeForParcelDelivery(originDistributionCenter, commodity, bound, longDistanceTruckTrip.getLoad_tn());
            } else {
                addVolumeForSmallTruckDelivery(originDistributionCenter, commodity, bound, longDistanceTruckTrip.getLoad_tn());
            }

        } else {
            if (!originZone.isInStudyArea()) {
                //if zone does not have microzone
                origCoord = originZone.getCoordinates();
            } else {
                //if zone does have microzones
                //all flows are here BUSINESS_CUSTOMER
                InternalZone internalZone = (InternalZone) originZone;
                int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone, dataSet.getMakeTable());
                origCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
            }
        }

        if (flowSegment.getSegmentType().equals(SegmentType.PRE)) {
            destCoord = dataSet.getTerminals().get(flowSegment.getDestinationTerminal()).getCoordinates();
        } else if (!commodity.getCommodityGroup().getGoodDistribution().equals(GoodDistribution.DOOR_TO_DOOR) &&
                destinationZone.isInStudyArea()) {
            DistributionCenter destinationDistributionCenter = chooseDistributionCenter(flowSegment.getSegmentDestination(), commodity.getCommodityGroup());
            destCoord = destinationDistributionCenter.getCoordinates();
            //further disaggregate the flows, including the destination microzones if needed
            if (commodity.equals(Commodity.POST_PACKET)) {
                addVolumeForParcelDelivery(destinationDistributionCenter, commodity, bound, longDistanceTruckTrip.getLoad_tn());
            } else {
                addVolumeForSmallTruckDelivery(destinationDistributionCenter, commodity, bound, longDistanceTruckTrip.getLoad_tn());
            }

        } else {
            if (!destinationZone.isInStudyArea()) {
                //if zone does not have microzones
                destCoord = destinationZone.getCoordinates();
            } else {
                //if zone does have microzones
                InternalZone internalZone = (InternalZone) destinationZone;
                int microZoneId = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, internalZone, dataSet.getUseTable());
                destCoord = internalZone.getMicroZones().get(microZoneId).getCoordinates();
            }
        }

        if (origCoord != null && destCoord != null) {
            longDistanceTruckTrip.setOrigCoord(origCoord);
            longDistanceTruckTrip.setDestCoord(destCoord);
            dataSet.getLongDistanceTruckTrips().add(longDistanceTruckTrip);
            return true;
        } else {
            return false;
        }
    }

    private DistributionCenter chooseDistributionCenter(int zoneId, CommodityGroup commodityGroup) {
        ArrayList<DistributionCenter> distributionCenters = dataSet.getDistributionCenterForZoneAndCommodityGroup(zoneId, commodityGroup);
        Collections.shuffle(distributionCenters, properties.getRand());
        return distributionCenters.get(0);
    }

    private void addVolumeForSmallTruckDelivery(DistributionCenter distributionCenter, Commodity commodity, Bound bound, double load_tn) {
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

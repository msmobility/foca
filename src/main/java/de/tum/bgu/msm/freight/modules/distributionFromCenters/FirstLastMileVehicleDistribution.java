package de.tum.bgu.msm.freight.modules.distributionFromCenters;

import com.sun.org.apache.bcel.internal.generic.ARRAYLENGTH;
import de.tum.bgu.msm.freight.FreightFlows;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.DistanceBin;
import de.tum.bgu.msm.freight.data.freight.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.ShortDistanceTruckTrip;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class FirstLastMileVehicleDistribution implements Module {

    private Properties properties;
    private DataSet dataSet;
    private double smallTruckRelativeSize = 0.5;
    private static final Logger logger = Logger.getLogger(FirstLastMileVehicleDistribution.class);


    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
    }

    @Override
    public void run() {
        generateCoordinates();

    }

    private void generateCoordinates() {
        AtomicInteger counter = new AtomicInteger(0);
        for (DistributionCenter distributionCenter : dataSet.getFlowSegmentsDeliveredBySmallTrucks().keySet()){
            for (Commodity commodity : Commodity.values()){
                for (FlowSegment flowSegment : dataSet.getFlowSegmentsDeliveredBySmallTrucks().get(distributionCenter)){
                    Map<Zone, Double> volumesByZoneAtOrigin = new HashMap<>();

                    Zone originZone = dataSet.getZones().get(flowSegment.getOrigin());
                    Zone destinationZone = dataSet.getZones().get(flowSegment.getDestination());
                    if (originZone.isInStudyArea() && flowSegment.getCommodity().equals(commodity)){
                        if (volumesByZoneAtOrigin.containsKey(originZone)){
                            volumesByZoneAtOrigin.put(originZone, flowSegment.getVolume_tn() + volumesByZoneAtOrigin.get(originZone));
                        } else {
                            volumesByZoneAtOrigin.put(originZone, flowSegment.getVolume_tn());
                        }
                    }
                    for (Zone zone : volumesByZoneAtOrigin.keySet()){
                        double sentVolume_tn = volumesByZoneAtOrigin.get(zone);
                        disaggregateToSmallTrucks(sentVolume_tn, commodity, distributionCenter, zone, false);
                    }

                    Map<Zone, Double> volumesByZoneAtDestination = new HashMap<>();
                    if (destinationZone.isInStudyArea() && flowSegment.getCommodity().equals(commodity)){
                        if (volumesByZoneAtDestination.containsKey(originZone)){
                            volumesByZoneAtDestination.put(destinationZone, flowSegment.getVolume_tn() + volumesByZoneAtDestination.get(destinationZone));
                        } else {
                            volumesByZoneAtDestination.put(destinationZone, flowSegment.getVolume_tn());
                        }
                    }
                    for (Zone zone : volumesByZoneAtDestination.keySet()){
                        double receivedVolume_tn = volumesByZoneAtDestination.get(zone);
                        disaggregateToSmallTrucks(receivedVolume_tn, commodity, distributionCenter, zone, true);
                    }


                }
                logger.info("Generated small truck trips for commodity " + commodity + " and distribution center " + distributionCenter.getId());

            }
        }
        logger.info("Generated " + dataSet.getShortDistanceTruckTrips().size() + " short distance trips.");


    }

    private void disaggregateToSmallTrucks(double volume,Commodity commodity, DistributionCenter distributionCenter, Zone zone, boolean toDestination) {

        double load = dataSet.getTruckLoadsByDistanceAndCommodity().get(commodity, DistanceBin.D0_50) * smallTruckRelativeSize;
        int trucks = (int) (volume/load/properties.getDaysPerYear());

        for (int t = 0; t < trucks; t++ ) {
            ShortDistanceTruckTrip sdtt;
            Coord distributionCenterCoord = distributionCenter.getCoordinates();
            if (toDestination) {
                int microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, (InternalZone) zone, dataSet.getUseTable());
                Coord customerCoord = ((InternalZone) zone).getMicroZones().get(microZone).getCoordinates();

                sdtt  = new ShortDistanceTruckTrip(distributionCenterCoord, customerCoord, commodity, distributionCenter);

            } else {
                int microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, (InternalZone) zone, dataSet.getUseTable());
                Coord customerCoord = ((InternalZone) zone).getMicroZones().get(microZone).getCoordinates();

                sdtt  = new ShortDistanceTruckTrip(customerCoord, distributionCenterCoord, commodity, distributionCenter);
            }

            dataSet.getShortDistanceTruckTrips().add(sdtt);
        }
    }
}

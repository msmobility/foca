package de.tum.bgu.msm.freight.modules.distributionFromCenters;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.DistanceBin;
import de.tum.bgu.msm.freight.data.freight.ShortDistanceTruckTrip;
import de.tum.bgu.msm.freight.data.geo.Bound;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class FirstLastMileVehicleDistribution implements Module {

    private Properties properties;
    private DataSet dataSet;
    private static final Logger logger = Logger.getLogger(FirstLastMileVehicleDistribution.class);
    private AtomicInteger counter = new AtomicInteger(0);


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

        double totalVolume_tn = 0;

        for (DistributionCenter distributionCenter : dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().rowKeySet()) {
            for (Commodity commodity : dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().columnKeySet()) {

                Map<Bound, Double> volumeProcessedByDistributionCenter = dataSet.getVolByCommodityDistributionCenterAndBoundBySmallTrucks().get(distributionCenter, commodity);

                if (volumeProcessedByDistributionCenter != null){
                    Zone zoneServedByDistributionCenter = dataSet.getZones().get(distributionCenter.getZoneId());

                    double sentVolume_tn = volumeProcessedByDistributionCenter.getOrDefault(Bound.OUTBOUND, 0.) +
                            volumeProcessedByDistributionCenter.getOrDefault(Bound.INTRAZONAL,0.) * 0.5;

                    disaggregateToSmallTrucks(sentVolume_tn, commodity, distributionCenter, zoneServedByDistributionCenter, false);

                    double receivedVolume_tn = volumeProcessedByDistributionCenter.getOrDefault(Bound.INBOUND, 0.) +
                            volumeProcessedByDistributionCenter.getOrDefault(Bound.INTRAZONAL, 0.) * 0.5;

                    disaggregateToSmallTrucks(receivedVolume_tn, commodity, distributionCenter, zoneServedByDistributionCenter, true);

                    totalVolume_tn += sentVolume_tn;
                    totalVolume_tn += receivedVolume_tn;

                    logger.info("Generated small truck trips for commodity " + commodity + " and distribution center " + distributionCenter.getId());
                }
            }
        }
        logger.info("Generated " + dataSet.getShortDistanceTruckTrips().size() + " short distance trips: " + totalVolume_tn + " tn");
    }


    private void disaggregateToSmallTrucks(double volume, Commodity commodity, DistributionCenter distributionCenter, Zone zone, boolean toDestination) {

        double load = dataSet.getTruckLoadsByDistanceAndCommodity().get(commodity, DistanceBin.D0_50);

        //no empty trucks here?
        int trucks_int = (int) Math.floor(volume/load);
        if (properties.getRand().nextDouble() < volume/load - trucks_int){
            trucks_int++;
        }

        //re-adjust load:
        load = volume / trucks_int;

        for (int t = 0; t < trucks_int; t++) {
            ShortDistanceTruckTrip sdtt;
            Coord distributionCenterCoord = distributionCenter.getCoordinates();
            if (toDestination) {
                int microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, (InternalZone) zone, dataSet.getUseTable());
                Coord customerCoord = ((InternalZone) zone).getMicroZones().get(microZone).getCoordinates();

                sdtt = new ShortDistanceTruckTrip(counter.getAndIncrement(), distributionCenterCoord, customerCoord, commodity, distributionCenter, toDestination, load);

            } else {
                int microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, (InternalZone) zone, dataSet.getMakeTable());
                Coord customerCoord = ((InternalZone) zone).getMicroZones().get(microZone).getCoordinates();

                sdtt = new ShortDistanceTruckTrip(counter.getAndIncrement(), customerCoord, distributionCenterCoord, commodity, distributionCenter, toDestination, load);
            }

            dataSet.getShortDistanceTruckTrips().add(sdtt);
        }
    }
}

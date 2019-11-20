package de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.DistanceBin;
import de.tum.bgu.msm.freight.data.freight.TruckTrip;
import de.tum.bgu.msm.freight.data.freight.urban.SDTruckTrip;
import de.tum.bgu.msm.freight.data.freight.Bound;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Id;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Disaggregates flows from distribution center to final destinations, for commodities that are delivered in small trucks and not
 * in parcels or units.
 */
public class SDTruckGenerator implements Module {

    private Properties properties;
    private DataSet dataSet;
    private static final Logger logger = Logger.getLogger(SDTruckGenerator.class);
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
        logger.info("Generated " + dataSet.getSDTruckTrips().size() + " short distance trips: " + totalVolume_tn + " tn");
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
            SDTruckTrip sdtt;
            Coordinate distributionCenterCoord = distributionCenter.getCoordinates();
            if (toDestination) {
                int microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity, distributionCenter.getZonesServedByThis(), dataSet.getUseTable(), properties.getRand());
                Coordinate customerCoord = ((InternalZone) zone).getMicroZones().get(microZone).getCoordinates(properties.getRand());
                Id<TruckTrip> truckTripId = Id.create("SD_" + counter.getAndIncrement(), TruckTrip.class);
                sdtt = new SDTruckTrip(truckTripId, distributionCenterCoord, customerCoord, commodity, distributionCenter, toDestination, load);

            } else {
                int microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(commodity,  distributionCenter.getZonesServedByThis(), dataSet.getMakeTable(), properties.getRand());
                Coordinate customerCoord = ((InternalZone) zone).getMicroZones().get(microZone).getCoordinates(properties.getRand());
                Id<TruckTrip> truckTripId = Id.create("SD_" + counter.getAndIncrement(), TruckTrip.class);
                sdtt = new SDTruckTrip(truckTripId, customerCoord, distributionCenterCoord, commodity, distributionCenter, toDestination, load);
            }

            dataSet.getSDTruckTrips().add(sdtt);
        }
    }
}

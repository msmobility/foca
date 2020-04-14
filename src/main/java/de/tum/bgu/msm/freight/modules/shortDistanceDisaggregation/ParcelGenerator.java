package de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelTransaction;
import de.tum.bgu.msm.freight.data.freight.Bound;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.ParcelShop;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.ParcelWeightDistribution_kg;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.modules.common.WeightDistribution;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.contrib.freight.utils.FreightUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates parcels using a weight distribution and allocates the trip end that is not the distribution center
 */
public class ParcelGenerator implements Module {

    private static final Logger logger = Logger.getLogger(ParcelGenerator.class);
    private Properties properties;
    private DataSet dataSet;
    private double minimumWeight_kg = 0.5;
    private Map<ParcelTransaction, Double> parcelDeliveryTransactionProbabilties;
    private Map<ParcelTransaction, Double> parcelPickUpTransactionProbabilties;

    private final AtomicInteger counter = new AtomicInteger(0);
    private WeightDistribution weightDistribution;

    private final int density_kg_m3 = 16;
    private double parcelWeightCounter = 0;
    private double postWeightCounter = 0;
    private int parcelCounter = 0;
    private int postCounter = 0;


    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        parcelDeliveryTransactionProbabilties = new HashMap<>();
        parcelPickUpTransactionProbabilties = new HashMap<>();
        for (ParcelTransaction parcelTransaction : ParcelTransaction.values()) {
            parcelDeliveryTransactionProbabilties.put(parcelTransaction, parcelTransaction.getShareDeliveriesAtCustomer());
            parcelPickUpTransactionProbabilties.put(parcelTransaction, parcelTransaction.getSharePickupsAtCustomer());
        }
        weightDistribution = new ParcelWeightDistribution_kg(dataSet, properties);


    }

    @Override
    public void run() {
        generateParcels();

        logger.info("Parcels: " + parcelCounter  + " units with a weight of " + parcelWeightCounter);
        logger.info("Post: " + postCounter  + " units with a weight of " + postWeightCounter + " (will not be considered in FOCA");

        chooseTransactionType();
        assignCoordinates();
    }



    private void generateParcels() {
        for (DistributionCenter distributionCenter : dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().rowKeySet()) {
            for (Commodity commodity : dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().columnKeySet()) {
                if (commodity.equals(Commodity.POST_PACKET)) {
                    Map<Bound, Double> volumesProcessedByThisDistributionCenter = dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().get(distributionCenter, commodity);
                    if (volumesProcessedByThisDistributionCenter != null) {
                        double volumeDelivered = volumesProcessedByThisDistributionCenter.getOrDefault(Bound.INBOUND, 0.) +
                                volumesProcessedByThisDistributionCenter.getOrDefault(Bound.INTRAZONAL, 0.) * 0.5;

                        logger.warn("dc: " + distributionCenter.getId() + " volume_to: " + volumeDelivered);
                        dissagregateVolumeToParcels(volumeDelivered, distributionCenter, true, commodity);

                        double volumePickedUp = volumesProcessedByThisDistributionCenter.getOrDefault(Bound.OUTBOUND, 0.) +
                                volumesProcessedByThisDistributionCenter.getOrDefault(Bound.INTRAZONAL, 0.) * 0.5;
                        logger.warn("dc: " + distributionCenter.getId() + " volume_fom: " + volumePickedUp);
                        dissagregateVolumeToParcels(volumePickedUp, distributionCenter, false, commodity);
                    }


                } else {
                    throw new RuntimeException("No assignment for parcels for this commodity is expected: " + commodity);
                }


            }
        }
        logger.info("Generated " + counter + " parcels to/from the study area.");


    }

    private void chooseTransactionType() {
        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()) {
            List<Parcel> parcels = dataSet.getParcelsByDistributionCenter().get(distributionCenter);
            for (Parcel parcel : parcels) {
                if (parcel.isToDestination()) {
                    ParcelTransaction parcelTransaction =
                            FreightFlowUtils.select(parcelDeliveryTransactionProbabilties, FreightFlowUtils.getSum(parcelDeliveryTransactionProbabilties.values()), properties.getRand());
                    parcel.setParcelTransaction(parcelTransaction);
                } else {
                    ParcelTransaction parcelTransaction =
                            FreightFlowUtils.select(parcelPickUpTransactionProbabilties, FreightFlowUtils.getSum(parcelDeliveryTransactionProbabilties.values()), properties.getRand());
                    parcel.setParcelTransaction(parcelTransaction);
                }
            }
        }

    }

    private void assignCoordinates() {
        AtomicInteger counter = new AtomicInteger(0);
        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()) {
            Map<ParcelShop, Double> parcelShopProbabilities = new HashMap<>();
            for (ParcelShop parcelShop : distributionCenter.getParcelShopsServedByThis()){
                parcelShopProbabilities.put(parcelShop, 1.);
            }
            for (Parcel parcel : dataSet.getParcelsByDistributionCenter().get(distributionCenter)) {
                if (parcel.isToDestination()) {
                    parcel.setOriginCoord(parcel.getDistributionCenter().getCoordinates());
                    InternalZone destinationZone = (InternalZone) dataSet.getZones().get(parcel.getDistributionCenter().getZoneId());
                    int microZone;
                    if (parcel.getParcelTransaction().equals(ParcelTransaction.PRIVATE_CUSTOMER)) {
                        microZone = SpatialDisaggregator.disaggregateToMicroZonePrivate(distributionCenter.getZonesServedByThis(), properties.getRand());
                        parcel.setDestMicroZone(microZone);
                        parcel.setDestCoord(destinationZone.getMicroZones().get(microZone).getCoordinates(properties.getRand()));
                    } else if (parcel.getParcelTransaction().equals(ParcelTransaction.BUSINESS_CUSTOMER)) {
                        microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(parcel.getCommodity(),
                                distributionCenter.getZonesServedByThis(), dataSet.getUseTable(), properties.getRand());
                        parcel.setDestMicroZone(microZone);
                        parcel.setDestCoord(destinationZone.getMicroZones().get(microZone).getCoordinates(properties.getRand()));
                    } else {
                        ParcelShop parcelShop = FreightFlowUtils.select(parcelShopProbabilities, parcelShopProbabilities.values().size(), properties.getRand() );
                        int microZoneId = parcelShop.getMicroZoneId();
                        parcel.setDestMicroZone(microZoneId);
                        parcel.setDestCoord(parcelShop.getCoord_gk4());
                        parcel.setParcelShop(parcelShop);
                    }
                } else {
                    parcel.setDestCoord(parcel.getDistributionCenter().getCoordinates());
                    InternalZone originZone = (InternalZone) dataSet.getZones().get(parcel.getDistributionCenter().getZoneId());
                    int microZone;
                    if (parcel.getParcelTransaction().equals(ParcelTransaction.PRIVATE_CUSTOMER)) {
                        microZone = SpatialDisaggregator.disaggregateToMicroZonePrivate(distributionCenter.getZonesServedByThis(), properties.getRand());
                        parcel.setOrigMicroZone(microZone);
                        parcel.setOriginCoord(originZone.getMicroZones().get(microZone).getCoordinates(properties.getRand()));
                    } else if (parcel.getParcelTransaction().equals(ParcelTransaction.BUSINESS_CUSTOMER)) {

                        microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(parcel.getCommodity(),
                                distributionCenter.getZonesServedByThis(), dataSet.getUseTable(), properties.getRand());
                        parcel.setOrigMicroZone(microZone);
                        parcel.setOriginCoord(originZone.getMicroZones().get(microZone).getCoordinates(properties.getRand()));
                    } else {
                        ParcelShop parcelShop = FreightFlowUtils.select(parcelShopProbabilities, parcelShopProbabilities.values().size(), properties.getRand() );
                        int microZoneId = parcelShop.getMicroZoneId();
                        parcel.setOrigMicroZone(microZoneId);
                        parcel.setOriginCoord(parcelShop.getCoord_gk4());
                        parcel.setParcelShop(parcelShop);
                    }
                }
                counter.incrementAndGet();
                if (counter.get() % 10000 == 0) {
                    logger.info(counter.get() + " parcels already processed");
                }

            }
        }
        logger.warn(properties.getRand().nextDouble());
        logger.info(counter.get() + " parcels already processed");

    }

    private void dissagregateVolumeToParcels(double volume_tn, DistributionCenter distributionCenter, boolean toCustomer, Commodity commodity) {

        List<Parcel> parcelsThisDistributionCenter;

        if (!dataSet.getParcelsByDistributionCenter().containsKey(distributionCenter)) {
            parcelsThisDistributionCenter = new ArrayList<>();
            dataSet.getParcelsByDistributionCenter().put(distributionCenter, parcelsThisDistributionCenter);
        } else {
            parcelsThisDistributionCenter = dataSet.getParcelsByDistributionCenter().get(distributionCenter);
        }


        double cum_weight = 0;
        while (cum_weight < volume_tn * 1000) {
            double weight_kg = weightDistribution.getRandomWeight(Commodity.POST_PACKET, 0.);
            if (weight_kg > minimumWeight_kg) {
                parcelsThisDistributionCenter.add(new Parcel(counter.getAndIncrement(),
                        toCustomer, weight_kg / density_kg_m3, weight_kg, distributionCenter, commodity));
                parcelWeightCounter += weight_kg;
                parcelCounter++;
            } else {
                postWeightCounter += weight_kg;
                postCounter++;
            }
            cum_weight += weight_kg;
        }
    }

}

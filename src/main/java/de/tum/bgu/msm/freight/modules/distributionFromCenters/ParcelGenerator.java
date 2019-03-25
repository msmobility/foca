package de.tum.bgu.msm.freight.modules.distributionFromCenters;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.Parcel;
import de.tum.bgu.msm.freight.data.freight.Transaction;
import de.tum.bgu.msm.freight.data.geo.Bound;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.ParcelEmpiricalWeightDistribution_kg;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.modules.common.WeightDistribution;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ParcelGenerator implements Module {

    private static final Logger logger = Logger.getLogger(ParcelGenerator.class);
    private Properties properties;
    private DataSet dataSet;
    private double minimumWeight_kg = 0.5;
    private Map<Transaction, Double> parcelDeliveryTransactionProbabilties;
    private Map<Transaction, Double> parcelPickUpTransactionProbabilties;

    private final AtomicInteger counter = new AtomicInteger(0);
    private WeightDistribution weightDistribution;


    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        parcelDeliveryTransactionProbabilties = new HashMap<>();
        parcelPickUpTransactionProbabilties = new HashMap<>();
        for (Transaction transaction : Transaction.values()){
            parcelDeliveryTransactionProbabilties.put(transaction, transaction.getShareDeliveriesAtCustomer());
            parcelPickUpTransactionProbabilties.put(transaction, transaction.getSharePickupsAtCustomer());
        }
        weightDistribution = new ParcelEmpiricalWeightDistribution_kg(dataSet, properties);


    }

    @Override
    public void run() {
        generateParcels();
        chooseTransactionType();
        assignCoordinates();
    }

    private void generateParcels() {
        for (DistributionCenter distributionCenter : dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().rowKeySet()){
            for (Commodity commodity : dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().columnKeySet()) {
                if (commodity.equals(Commodity.POST_PACKET)){
                    Map<Bound,Double> volumesProcessedByThisDistributionCenter = dataSet.getVolByCommodityDistributionCenterAndBoundByParcels().get(distributionCenter,commodity);
                    if (volumesProcessedByThisDistributionCenter != null){
                        double volumeDelivered = volumesProcessedByThisDistributionCenter.getOrDefault(Bound.INBOUND, 0.) +
                                volumesProcessedByThisDistributionCenter.getOrDefault(Bound.INTRAZONAL, 0.) * 0.5;

                        dissagregateVolumeToParcels(volumeDelivered, distributionCenter,true, commodity);

                        double volumePickedUp = volumesProcessedByThisDistributionCenter.getOrDefault(Bound.OUTBOUND, 0.) +
                                volumesProcessedByThisDistributionCenter.getOrDefault(Bound.INTRAZONAL,0.) * 0.5;

                        dissagregateVolumeToParcels(volumePickedUp, distributionCenter,false, commodity);
                    }


                } else {
                    throw new RuntimeException("No assignment for parcels for this commodity is expected: " + commodity);
                }


            }
        }
        logger.info("Generated " + counter + " parcels to/from the study area.");
    }

    private void chooseTransactionType() {
        for (Parcel parcel : dataSet.getParcels()){
            if (parcel.isToDestination()){
                Transaction transaction =
                        FreightFlowUtils.select(parcelDeliveryTransactionProbabilties, FreightFlowUtils.getSum(parcelDeliveryTransactionProbabilties.values()));
                parcel.setTransaction(transaction);
            } else {
                Transaction transaction =
                        FreightFlowUtils.select(parcelPickUpTransactionProbabilties, FreightFlowUtils.getSum(parcelDeliveryTransactionProbabilties.values()));
                parcel.setTransaction(transaction);
            }
        }

    }

    private void assignCoordinates() {
        AtomicInteger counter = new AtomicInteger(0);
        for (Parcel parcel : dataSet.getParcels()){
            if (parcel.isToDestination()) {
                parcel.setOriginCoord(parcel.getDistributionCenter().getCoordinates());
                InternalZone destinationZone = (InternalZone) dataSet.getZones().get(parcel.getDistributionCenter().getZoneId());
                int microZone;
                if (parcel.getTransaction().equals(Transaction.PRIVATE_CUSTOMER)) {
                    microZone = SpatialDisaggregator.disaggregateToMicroZonePrivate(destinationZone);
                    parcel.setDestCoord(destinationZone.getMicroZones().get(microZone).getCoordinates());
                } else if (parcel.getTransaction().equals(Transaction.BUSINESS_CUSTOMER)){
                    microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(parcel.getCommodity(),
                            destinationZone, dataSet.getUseTable());
                    parcel.setDestCoord(destinationZone.getMicroZones().get(microZone).getCoordinates());
                } else {
                    //todo nothing done now if it is not an individual customer
                }
            } else {
                parcel.setDestCoord(parcel.getDistributionCenter().getCoordinates());
                InternalZone originZone = (InternalZone) dataSet.getZones().get(parcel.getDistributionCenter().getZoneId());
                int microZone;
                if (parcel.getTransaction().equals(Transaction.PRIVATE_CUSTOMER)) {
                    microZone = SpatialDisaggregator.disaggregateToMicroZonePrivate(originZone);
                    parcel.setOriginCoord(originZone.getMicroZones().get(microZone).getCoordinates());
                } else if (parcel.getTransaction().equals(Transaction.BUSINESS_CUSTOMER)){
                    microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(parcel.getCommodity(),
                            originZone, dataSet.getUseTable());
                    parcel.setOriginCoord(originZone.getMicroZones().get(microZone).getCoordinates());
                } else {
                    //todo nothing done now if it is not an individual customer
                }
            }
            counter.incrementAndGet();
            if (counter.get() % 10000 == 0 ){
                logger.info(counter.get() + " parcels already processed");
            }

        }
        logger.info(counter.get() + " parcels already processed");
    }

    private void dissagregateVolumeToParcels(double volume_tn, DistributionCenter distributionCenter, boolean toCustomer, Commodity commodity){
        double cum_weight = 0;
        while (cum_weight < volume_tn * 1000) {
            double weight_kg = weightDistribution.getRandomWeight(Commodity.POST_PACKET, 0.);
                //todo temporary! volume is estimated by uniform density
                if (weight_kg > minimumWeight_kg) {
                    if (properties.getRand().nextDouble() < properties.getSampleFactorForParcels()) {
                        dataSet.getParcels().add(new Parcel(counter.getAndIncrement(), toCustomer, weight_kg / 16, weight_kg, distributionCenter, commodity));
                    }
                }
                cum_weight += weight_kg;
        }
    }

}

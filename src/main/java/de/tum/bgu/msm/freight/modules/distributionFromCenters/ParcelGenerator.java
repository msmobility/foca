package de.tum.bgu.msm.freight.modules.distributionFromCenters;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.Parcel;
import de.tum.bgu.msm.freight.data.freight.Transaction;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.Zone;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.common.SpatialDisaggregator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ParcelGenerator implements Module {

    private static final Logger logger = Logger.getLogger(ParcelGenerator.class);
    private Properties properties;
    private DataSet dataSet;
    private Zone zone;
    private double minimumWeight_kg = 0.5;


    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        this.zone = zone;

    }

    @Override
    public void run() {
        generateParcels();
        choseTransactionType();
        assignCoordinates();
    }

    private void generateParcels() {
        int counter = 0;

        for (DistributionCenter distributionCenter : dataSet.getFlowSegmentsDeliveredByParcel().keySet()){
            for (FlowSegment flowSegment : dataSet.getFlowSegmentsDeliveredByParcel().get(distributionCenter)) {
                double cum_weight = 0;
                while (cum_weight < flowSegment.getVolume_tn() * 1000 / properties.getDaysPerYear()) {
                    double weight = pickUpRandomWeight();
                    if (dataSet.getZones().get(flowSegment.getOrigin()).isInStudyArea() ||
                            dataSet.getZones().get(flowSegment.getDestination()).isInStudyArea()) {
                        boolean toDestination = dataSet.getZones().get(flowSegment.getDestination()).isInStudyArea();
                        //todo temporary! volume is estimated by uniform density
                        if (weight > minimumWeight_kg) {

                            if (properties.getRand().nextDouble() < properties.getSampleFactorForParcels()) {
                                dataSet.getParcels().add(new Parcel(counter, toDestination, weight / 16, weight, distributionCenter, flowSegment));
                                counter++;
                            }
                        }
                        cum_weight += weight;
                    } else {
                        throw new RuntimeException("Something failed here!!!!");
                    }
                }
            }
        }
        logger.info("Generated " + counter + " parcels to/from the study area.");
    }

    private void choseTransactionType() {
        for (Parcel parcel : dataSet.getParcels()){
            if (parcel.isToDestination()){
                parcel.setTransaction(properties.getRand().nextDouble() < properties.getShareB22Recipients() ? Transaction.B2B :  Transaction.B2C);
            } else {
                parcel.setTransaction(properties.getRand().nextDouble() < properties.getShareB2Bsenders() ? Transaction.B2B :  Transaction.B2C);
            }

        }


    }

    private void assignCoordinates() {
        AtomicInteger counter = new AtomicInteger(0);
        for (Parcel parcel : dataSet.getParcels()){

            if (parcel.isToDestination()) {
                parcel.setOriginCoord(parcel.getDistributionCenter().getCoordinates());
                InternalZone destinationZone = (InternalZone) dataSet.getZones().get(parcel.getFlowSegment().getDestination());
                int microZone;
                if (parcel.getTransaction().equals(Transaction.B2C)) {
                    microZone = SpatialDisaggregator.disaggregateToMicroZonePrivate(destinationZone);
                } else {
                    microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(parcel.getFlowSegment().getCommodity(),
                            destinationZone, dataSet.getUseTable());
                }
                parcel.setDestCoord(destinationZone.getMicroZones().get(microZone).getCoordinates());
            } else {
                parcel.setDestCoord(parcel.getDistributionCenter().getCoordinates());
                InternalZone originZone = (InternalZone) dataSet.getZones().get(parcel.getFlowSegment().getOrigin());
                int microZone;
                if (parcel.getTransaction().equals(Transaction.B2C)) {
                    microZone = SpatialDisaggregator.disaggregateToMicroZonePrivate(originZone );
                } else {
                    microZone = SpatialDisaggregator.disaggregateToMicroZoneBusiness(parcel.getFlowSegment().getCommodity(),
                            originZone, dataSet.getUseTable());
                }
                parcel.setDestCoord(originZone.getMicroZones().get(microZone).getCoordinates());
            }
            counter.incrementAndGet();
            if (counter.get() % 10000 == 0 ){
                logger.info(counter.get() + " parcels already processed");
            }

        }
        logger.info(counter.get() + " parcels already processed");
    }

    private double pickUpRandomWeight(){
        Map<Double,Double> weightDistribution = dataSet.getParcelWeightDistribution();


        return FreightFlowUtils.select(weightDistribution, FreightFlowUtils.getSum(weightDistribution.values()));

    }

}

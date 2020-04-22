package de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelDistributionType;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelTransaction;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.List;

public class GlobalModalShareModeChoice implements ModeChoiceModel {

    private static Logger logger = Logger.getLogger(GlobalModalShareModeChoice.class);
    private Properties properties;
    private DataSet dataSet;

    public double getShareOfCargoBikesAtThisMicroZone(int microzoneId, double weight) {
        return properties.shortDistance().getShareOfCargoBikesAtZonesServedByMicroDepot();
    }

    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.properties = properties;
        this.dataSet = dataSet;
    }

    @Override
    public void run() {
        chooseParcelDistributionType();
    }


    private void chooseParcelDistributionType() {

        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()) {
            List<Integer> internalZonesServedByMicroDepots = new ArrayList<>();
            for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()){
                for (InternalMicroZone internalMicroZone : microDepot.getZonesServedByThis()){
                    internalZonesServedByMicroDepots.add(internalMicroZone.getId());
                }
            }

            for (Parcel parcel : dataSet.getParcelsByDistributionCenter().get(distributionCenter)) {
                double randomValue = properties.getRand().nextDouble();
                //random number to assign to microdepot or to motorized(here, to try to obtain deterministic results)
                if (!parcel.isToDestination()) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }

                if (parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }

                if (!internalZonesServedByMicroDepots.contains(parcel.getDestMicroZoneId())) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }
                if (parcel.getWeight_kg() > properties.modeChoice().getMaxWeightForCargoBike_kg()) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }
                //may be distributed by cargo bikes, then depend on the share only
                if (randomValue <
                        getShareOfCargoBikesAtThisMicroZone(parcel.getDestMicroZoneId(), parcel.getWeight_kg())) {
                    here:
                    for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {
                        InternalZone internalZone = (InternalZone) dataSet.getZones().get(distributionCenter.getZoneId());
                        if (microDepot.getZonesServedByThis().contains(internalZone.getMicroZones().get(parcel.getDestMicroZoneId()))) {
                            parcel.setParcelDistributionType(ParcelDistributionType.CARGO_BIKE);
                            parcel.setMicroDepot(microDepot);
                            break here;
                        }
                    }
                } else {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                }
            }
        }
        logger.info("Finished mode choice model");
    }

}

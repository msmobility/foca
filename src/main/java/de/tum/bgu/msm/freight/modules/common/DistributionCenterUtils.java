package de.tum.bgu.msm.freight.modules.common;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Bound;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;

import java.util.HashMap;
import java.util.Map;

public class DistributionCenterUtils {


    public static void addVolumeForSmallTruckDelivery(DistributionCenter distributionCenter, Commodity commodity, Bound bound, double load_tn, DataSet dataSet) {
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

    public static void addVolumeForParcelDelivery(DistributionCenter distributionCenter, Commodity commodity, Bound bound, double load_tn, DataSet dataSet) {
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

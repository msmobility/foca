package de.tum.bgu.msm.freight.modules.common;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.data.freight.DistanceBin;
import de.tum.bgu.msm.freight.properties.Properties;

import java.util.Random;

public class UniformTruckWeightDistribution_tn implements WeightDistribution {

    private final double load_range_factor = 0.25;
    private DataSet dataSet;
    private Random random;

    public UniformTruckWeightDistribution_tn(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.random = properties.getRand();
    }


    @Override
    public double getRandomWeight(Commodity commodity, double distance) {
        double mean = dataSet.getTruckLoadsByDistanceAndCommodity().get(commodity, DistanceBin.getDistanceBin(distance));
        return mean + (0.5 - random.nextDouble()) * load_range_factor * mean;
    }
}

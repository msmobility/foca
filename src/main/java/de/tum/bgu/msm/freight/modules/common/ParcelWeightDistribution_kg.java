package de.tum.bgu.msm.freight.modules.common;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Commodity;
import de.tum.bgu.msm.freight.properties.Properties;

import java.util.Map;
import java.util.Random;

public class ParcelWeightDistribution_kg implements WeightDistribution {

    private final DataSet dataSet;
    private final double weightDistributionInterval;
    private final Map<Double,Double> weightDistribution;
    private final Random random;

    public ParcelWeightDistribution_kg(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.weightDistributionInterval = dataSet.getWeightDistributionInterval();
        this.weightDistribution = dataSet.getParcelWeightDistribution();
        this.random = properties.getRand();
    }

    @Override
    public double getRandomWeight(Commodity commodity, double distance) {
        return FreightFlowUtils.select(weightDistribution, FreightFlowUtils.getSum(weightDistribution.values()), random) - weightDistributionInterval * random.nextDouble();
    }

}

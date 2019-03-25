package de.tum.bgu.msm.freight.modules.common;

import de.tum.bgu.msm.freight.data.freight.Commodity;

import java.util.Random;

public interface WeightDistribution {


    double getRandomWeight(Commodity commodity, double distance);
}

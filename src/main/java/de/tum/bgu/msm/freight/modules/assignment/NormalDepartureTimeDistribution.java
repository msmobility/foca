package de.tum.bgu.msm.freight.modules.assignment;


import de.tum.bgu.msm.freight.FreightFlowUtils;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.HashMap;
import java.util.Map;

public class NormalDepartureTimeDistribution implements DepartureTimeDistribution{


    private final double mean = 9.91;
    private final double standardDeviation = 5.94;


    private Map<Integer, Double> preCalculatedProbabilities;


    public NormalDepartureTimeDistribution(){
        preCalculatedProbabilities = new HashMap<>();

        NormalDistribution normalDistribution = new NormalDistribution(mean, standardDeviation);
        normalDistribution.reseedRandomGenerator(1);

        for (int minute = 0; minute < 24*60; minute++){
            double minuteInHours = Double.valueOf(minute)/60;
            preCalculatedProbabilities.put(minute, normalDistribution.density(minuteInHours) +
                    normalDistribution.density(minuteInHours - 24) +
                    normalDistribution.density(minuteInHours + 24));
        }
    }


    @Override
    public double getDepartureTime(double travelTime_m) {
        return FreightFlowUtils.select(preCalculatedProbabilities, FreightFlowUtils.getSum(preCalculatedProbabilities.values()));
    }
}

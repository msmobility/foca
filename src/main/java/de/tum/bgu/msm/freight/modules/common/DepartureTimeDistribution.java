package de.tum.bgu.msm.freight.modules.common;

/**
 * Departure time distribution for trucks
 */
public interface DepartureTimeDistribution {

    double getDepartureTime(double travelTime);
}

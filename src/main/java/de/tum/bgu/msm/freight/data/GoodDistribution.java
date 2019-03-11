package de.tum.bgu.msm.freight.data;

public enum GoodDistribution {

    /**
     * One truck from origin to destination
     */
    DOOR_TO_DOOR,
    /**
     * If the truck arrives/departs to/from the study areas, dissagregate to parcels from distribution centers
     */
    SINGLE_DELIVERY,
    /**
     * Deliveris of single - small trucks from distribution centers
     */
    SINGLE_VEHICLE

}

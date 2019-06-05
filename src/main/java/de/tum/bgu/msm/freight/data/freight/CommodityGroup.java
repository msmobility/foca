package de.tum.bgu.msm.freight.data.freight;

public enum CommodityGroup {

    AGRI (LDDistributionType.DOOR_TO_DOOR),
    PRIMARY (LDDistributionType.DOOR_TO_DOOR),
    SOIL_ROCK (LDDistributionType.DOOR_TO_DOOR),
    FOOD (LDDistributionType.SINGLE_VEHICLE),
    MANUFACT(LDDistributionType.SINGLE_VEHICLE),
    HEAVY_MANUFACT (LDDistributionType.DOOR_TO_DOOR),
    CHEMICAL (LDDistributionType.DOOR_TO_DOOR),
    WASTE (LDDistributionType.DOOR_TO_DOOR),
    GROUP(LDDistributionType.SINGLE_VEHICLE),
    PACKET(LDDistributionType.PARCEL_DELIVERY),
    OTHER (LDDistributionType.DOOR_TO_DOOR),
    EMPTY(LDDistributionType.DOOR_TO_DOOR);

    private LDDistributionType longDistanceGoodDistribution;

    CommodityGroup(LDDistributionType LDDistributionType) {
        this.longDistanceGoodDistribution = LDDistributionType;
    }

    public LDDistributionType getLongDistanceGoodDistribution() {
        return longDistanceGoodDistribution;
    }
}

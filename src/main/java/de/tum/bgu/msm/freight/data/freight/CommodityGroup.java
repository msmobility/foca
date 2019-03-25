package de.tum.bgu.msm.freight.data.freight;

public enum CommodityGroup {

    AGRI (GoodDistribution.DOOR_TO_DOOR),
    PRIMARY (GoodDistribution.DOOR_TO_DOOR),
    SOIL_ROCK (GoodDistribution.DOOR_TO_DOOR),
    FOOD (GoodDistribution.SINGLE_VEHICLE),
    MANUFACT(GoodDistribution.SINGLE_VEHICLE),
    HEAVY_MANUFACT (GoodDistribution.DOOR_TO_DOOR),
    CHEMICAL (GoodDistribution.DOOR_TO_DOOR),
    WASTE (GoodDistribution.DOOR_TO_DOOR),
    GROUP(GoodDistribution.SINGLE_VEHICLE),
    PACKET(GoodDistribution.PARCEL_DELIVERY),
    OTHER (GoodDistribution.DOOR_TO_DOOR),
    EMPTY(GoodDistribution.DOOR_TO_DOOR);

    private GoodDistribution goodDistribution;

    CommodityGroup(GoodDistribution goodDistribution) {
        this.goodDistribution = goodDistribution;
    }

    public GoodDistribution getGoodDistribution() {
        return goodDistribution;
    }
}

package de.tum.bgu.msm.freight.data.freight;

import java.util.HashMap;
import java.util.Map;

public enum Commodity {

    AGRI(10, CommodityGroup.AGRI),
    BLACK_COAL (21, CommodityGroup.PRIMARY),
    BROWN_COAL (22, CommodityGroup.PRIMARY),
    OIL_GAS(23, CommodityGroup.PRIMARY),
    ORE(31, CommodityGroup.PRIMARY),
    FERTILIZER(32,CommodityGroup.PRIMARY),
    MINERAL(33, CommodityGroup.SOIL_ROCK),
    FOOD(40, CommodityGroup.FOOD),
    TEXTIL(50,CommodityGroup.MANUFACT),
    WOOD_PAPER(60, CommodityGroup.MANUFACT),
    COKE(71,CommodityGroup.PRIMARY),
    MINERAL_OIL(72, CommodityGroup.PRIMARY),
    CHEMICAL(80, CommodityGroup.CHEMICAL),
    OTHER_MINERAL(90, CommodityGroup.PRIMARY),
    METAL(100, CommodityGroup.HEAVY_MANUFACT),
    MACHINE(110, CommodityGroup.HEAVY_MANUFACT),
    VEHICLE(120, CommodityGroup.HEAVY_MANUFACT),
    FURNITURE_SPORT(130, CommodityGroup.MANUFACT),
    SECONDARY_WASTE(140, CommodityGroup.WASTE),
    POST_PACKET(150, CommodityGroup.PACKET),
    FOR_FREIGHT_MOVEMENT(160, CommodityGroup.MANUFACT),
    NOT_MARKETED(170, CommodityGroup.MANUFACT),
    GROUPED_GOODS(180, CommodityGroup.GROUP),
    NOT_IDENTIFIED(190, CommodityGroup.OTHER),
    OTHER(200, CommodityGroup.OTHER),
    EMPTY(0, CommodityGroup.EMPTY);

    private final int code;
    private CommodityGroup commodityGroup;

    Commodity(int code, CommodityGroup group){
        this.code = code;
        this.commodityGroup = group;
    }

    public static Map<Integer, Commodity> getMapOfValues(){
        Map<Integer, Commodity> mapOfValuesAndCodes = new HashMap<Integer, Commodity>();
        for(Commodity commodity : values()){
            mapOfValuesAndCodes.put(commodity.code, commodity);
        }
        return mapOfValuesAndCodes;
    }

    public CommodityGroup getCommodityGroup(){
        return this.commodityGroup;
    }

    public int getCode() {
        return code;
    }
}

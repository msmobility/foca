package de.tum.bgu.msm.freight.data;

import java.util.HashMap;
import java.util.Map;

public enum Commodity {

    AGRI(10),
    BLACK_COAL (21),
    BROWN_COAL (22),
    OIL_GAS(23),
    ORE(31),
    FERTILIZER(32),
    MINERAL(33),
    FOOD(40),
    TEXTIL(50),
    WOOD_PAPER(60),
    COKE(71),
    MINERAL_OIL(72),
    CHEMICAL(80),
    OTHER_MINERAL(90),
    METAL(100),
    MACHINE(110),
    VEHICLE(120),
    FURNITURE_SPORT(130),
    SECONDARY_WASTE(140),
    POST_PACKET(150),
    FOR_FREIGHT_MOVEMENT(160),
    NOT_MARKETED(170),
    GROUPED_GOODS(180),
    NOT_IDENTIFIED(190),
    OTHER(200),
    EMPTY(0);

    private final int code;

    Commodity(int code){
        this.code = code;
    }

    public static Map<Integer, Commodity> getMapOfValues(){
        Map<Integer, Commodity> mapOfValuesAndCodes = new HashMap<Integer, Commodity>();
        for(Commodity commodity : values()){
            mapOfValuesAndCodes.put(commodity.code, commodity);
        }
        return mapOfValuesAndCodes;
    }
}

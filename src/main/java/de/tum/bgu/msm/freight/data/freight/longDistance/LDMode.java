package de.tum.bgu.msm.freight.data.freight.longDistance;

public enum LDMode {

    RAIL(1),
    ROAD(2),
    INLAND_WATER(3);

    private final int code;

    LDMode(int code){
        this.code = code;
    }

    public static LDMode valueOf(int code){
        switch (code){
            case 1:
                return RAIL;
            case 2:
                return ROAD;
            case 3:
                return INLAND_WATER;
            default:
                throw new RuntimeException("Mode for code " + code + "not specified.");

        }
    }


}

package de.tum.bgu.msm.freight.data.freight.longDistance;

public enum FlowType {

    EMPTY(0),
    CONVENTIONAL (1),
    CONTAINER_RO_RO(2);


    private int code;

    FlowType(int code) {
        this.code = code;
    }

    public static FlowType getFromCode(int code){
        switch (code) {
            case 1:
                return FlowType.CONVENTIONAL;
            case 2:
                return FlowType.CONTAINER_RO_RO;
            default:
                throw new RuntimeException("Flow type for code " + code + "not specified.");
        }
    }
}

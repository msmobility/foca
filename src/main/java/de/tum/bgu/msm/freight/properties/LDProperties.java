package de.tum.bgu.msm.freight.properties;


public class LDProperties extends PropertiesGroup {

    private boolean disaggregateLongDistanceFlows = true;
    private String longDistanceTruckInputFile = null;

    public boolean isDisaggregateLongDistanceFlows() {
        return disaggregateLongDistanceFlows;
    }

    public void setDisaggregateLongDistanceFlows(boolean disaggregateLongDistanceFlows) {
        this.disaggregateLongDistanceFlows = disaggregateLongDistanceFlows;
    }

    public String getLongDistanceTruckInputFile() {
        return longDistanceTruckInputFile;
    }

    public void setLongDistanceTruckInputFile(String longDistanceTruckInputFile) {
        this.longDistanceTruckInputFile = longDistanceTruckInputFile;
    }
}

package de.tum.bgu.msm.freight.properties;


import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class LDProperties extends PropertiesGroup {

    private boolean disaggregateLongDistanceFlows = true;
    private String longDistanceTruckInputFile = null;
    private double truckScaleFactor = 1.;
    private boolean storeExpectedTimes = false;
    private int daysPerYear = 365;
    private String distributionCentersFile = "input/distributionCenters/distributionCenters.csv";

    public LDProperties(ResourceBundle bundle) {
        super(bundle);
        distributionCentersFile = PropertiesUtil.getStringProperty(bundle, "distributionCentersFile", distributionCentersFile);
        truckScaleFactor = PropertiesUtil.getDoubleProperty(bundle, "truckScaleFactor", truckScaleFactor);
        storeExpectedTimes = PropertiesUtil.getBooleanProperty(bundle, "storeExpectedTimes", storeExpectedTimes);
        daysPerYear = PropertiesUtil.getIntProperty(bundle, "daysPerYear", daysPerYear);

    }

    public double getTruckScaleFactor() {
        return truckScaleFactor;
    }

    public void setTruckScaleFactor(double truckScaleFactor) {
        this.truckScaleFactor = truckScaleFactor;
    }

    public boolean isStoreExpectedTimes() {
        return storeExpectedTimes;
    }

    public void setStoreExpectedTimes(boolean storeExpectedTimes) {
        this.storeExpectedTimes = storeExpectedTimes;
    }

    public int getDaysPerYear() {
        return daysPerYear;
    }

    public void setDaysPerYear(int daysPerYear) {
        this.daysPerYear = daysPerYear;
    }


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

    public String getDistributionCentersFile() {
        return distributionCentersFile;
    }

    public void setDistributionCentersFile(String distributionCentersFile) {
        this.distributionCentersFile = distributionCentersFile;
    }

}

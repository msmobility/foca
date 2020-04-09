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
    private String simpleNetworkFile = "./networks/matsim/europe_v2.xml.gz";

    public LDProperties(ResourceBundle bundle) {
        super(bundle);
        PropertiesUtil.newPropertySubmodule("Long-distance trucks");
        disaggregateLongDistanceFlows = PropertiesUtil.getBooleanProperty(bundle, "disaggregateLongDistanceFlows", disaggregateLongDistanceFlows);
        longDistanceTruckInputFile = PropertiesUtil.getStringProperty(bundle, "longDistanceTruckInputFile", longDistanceTruckInputFile);
        truckScaleFactor = PropertiesUtil.getDoubleProperty(bundle, "truckScaleFactor", truckScaleFactor);
        storeExpectedTimes = PropertiesUtil.getBooleanProperty(bundle, "storeExpectedTimes", storeExpectedTimes);
        daysPerYear = PropertiesUtil.getIntProperty(bundle, "daysPerYear", daysPerYear);
        distributionCentersFile = PropertiesUtil.getStringProperty(bundle, "distributionCentersFile", distributionCentersFile);
        simpleNetworkFile = PropertiesUtil.getStringProperty(bundle, "terminalsFileName", simpleNetworkFile);

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

    public String getSimpleNetworkFile() {
        return simpleNetworkFile;
    }

    public void setSimpleNetworkFile(String simpleNetworkFile) {
        this.simpleNetworkFile = simpleNetworkFile;
    }

}

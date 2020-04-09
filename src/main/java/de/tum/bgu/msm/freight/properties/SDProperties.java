package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class SDProperties extends PropertiesGroup {

    private double shareOfCargoBikesAtZonesServedByMicroDepot = 1.;
    private int[] selectedDistributionCenters = new int[]{-1};
    private int maxNumberOfParcelsByCarrier = 200;
    private boolean readMicroDepotsFromFile = true;
    private double maxDistanceToMicroDepot = 4000;

    public SDProperties(ResourceBundle bundle) {
        super(bundle);
        PropertiesUtil.newPropertySubmodule("Short-distance trucks and parcel disaggregation");
        shareOfCargoBikesAtZonesServedByMicroDepot = PropertiesUtil.getDoubleProperty(bundle, "shareOfCargoBikesAtZonesServedByMicroDepot", shareOfCargoBikesAtZonesServedByMicroDepot);
        selectedDistributionCenters = PropertiesUtil.getIntPropertyArray(bundle, "selectedDistributionCenters", selectedDistributionCenters);
        maxNumberOfParcelsByCarrier = PropertiesUtil.getIntProperty(bundle, "maxNumberOfParcelsByCarrier", maxNumberOfParcelsByCarrier);
        readMicroDepotsFromFile = PropertiesUtil.getBooleanProperty(bundle, "readMicroDepotsFromFile", readMicroDepotsFromFile);
        maxDistanceToMicroDepot = PropertiesUtil.getDoubleProperty(bundle, "maxDistanceToMicroDepot", maxDistanceToMicroDepot);
    }

    public void setMaxDistanceToMicroDepot(double maxDistanceToMicroDepot) {
        this.maxDistanceToMicroDepot = maxDistanceToMicroDepot;
    }

    public void setDistanceBetweenMicrodepotsInGrid(double distanceBetweenMicrodepotsInGrid) {
        this.distanceBetweenMicrodepotsInGrid = distanceBetweenMicrodepotsInGrid;
    }

    private double distanceBetweenMicrodepotsInGrid = 1000;


    public int[] getSelectedDistributionCenters() {
        return selectedDistributionCenters;
    }

    public void setSelectedDistributionCenters(int[] selectedDistributionCenters) {
        this.selectedDistributionCenters = selectedDistributionCenters;
    }

    public double getShareOfCargoBikesAtZonesServedByMicroDepot() {
        return shareOfCargoBikesAtZonesServedByMicroDepot;
    }

    public void setShareOfCargoBikesAtZonesServedByMicroDepot(double shareOfCargoBikesAtZonesServedByMicroDepot) {
        this.shareOfCargoBikesAtZonesServedByMicroDepot = shareOfCargoBikesAtZonesServedByMicroDepot;
    }

    public int getMaxParcelsByCarrier(){
        return maxNumberOfParcelsByCarrier;
    }

    public boolean isReadMicroDepotsFromFile() {
        return readMicroDepotsFromFile;
    }

    public void setReadMicroDepotsFromFile(boolean readMicroDepotsFromFile) {
        this.readMicroDepotsFromFile = readMicroDepotsFromFile;
    }

    public double getMaxDistanceToMicroDepot() {
        return maxDistanceToMicroDepot;
    }

    public double getDistanceBetweenMicrodepotsInGrid() {
        return distanceBetweenMicrodepotsInGrid;
    }
}

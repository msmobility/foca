package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class ModeChoiceProperties extends PropertiesGroup {

    private double operatingCostBike_eur_km = 0.9200;
    private double operatingCostTruck_eur_km = 1.7765;
    private double kApproximation = 1.5;
    private double serviceCostBike_eur_parcel = 1.0152;
    private double serviceCostTruck_eur_parcel = 1.2585;
    private double extraHandlingBike_eur_m3 = 8.4;
    private double capacityTruck_m3 = 12.5;
    private double capacityFeeder_m3 = 12.5;
    private double maxWeightForCargoBike_kg = 10;
    private double gridSpacing = 4000;

    public ModeChoiceProperties(ResourceBundle bundle) {
        super(bundle);
        PropertiesUtil.newPropertySubmodule("Mode choice");
        operatingCostBike_eur_km = PropertiesUtil.getDoubleProperty(bundle, "operatingCostBike_eur_km", operatingCostBike_eur_km);
        operatingCostTruck_eur_km = PropertiesUtil.getDoubleProperty(bundle, "operatingCostTruck_eur_km", operatingCostTruck_eur_km);
        kApproximation = PropertiesUtil.getDoubleProperty(bundle, "kApproximation", kApproximation);
        serviceCostBike_eur_parcel = PropertiesUtil.getDoubleProperty(bundle, "serviceCostBike_eur_parcel", serviceCostBike_eur_parcel);
        serviceCostTruck_eur_parcel = PropertiesUtil.getDoubleProperty(bundle, "serviceCostTruck_eur_parcel", serviceCostTruck_eur_parcel);
        extraHandlingBike_eur_m3 = PropertiesUtil.getDoubleProperty(bundle, "extraHandlingBike_eur_m3", extraHandlingBike_eur_m3);
        capacityTruck_m3 = PropertiesUtil.getDoubleProperty(bundle, "capacityTruck_m3", capacityTruck_m3);
        capacityFeeder_m3 = PropertiesUtil.getDoubleProperty(bundle, "capacityFeeder_m3", capacityFeeder_m3);
        maxWeightForCargoBike_kg = PropertiesUtil.getDoubleProperty(bundle, "maxWeightForCargoBike_kg", maxWeightForCargoBike_kg);
        gridSpacing = PropertiesUtil.getDoubleProperty(bundle, "gridSpacing", gridSpacing);


    }

    public double getOperatingCostBike_eur_km() {
        return operatingCostBike_eur_km;
    }

    public void setOperatingCostBike_eur_km(double operatingCostBike_eur_km) {
        this.operatingCostBike_eur_km = operatingCostBike_eur_km;
    }

    public double getOperatingCostTruck_eur_km() {
        return operatingCostTruck_eur_km;
    }

    public void setOperatingCostTruck_eur_km(double operatingCostTruck_eur_km) {
        this.operatingCostTruck_eur_km = operatingCostTruck_eur_km;
    }

    public double getkApproximation() {
        return kApproximation;
    }

    public void setkApproximation(double kApproximation) {
        this.kApproximation = kApproximation;
    }

    public double getServiceCostBike_eur_parcel() {
        return serviceCostBike_eur_parcel;
    }

    public void setServiceCostBike_eur_parcel(double serviceCostBike_eur_parcel) {
        this.serviceCostBike_eur_parcel = serviceCostBike_eur_parcel;
    }

    public double getServiceCostTruck_eur_parcel() {
        return serviceCostTruck_eur_parcel;
    }

    public void setServiceCostTruck_eur_parcel(double serviceCostTruck_eur_parcel) {
        this.serviceCostTruck_eur_parcel = serviceCostTruck_eur_parcel;
    }

    public double getExtraHandlingBike_eur_m3() {
        return extraHandlingBike_eur_m3;
    }

    public void setExtraHandlingBike_eur_m3(double extraHandlingBike_eur_m3) {
        this.extraHandlingBike_eur_m3 = extraHandlingBike_eur_m3;
    }

    public double getCapacityTruck_m3() {
        return capacityTruck_m3;
    }

    public void setCapacityTruck_m3(double capacityTruck_m3) {
        this.capacityTruck_m3 = capacityTruck_m3;
    }

    public double getCapacityFeeder_m3() {
        return capacityFeeder_m3;
    }

    public void setCapacityFeeder_m3(double capacityFeeder_m3) {
        this.capacityFeeder_m3 = capacityFeeder_m3;
    }

    public double getMaxWeightForCargoBike_kg() {
        return maxWeightForCargoBike_kg;
    }

    public void setMaxWeightForCargoBike_kg(double maxWeightForCargoBike_kg) {
        this.maxWeightForCargoBike_kg = maxWeightForCargoBike_kg;
    }

    public double getGridSpacing() {
        return gridSpacing;
    }

    public void setGridSpacing(double gridSpacing) {
        this.gridSpacing = gridSpacing;
    }
}

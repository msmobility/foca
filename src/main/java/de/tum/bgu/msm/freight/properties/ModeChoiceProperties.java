package de.tum.bgu.msm.freight.properties;

import de.tum.bgu.msm.properties.PropertiesUtil;

import java.util.ResourceBundle;

public class ModeChoiceProperties extends PropertiesGroup {

    private double operatingCostBike_eur_km = 3.1097;
    private double operatingCostTruck_eur_km = 2.8817;
    private double operatingCostFeeder_eur_km = 1.9301;
    private double kApproximation = 0.820;
    private double serviceCostBike_eur_parcel = 1.0248;
    private double serviceCostTruck_eur_parcel = 1.1386;
    private double extraHandlingBike_eur_unit = 0.76;
    private double capacityTruck_units = 120;
    private double capacityFeeder_units = 240;
    private double maxWeightForCargoBike_kg = 1000;
    //private double gridSpacing = 4000;

    public ModeChoiceProperties(ResourceBundle bundle) {
        super(bundle);
        PropertiesUtil.newPropertySubmodule("Mode choice");
        operatingCostBike_eur_km = PropertiesUtil.getDoubleProperty(bundle, "operatingCostBike_eur_km", operatingCostBike_eur_km);
        operatingCostTruck_eur_km = PropertiesUtil.getDoubleProperty(bundle, "operatingCostTruck_eur_km", operatingCostTruck_eur_km);
        kApproximation = PropertiesUtil.getDoubleProperty(bundle, "kApproximation", kApproximation);
        serviceCostBike_eur_parcel = PropertiesUtil.getDoubleProperty(bundle, "serviceCostBike_eur_parcel", serviceCostBike_eur_parcel);
        serviceCostTruck_eur_parcel = PropertiesUtil.getDoubleProperty(bundle, "serviceCostTruck_eur_parcel", serviceCostTruck_eur_parcel);
        extraHandlingBike_eur_unit = PropertiesUtil.getDoubleProperty(bundle, "extraHandlingBike_eur_unit", extraHandlingBike_eur_unit);
        capacityTruck_units = PropertiesUtil.getDoubleProperty(bundle, "capacityTruck_units", capacityTruck_units);
        capacityFeeder_units = PropertiesUtil.getDoubleProperty(bundle, "capacityFeeder_units", capacityFeeder_units);
        maxWeightForCargoBike_kg = PropertiesUtil.getDoubleProperty(bundle, "maxWeightForCargoBike_kg", maxWeightForCargoBike_kg);
        //gridSpacing = PropertiesUtil.getDoubleProperty(bundle, "gridSpacing", gridSpacing);


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

    public double getExtraHandlingBike_eur_unit() {
        return extraHandlingBike_eur_unit;
    }

    public void setExtraHandlingBike_eur_unit(double extraHandlingBike_eur_unit) {
        this.extraHandlingBike_eur_unit = extraHandlingBike_eur_unit;
    }

    public double getCapacityTruck_units() {
        return capacityTruck_units;
    }

    public void setCapacityTruck_units(double capacityTruck_units) {
        this.capacityTruck_units = capacityTruck_units;
    }

    public double getCapacityFeeder_units() {
        return capacityFeeder_units;
    }

    public void setCapacityFeeder_units(double capacityFeeder_units) {
        this.capacityFeeder_units = capacityFeeder_units;
    }

    public double getMaxWeightForCargoBike_kg() {
        return maxWeightForCargoBike_kg;
    }

    public void setMaxWeightForCargoBike_kg(double maxWeightForCargoBike_kg) {
        this.maxWeightForCargoBike_kg = maxWeightForCargoBike_kg;
    }

//    public double getGridSpacing() {
//        return gridSpacing;
//    }
//
//    public void setGridSpacing(double gridSpacing) {
//        this.gridSpacing = gridSpacing;
//    }

    public double getOperatingCostFeeder_eur_km() {
        return operatingCostFeeder_eur_km;
    }
}

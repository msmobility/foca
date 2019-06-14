package de.tum.bgu.msm.emission.data;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.Vehicle;

import java.util.EnumMap;

public class AnalyzedVehicle extends AnalyzedObject<Vehicle> {

    private double distanceTravelled;

    public AnalyzedVehicle(Id<Vehicle> id) {
        super(id);
    }

    public double getDistanceTravelled(){
        return distanceTravelled;
    }

    public void addDistanceTravelled(double length) {
        this.distanceTravelled += length;
    }
}

package de.tum.bgu.msm.emission.data;

import org.matsim.api.core.v01.Id;
import org.matsim.vehicles.Vehicle;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class AnalyzedObject<T> {
    protected Id<T> id;
    protected Map<String, Double> warmEmissions;
    protected Map<String, Double> coldEmissions;

    public AnalyzedObject(Id<T> id) {
        this.id = id;
        this.warmEmissions = new HashMap<>();
        this.coldEmissions = new HashMap<>();
    }

    public Map<String, Double>  getWarmEmissions() {
        return warmEmissions;
    }

    public Map<String, Double>  getColdEmissions() {
        return coldEmissions;
    }

    public Id<T> getId(){
        return id;
    }
}

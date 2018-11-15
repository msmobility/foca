package de.tum.bgu.msm.freight.data;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.data.Zone;
import org.matsim.api.core.v01.Id;

import javax.swing.*;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FreightFlowsDataSet {

    private Map<Integer, Zone> zones = new HashMap<Integer, Zone>();

    private HashBasedTable<Integer, Integer, ArrayList<OrigDestFlow>> flowMatrix = HashBasedTable.create();

    private HashBasedTable<Integer, Integer, Double> uncongestedTravelTimeMatrix = HashBasedTable.create();

    private HashBasedTable<Commodity, DistanceBin, Double> truckLoadsByDistanceAndCommodity = HashBasedTable.create();

    private HashBasedTable<Commodity, DistanceBin, Double> emptyTrucksProportionByDistanceAndCommodity = HashBasedTable.create();

    private Map<Id, Integer> observedCounts = new HashMap<>();

    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public Map<Integer, Zone> getInternalAndExternalZonesOnly(){
        Map<Integer, Zone> zonesSubset = new HashMap<>();
        for (Zone zone :  zones.values()){
            if (!zone.getClass().equals(InternalMicroZone.class)){
                zonesSubset.put(zone.getId(), zone);
            }
        }
        return zonesSubset;
    }

    public HashBasedTable<Integer, Integer, ArrayList<OrigDestFlow>> getFlowMatrix() {
        return flowMatrix;
    }

    public double getUncongestedTravelTime(int origin, int destination){
        try{
            return uncongestedTravelTimeMatrix.get(origin,destination);
        } catch (NullPointerException e){
            return 0;
        }

    }

    public HashBasedTable<Commodity, DistanceBin, Double> getTruckLoadsByDistanceAndCommodity() {
        return truckLoadsByDistanceAndCommodity;
    }

    public HashBasedTable<Commodity, DistanceBin, Double> getEmptyTrucksProportionsByDistanceAndCommodity() {
        return emptyTrucksProportionByDistanceAndCommodity;
    }

    public Map<Id, Integer> getObservedCounts() {
        return observedCounts;
    }


    public HashBasedTable<Integer, Integer, Double> getUncongestedTravelTimeMatrix() {
        return uncongestedTravelTimeMatrix;
    }
}

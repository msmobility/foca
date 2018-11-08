package de.tum.bgu.msm.freight.data;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.data.Zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FreightFlowsDataSet {

    private Map<Integer, Zone> zones = new HashMap<Integer, Zone>();

    private HashBasedTable<Integer, Integer, ArrayList<OrigDestFlow>> flowMatrix = HashBasedTable.create();

    private HashBasedTable<Commodity, DistanceBin, Double> truckLoadsByDistanceAndCommodity = HashBasedTable.create();

    private HashBasedTable<Commodity, DistanceBin, Double> emptyTrucksProportionByDistanceAndCommodity = HashBasedTable.create();

    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public HashBasedTable<Integer, Integer, ArrayList<OrigDestFlow>> getFlowMatrix() {
        return flowMatrix;
    }

    public HashBasedTable<Commodity, DistanceBin, Double> getTruckLoadsByDistanceAndCommodity() {
        return truckLoadsByDistanceAndCommodity;
    }

    public HashBasedTable<Commodity, DistanceBin, Double> getEmptyTruckProportionsByDistanceAndCommodity() {
        return emptyTrucksProportionByDistanceAndCommodity;
    }
}

package de.tum.bgu.msm.freight.data;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.Terminal;
import de.tum.bgu.msm.freight.data.geo.Zone;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Population;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataSet {

    private Map<Integer, Zone> zones = new HashMap<Integer, Zone>();

    private HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> flowMatrix = HashBasedTable.create();

    private HashBasedTable<Integer, Integer, Double> uncongestedTravelTimeMatrix = HashBasedTable.create();

    private HashBasedTable<Commodity, DistanceBin, Double> truckLoadsByDistanceAndCommodity = HashBasedTable.create();

    private HashBasedTable<Commodity, DistanceBin, Double> emptyTrucksProportionByDistanceAndCommodity = HashBasedTable.create();

    private Map<Id, Integer> observedCounts = new HashMap<>();

    private Map<Integer, Map<CommodityGroup, ArrayList<DistributionCenter>>> distributionCenters = new HashMap<>();

    private HashBasedTable<String, Commodity, Double> makeTable = HashBasedTable.create();

    private HashBasedTable<String, Commodity, Double> useTable = HashBasedTable.create();

    private Map<Integer, Terminal> terminals = new HashMap<>();

    private final ArrayList<FlowSegment> assignedFlowSegments = new ArrayList<>();

    private Population matsimPopulation;


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

    public HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> getFlowMatrix() {
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

    public ArrayList<DistributionCenter> getDistributionCenterForZoneAndCommodityGroup(int zoneId, CommodityGroup commodityGroup) {
            return distributionCenters.get(zoneId).get(commodityGroup);
    }

    public Map<Integer, Map<CommodityGroup, ArrayList<DistributionCenter>>> getDistributionCenters() {
        return distributionCenters;
    }

    public HashBasedTable<String, Commodity, Double> getMakeTable() {
        return makeTable;
    }

    public HashBasedTable<String, Commodity, Double> getUseTable() {
        return useTable;
    }

    public Map<Integer, Terminal> getTerminals() {
        return terminals;
    }

    public Population getMatsimPopulation() {
        return matsimPopulation;
    }

    public void setMatsimPopulation(Population matsimPopulation) {
        this.matsimPopulation = matsimPopulation;
    }

    public ArrayList<FlowSegment> getAssignedFlowSegments() {
        return assignedFlowSegments;
    }
}

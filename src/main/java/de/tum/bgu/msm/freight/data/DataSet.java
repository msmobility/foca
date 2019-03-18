package de.tum.bgu.msm.freight.data;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.Terminal;
import de.tum.bgu.msm.freight.data.geo.Zone;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Population;

import java.util.*;

public class DataSet {

    private final Map<Integer, Zone> zones = new HashMap<Integer, Zone>();

    private final HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> flowMatrix = HashBasedTable.create();

    private final HashBasedTable<Integer, Integer, Double> uncongestedTravelTimeMatrix = HashBasedTable.create();

    private final HashBasedTable<Commodity, DistanceBin, Double> truckLoadsByDistanceAndCommodity = HashBasedTable.create();

    private final HashBasedTable<Commodity, DistanceBin, Double> emptyTrucksProportionByDistanceAndCommodity = HashBasedTable.create();

    private Map<Id, Integer> observedCounts = new HashMap<>();

    private final Map<Integer, Map<CommodityGroup, ArrayList<DistributionCenter>>> distributionCenters = new HashMap<>();

    private final HashBasedTable<String, Commodity, Double> makeTable = HashBasedTable.create();

    private final HashBasedTable<String, Commodity, Double> useTable = HashBasedTable.create();

    private final Map<Integer, Terminal> terminals = new HashMap<>();

    private final List<FlowSegment> assignedFlowSegments = new ArrayList<>();

    private final Map<DistributionCenter, ArrayList<FlowSegment>> flowSegmentsDeliveredByParcel = new HashMap<>();

    private final Map<DistributionCenter, ArrayList<FlowSegment>> flowSegmentsDeliveredBySmallTrucks = new HashMap<>();

    private final List<Parcel> parcels = new ArrayList<>();

    private Population matsimPopulation;

    private final Map<Double,Double> parcelWeightDistribution = new TreeMap<>();

    private final List<LongDistanceTruckTrip> longDistanceTruckTrips = new ArrayList<>();

    private final List<ShortDistanceTruckTrip> shortDistanceTruckTrips = new ArrayList<>();


    //getters and setters

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

    public List<FlowSegment> getAssignedFlowSegments() {
        return assignedFlowSegments;
    }

    public Map<DistributionCenter, ArrayList<FlowSegment>> getFlowSegmentsDeliveredByParcel() {
        return flowSegmentsDeliveredByParcel;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }

    public Map<Double, Double> getParcelWeightDistribution() {
        return parcelWeightDistribution;
    }

    public Map<DistributionCenter, ArrayList<FlowSegment>> getFlowSegmentsDeliveredBySmallTrucks() {
        return flowSegmentsDeliveredBySmallTrucks;
    }

    public List<LongDistanceTruckTrip> getLongDistanceTruckTrips() {
        return longDistanceTruckTrips;
    }

    public List<ShortDistanceTruckTrip> getShortDistanceTruckTrips() {
        return shortDistanceTruckTrips;
    }
}

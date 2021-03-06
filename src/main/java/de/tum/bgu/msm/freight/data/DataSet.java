package de.tum.bgu.msm.freight.data;

import com.google.common.collect.HashBasedTable;
import de.tum.bgu.msm.freight.data.freight.*;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowOriginToDestination;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDTruckTrip;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.SDTruckTrip;
import de.tum.bgu.msm.freight.data.geo.*;
import de.tum.bgu.msm.schools.DataContainerWithSchools;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.freight.carrier.Carrier;

import java.util.*;

public class DataSet {

    private final Map<Integer, Zone> zones = new HashMap<Integer, Zone>();

    private final HashBasedTable<Integer, Integer, Map<Integer, FlowOriginToDestination>> flowMatrix = HashBasedTable.create();

    private final HashBasedTable<Integer, Integer, Double> uncongestedTravelTimeMatrix = HashBasedTable.create();

    private final HashBasedTable<Commodity, DistanceBin, Double> truckLoadsByDistanceAndCommodity = HashBasedTable.create();

    private final HashBasedTable<Commodity, DistanceBin, Double> emptyTrucksProportionByDistanceAndCommodity = HashBasedTable.create();

    private Map<Id, Integer> observedCounts = new HashMap<>();

    private final Map<Integer, Map<CommodityGroup, Map<Integer, DistributionCenter>>> distributionCenters = new LinkedHashMap<>();

    private final HashBasedTable<String, Commodity, Double> makeTable = HashBasedTable.create();

    private final HashBasedTable<String, Commodity, Double> useTable = HashBasedTable.create();

    private final Map<Integer, Terminal> terminals = new LinkedHashMap<>();

    private final List<FlowSegment> assignedFlowSegments = new ArrayList<>();

    private final HashBasedTable<DistributionCenter, Commodity, Map<Bound,Double>> volByCommodityDistributionCenterAndBoundBySmallTrucks = HashBasedTable.create();

    private final HashBasedTable<DistributionCenter, Commodity, Map<Bound,Double>> volByCommodityDistributionCenterAndBoundByParcels = HashBasedTable.create();

    private final Map<DistributionCenter, List<Parcel>> parcelsByDistributionCenter = new LinkedHashMap<>();

    private Population matsimPopulation;

    private final Map<Double,Double> parcelWeightDistribution = new TreeMap<>();

    private final List<LDTruckTrip> LDTruckTrips = new ArrayList<>();

    private final List<SDTruckTrip> SDTruckTrips = new ArrayList<>();

    private DataContainerWithSchools siloDataContainer;

    private Map<Carrier, String> modeByCarrier;

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

    public Map<Integer, DistributionCenter> getDistributionCentersForZoneAndCommodityGroup(int zoneId, CommodityGroup commodityGroup) {
            return distributionCenters.get(zoneId).get(commodityGroup);
    }

    public Map<Integer, Map<CommodityGroup, Map<Integer, DistributionCenter>>> getDistributionCenters() {
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

    public HashBasedTable<DistributionCenter, Commodity, Map<Bound,Double>>  getVolByCommodityDistributionCenterAndBoundBySmallTrucks() {
        return volByCommodityDistributionCenterAndBoundBySmallTrucks;
    }

    public Map<DistributionCenter, List<Parcel>> getParcelsByDistributionCenter() {
        return parcelsByDistributionCenter;
    }

    public Map<Double, Double> getParcelWeightDistribution() {
        return parcelWeightDistribution;
    }

    public  HashBasedTable<DistributionCenter, Commodity, Map<Bound,Double>> getVolByCommodityDistributionCenterAndBoundByParcels() {
        return volByCommodityDistributionCenterAndBoundByParcels;
    }

    public List<LDTruckTrip> getLDTruckTrips() {
        return LDTruckTrips;
    }

    public List<SDTruckTrip> getSDTruckTrips() {
        return SDTruckTrips;
    }

    public double getWeightDistributionInterval() {
        Set<Double> keys = this.parcelWeightDistribution.keySet();
       return (keys.stream().max(Double::compareTo).get() - keys.stream().min(Double::compareTo).get())/ (keys.size() - 1);
    }
    public void setSiloDataContainer(DataContainerWithSchools dataContainer) {
        this.siloDataContainer = dataContainer;
    }

    public void setModeByCarrier(Map<Carrier, String> modeByCarrier) {
        this.modeByCarrier = modeByCarrier;
    }

    public Map<Carrier, String> getModeByCarrier() {
        return modeByCarrier;
    }
}

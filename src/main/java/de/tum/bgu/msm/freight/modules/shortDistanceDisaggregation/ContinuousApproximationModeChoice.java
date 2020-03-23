package de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDDistributionType;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelDistributionType;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import scala.xml.Null;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class ContinuousApproximationModeChoice implements ModeChoiceModel {

    private Logger logger = Logger.getLogger(ContinuousApproximationModeChoice.class);
    private DataSet dataSet;
    private Properties properties;

    private PrintWriter printWriter;

    private double operatingCostBike_eur_km = 0.9200;
    private double operatingCostTruck_eur_km = 1.7765;
    private double kApproximation = 1.5;
    private double serviceCostBike_eur_parcel = 1.0152;
    private double serviceCostTruck_eur_parcel = 1.2585;
    private double extraHandlingBike_eur_l = 0.84;
    private double capacityTruck = 120;
    private double capacityFeeder = capacityTruck;


    private Map<Integer, Map<LoadClass, ParcelDistributionType>> modeByClassAndZone = new HashMap<>();

    enum LoadClass {
        XS(1., 0.1), S(2., 1.), M(5., 10), L(100., 20.);
        private double weightUpperThreshold_kg;
        private double volume_l;

        LoadClass(double weight, double volume_l) {
            this.weightUpperThreshold_kg = weight;
            this.volume_l = volume_l;
        }
    }

    @Override
    public double getShareOfCargoBikesAtThisMicroZone(int microzoneId, double weight) {
        return 0;
    }

    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        try {
            this.printWriter = new PrintWriter(new File("./output/" + properties.getRunId() + "/analyticalModeChoice.csv"));
            printWriter.println("zone,microZone,dc,distanceToDc,size,area,density,combination,mode," +
                    "costs_lh,costs_service,cost_extra,costs_routing_bike,costs_routing_truck,costs_all");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        chooseZonalModes();
        assignModes();

    }

    private void chooseZonalModes() {

        for (int zoneId : properties.getAnalysisZones()) {
            InternalZone zone = (InternalZone) dataSet.getZones().get(zoneId);
            for (DistributionCenter distributionCenter : dataSet.getDistributionCentersForZoneAndCommodityGroup(zoneId, CommodityGroup.PACKET).values()) {
                Map<Integer, Map<LoadClass, Integer>> parcelsByZoneAndLoadClass = new HashMap<>();
                for (Parcel parcel : dataSet.getParcelsByDistributionCenter().get(distributionCenter)) {
                    LoadClass loadClass = getLoadClassFromWeight(parcel.getWeight_kg());
                    int microZoneid = parcel.getDestMicroZoneId();
                    parcelsByZoneAndLoadClass.putIfAbsent(microZoneid, new HashMap<>());
                    parcelsByZoneAndLoadClass.get(microZoneid).putIfAbsent(loadClass, 0);
                    parcelsByZoneAndLoadClass.get(microZoneid).put(loadClass, parcelsByZoneAndLoadClass.get(microZoneid).get(loadClass) + 1);
                }

                for (InternalMicroZone internalMicroZone : distributionCenter.getZonesServedByThis()) {

                    Coordinate zoneCoordinates = internalMicroZone.getCoordinates(properties.getRand());
                    Coordinate dcCoordinate = distributionCenter.getCoordinates();

                    double distanceToDc_km = Math.abs(zoneCoordinates.getX() - dcCoordinate.getX())/1000d +
                            Math.abs(zoneCoordinates.getY() - dcCoordinate.getY())/1000d;

                    modeByClassAndZone.putIfAbsent(internalMicroZone.getId(), new HashMap<>());
                    double area_km2 = internalMicroZone.getArea_m2() / 1e6;

                    Map<LoadClass, Double> longHaulCostsTruck = new HashMap<>();
                    Map<LoadClass, Double> longHaulCostsBike = new HashMap<>();
                    Map<LoadClass, Double> extraHandlingCostsBike = new HashMap<>();
                    Map<LoadClass, Double> serviceCostTruck = new HashMap<>();
                    Map<LoadClass, Double> serviceCostBike = new HashMap<>();
                    Map<LoadClass, Double> densities = new HashMap<>();

                    for (LoadClass loadClass : LoadClass.values()) {
                        //calculate costs by truck and by cargo bike and by size
                        int parcels;
                        try{
                            parcels = parcelsByZoneAndLoadClass.get(internalMicroZone.getId()).get(loadClass);
                        } catch (NullPointerException e){
                            parcels = 0;
                        }

                        double density = parcels / area_km2;

                        densities.put(loadClass, density);

                        //by cargo bike
                        longHaulCostsBike.put(loadClass, area_km2 * loadClass.volume_l * density *
                                2d * distanceToDc_km * operatingCostTruck_eur_km / capacityFeeder);

                        extraHandlingCostsBike.put(loadClass, area_km2 * loadClass.volume_l * density *
                                extraHandlingBike_eur_l);

                        serviceCostBike.put(loadClass, area_km2 * density * serviceCostBike_eur_parcel);

                        //by truck
                        longHaulCostsTruck.put(loadClass, area_km2 * loadClass.volume_l * density *
                                2d * distanceToDc_km * operatingCostTruck_eur_km / capacityTruck);

                        serviceCostTruck.put(loadClass, area_km2 * density * serviceCostTruck_eur_parcel);
                    }

                    double minCost = Double.MAX_VALUE;
                    int selectedCombinationIndex = 0;
                    Map<Integer,EnumMap<LoadClass, Double>> combinations = generateCombinations();
                    for (int combinationIndex : combinations.keySet()){
                        double cost = 0d;
                        double sumOfDensitiesBike = 0d;
                        double sumOfDensitiesTruck = 0d;
                        EnumMap<LoadClass, Double> thisCombination = combinations.get(combinationIndex);
                        for (LoadClass loadClass : LoadClass.values()){
                            double isCargoBike = thisCombination.get(loadClass);
                            cost += longHaulCostsBike.get(loadClass) * isCargoBike;
                            cost += longHaulCostsTruck.get(loadClass) *  (1d - isCargoBike);
                            cost += extraHandlingCostsBike.get(loadClass) * isCargoBike;
                            cost += serviceCostBike.get(loadClass) * isCargoBike;
                            cost += serviceCostTruck.get(loadClass) * (1d- isCargoBike);
                            sumOfDensitiesBike += densities.get(loadClass) * isCargoBike;
                            sumOfDensitiesTruck += densities.get(loadClass) * (1d- isCargoBike);

                            printWriter.print(zone.getId());
                            printWriter.print(",");
                            printWriter.print(internalMicroZone.getId());
                            printWriter.print(",");
                            printWriter.print(distributionCenter.getId());
                            printWriter.print(",");
                            printWriter.print(distanceToDc_km);
                            printWriter.print(",");
                            printWriter.print(loadClass.toString());
                            printWriter.print(",");
                            printWriter.print(area_km2);
                            printWriter.print(",");
                            printWriter.print(densities.get(loadClass));
                            printWriter.print(",");
                            printWriter.print(combinationIndex);
                            printWriter.print(",");
                            printWriter.print(isCargoBike == 1? "cargoBike" : "truck");
                            printWriter.print(",");
                            printWriter.print(longHaulCostsBike.get(loadClass) * isCargoBike + longHaulCostsTruck.get(loadClass) *  (1d - isCargoBike));
                            printWriter.print(",");
                            printWriter.print(serviceCostBike.get(loadClass) * isCargoBike + serviceCostTruck.get(loadClass) *  (1d - isCargoBike));
                            printWriter.print(",");
                            printWriter.print(extraHandlingCostsBike.get(loadClass) * isCargoBike);
                            printWriter.print(",");
                            printWriter.print(0);
                            printWriter.print(",");
                            printWriter.print(0);
                            printWriter.print(",");
                            printWriter.print(0);
                            printWriter.println();
                        }

                        cost = cost +
                                Math.sqrt(sumOfDensitiesBike) * kApproximation * operatingCostBike_eur_km * area_km2 +
                                Math.sqrt(sumOfDensitiesTruck) * kApproximation * operatingCostTruck_eur_km * area_km2;

                        printWriter.print(zone.getId());
                        printWriter.print(",");
                        printWriter.print(internalMicroZone.getId());
                        printWriter.print(",");
                        printWriter.print(distributionCenter.getId());
                        printWriter.print(",");
                        printWriter.print(distanceToDc_km);
                        printWriter.print(",");
                        printWriter.print("all");
                        printWriter.print(",");
                        printWriter.print(area_km2);
                        printWriter.print(",");
                        printWriter.print(0);
                        printWriter.print(",");
                        printWriter.print(combinationIndex);
                        printWriter.print(",");
                        printWriter.print("all");
                        printWriter.print(",");
                        printWriter.print(0);
                        printWriter.print(",");
                        printWriter.print(0);
                        printWriter.print(",");
                        printWriter.print(0);
                        printWriter.print(",");
                        printWriter.print(Math.sqrt(sumOfDensitiesBike) * kApproximation * operatingCostBike_eur_km * area_km2);
                        printWriter.print(",");
                        printWriter.print(Math.sqrt(sumOfDensitiesTruck) * kApproximation * operatingCostTruck_eur_km * area_km2);
                        printWriter.print(",");
                        printWriter.print(cost);
                        printWriter.println();


                        if (cost < minCost){
                            minCost = cost;
                            selectedCombinationIndex = combinationIndex;
                        }
                    }

                    EnumMap<LoadClass, Double> selectedCombination = combinations.get(selectedCombinationIndex);
                    for (LoadClass loadClass :selectedCombination.keySet()){
                        ParcelDistributionType parcelDistributionType = selectedCombination.get(loadClass) == 0. ?
                                ParcelDistributionType.MOTORIZED : ParcelDistributionType.CARGO_BIKE;

                        modeByClassAndZone.get(internalMicroZone.getId()).put(loadClass, parcelDistributionType);

                    }


                }

            }


        }

        printWriter.close();

    }

    private void assignModes() {
    }


    private LoadClass getLoadClassFromWeight(double weight) {
        if (weight < LoadClass.XS.weightUpperThreshold_kg) {
            return LoadClass.XS;
        } else if (weight < LoadClass.S.weightUpperThreshold_kg) {
            return LoadClass.S;
        } else if (weight < LoadClass.M.weightUpperThreshold_kg) {
            return LoadClass.M;
        } else {
            return LoadClass.L;
        }
    }

    private Map<Integer,EnumMap<LoadClass, Double>> generateCombinations(){
        Map<Integer,EnumMap<LoadClass, Double>> combinations = new HashMap<>();
        int seq = 0;
        EnumMap<LoadClass, Double> combination;
        combination = new EnumMap<>(LoadClass.class);
        combination.put(LoadClass.XS, 0d);
        combination.put(LoadClass.S, 0d);
        combination.put(LoadClass.M, 0d );
        combination.put(LoadClass.L, 0d);
        combinations.put(seq, combination);
        seq++;

        combination = new EnumMap<>(LoadClass.class);
        combination.put(LoadClass.XS, 1d);
        combination.put(LoadClass.S, 0d);
        combination.put(LoadClass.M, 0d);
        combination.put(LoadClass.L, 0d);
        combinations.put(seq, combination);
        seq++;

        combination = new EnumMap<>(LoadClass.class);
        combination.put(LoadClass.XS, 1d);
        combination.put(LoadClass.S, 1d);
        combination.put(LoadClass.M, 0d);
        combination.put(LoadClass.L, 0d);
        combinations.put(seq, combination);
        seq++;

        combination = new EnumMap<>(LoadClass.class);
        combination.put(LoadClass.XS, 1d);
        combination.put(LoadClass.S, 1d);
        combination.put(LoadClass.M, 1d);
        combination.put(LoadClass.L, 0d);
        combinations.put(seq, combination);
        seq++;

        combination = new EnumMap<>(LoadClass.class);
        combination.put(LoadClass.XS, 1d);
        combination.put(LoadClass.S, 1d);
        combination.put(LoadClass.M, 1d);
        combination.put(LoadClass.L, 1d);
        combinations.put(seq, combination);
        seq++;

        return combinations;

    }


}

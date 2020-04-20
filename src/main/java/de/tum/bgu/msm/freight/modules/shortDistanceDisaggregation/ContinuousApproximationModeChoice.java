package de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.CommodityGroup;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelDistributionType;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelTransaction;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.InternalMicroZone;
import de.tum.bgu.msm.freight.data.geo.InternalZone;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.opengis.geometry.BoundingBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class ContinuousApproximationModeChoice implements ModeChoiceModel {

    private final Logger logger = Logger.getLogger(ContinuousApproximationModeChoice.class);
    private DataSet dataSet;
    private Properties properties;

    private PrintWriter printWriter;


    private final Map<Integer, Integer> microZoneToAnalysisZoneMap = new HashMap<>();
    private final Map<Integer, Map<LoadClass, ParcelDistributionType>> modeByClassAndZone = new HashMap<>();

    enum LoadClass {
        XS(1., 0.005), S(2., 0.01), M(5., 0.05), L(100., 0.2);
        private double weightUpperThreshold_kg;
        private double volume_m3;

        LoadClass(double weight, double volume_m3) {
            this.weightUpperThreshold_kg = weight;
            this.volume_m3 = volume_m3;
        }
    }

    public ParcelDistributionType getModeAtThisMicroZone(int microzoneId, double weight) {

        int analysisZone = microZoneToAnalysisZoneMap.get(microzoneId);
        return modeByClassAndZone.get(analysisZone).get(getLoadClassFromWeight(weight));
    }

    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.dataSet = dataSet;
        this.properties = properties;
        try {
            this.printWriter = new PrintWriter(new File("./output/" + properties.getRunId() + "/analyticalModeChoice.csv"));
            printWriter.println("zone,analysisZone,x,y,dc,distanceToDc,size,area,density,combination,mode," +
                    "costs_lh,costs_service,cost_extra,costs_routing_bike,costs_routing_truck,costs_all");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        chooseZonalModes();
        chooseParcelDistributionType();
        summarizeModeChoice();

    }

    private void summarizeModeChoice() {
        int countVan = 0;
        int countBike = 0;
        int countNull = 0;
        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()) {
            for (Parcel parcel : dataSet.getParcelsByDistributionCenter().get(distributionCenter)) {
                if (parcel.getParcelDistributionType() == null) {
                    countNull++;
                } else if (parcel.getParcelDistributionType().equals(ParcelDistributionType.MOTORIZED)) {
                    countVan++;
                } else if (parcel.getParcelDistributionType().equals(ParcelDistributionType.CARGO_BIKE)) {
                    countBike++;
                } else {
                    logger.error("another parcel distribution type");
                }
            }
        }

        logger.info("Modal share van = " + countVan);
        logger.info("Modal share bike = " + countBike);
        logger.info("Cases with null = " + countNull);
    }

    class AnalysisZone {
        int id;
        double xCenter;
        double yCenter;

        public AnalysisZone(int id, double xCenter, double yCenter) {
            this.id = id;
            this.xCenter = xCenter;
            this.yCenter = yCenter;
        }
    }


    private void chooseZonalModes() {

        for (int zoneId : properties.getAnalysisZones()) {
            InternalZone zone = (InternalZone) dataSet.getZones().get(zoneId);

            Map<Integer, AnalysisZone> analysisZones = new HashMap<>();
            //create raster analysis cells
            BoundingBox bounds = zone.getShapeFeature().getBounds();
            double xMin = bounds.getMinX();
            double yMin = bounds.getMinY();
            double xMax = bounds.getMaxX();
            double yMax = bounds.getMaxY();

            int counter = 0;

            double gridSpacing = properties.modeChoice().getGridSpacing();
            double y = yMin + gridSpacing / 2;
            while (y < yMax) {
                double x = xMin + gridSpacing / 2;
                while (x < xMax) {
                    analysisZones.put(counter, new AnalysisZone(counter, x, y));
                    counter++;
                    x += gridSpacing;
                }
                y += gridSpacing;
            }
            logger.warn("Problem reduced to " + counter + " analysis zones");

            for (DistributionCenter distributionCenter : dataSet.getDistributionCentersForZoneAndCommodityGroup(zoneId, CommodityGroup.PACKET).values()) {
                Map<Integer, Map<LoadClass, Integer>> parcelsByZoneAndLoadClass = new HashMap<>();
                if (dataSet.getParcelsByDistributionCenter().get(distributionCenter) != null) {
                    for (Parcel parcel : dataSet.getParcelsByDistributionCenter().get(distributionCenter)) {
                        LoadClass loadClass = getLoadClassFromWeight(parcel.getWeight_kg());
                        Coordinate coordinate;
                        if (!parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                            if (parcel.isToDestination()) {
                                coordinate = parcel.getDestCoord();
                            } else {
                                coordinate = parcel.getOriginCoord();
                            }

                            //int microZoneid = parcel.getDestMicroZoneId();
                            //finds the closest analysis zone
                            int selecteAZ = -1;
                            double distance = Double.MAX_VALUE;
                            for (AnalysisZone az : analysisZones.values()) {
                                double thisDistance = Math.sqrt(Math.pow(az.xCenter - coordinate.getX(), 2) + Math.pow(az.yCenter - coordinate.getY(), 2));
                                if (thisDistance < distance) {
                                    distance = thisDistance;
                                    selecteAZ = az.id;
                                }
                            }

                            if (selecteAZ != -1) {
                                microZoneToAnalysisZoneMap.put(parcel.getDestMicroZoneId(), selecteAZ);
                                parcelsByZoneAndLoadClass.putIfAbsent(selecteAZ, new HashMap<>());
                                parcelsByZoneAndLoadClass.get(selecteAZ).putIfAbsent(loadClass, 0);
                                parcelsByZoneAndLoadClass.get(selecteAZ).put(loadClass, parcelsByZoneAndLoadClass.get(selecteAZ).get(loadClass) + 1);
                            } else {
                                logger.warn("The closest zone is not found!");
                            }
                        }
                    }

                    for (int analysisZoneId : parcelsByZoneAndLoadClass.keySet()) {
                        AnalysisZone analysisZone = analysisZones.get(analysisZoneId);
                        Coordinate zoneCoordinates = new Coordinate(analysisZone.xCenter, analysisZone.yCenter);
                        Coordinate dcCoordinate = distributionCenter.getCoordinates();
                        double distanceToDc_km = Math.abs(zoneCoordinates.getX() - dcCoordinate.getX()) / 1000d +
                                Math.abs(zoneCoordinates.getY() - dcCoordinate.getY()) / 1000d;

                        modeByClassAndZone.putIfAbsent(analysisZone.id, new HashMap<>());
                        double area_km2 = Math.pow(gridSpacing, 2) / 1e6;

                        Map<LoadClass, Double> longHaulCostsTruck = new HashMap<>();
                        Map<LoadClass, Double> longHaulCostsBike = new HashMap<>();
                        Map<LoadClass, Double> extraHandlingCostsBike = new HashMap<>();
                        Map<LoadClass, Double> serviceCostTruck = new HashMap<>();
                        Map<LoadClass, Double> serviceCostBike = new HashMap<>();
                        Map<LoadClass, Double> densities = new HashMap<>();

                        double operatingCostTruck_eur_km = properties.modeChoice().getOperatingCostTruck_eur_km();
                        for (LoadClass loadClass : LoadClass.values()) {
                            //calculate costs by truck and by cargo bike and by size
                            int parcels;
                            try {
                                parcels = parcelsByZoneAndLoadClass.get(analysisZone.id).get(loadClass);
                            } catch (NullPointerException e) {
                                parcels = 0;
                            }

                            double density = parcels / area_km2;

                            densities.put(loadClass, density);

                            //by cargo bike
                            double capacityFeeder_m3 = properties.modeChoice().getCapacityFeeder_m3();
                            longHaulCostsBike.put(loadClass, area_km2 * loadClass.volume_m3 * density *
                                    2d * distanceToDc_km * operatingCostTruck_eur_km / capacityFeeder_m3);

                            double extraHandlingBike_eur_m3 = properties.modeChoice().getExtraHandlingBike_eur_m3();
                            extraHandlingCostsBike.put(loadClass, area_km2 * loadClass.volume_m3 * density *
                                    extraHandlingBike_eur_m3);

                            double serviceCostBike_eur_parcel = properties.modeChoice().getServiceCostBike_eur_parcel();
                            serviceCostBike.put(loadClass, area_km2 * density * serviceCostBike_eur_parcel);

                            //by truck
                            double capacityTruck_m3 = properties.modeChoice().getCapacityTruck_m3();
                            longHaulCostsTruck.put(loadClass, area_km2 * loadClass.volume_m3 * density *
                                    2d * distanceToDc_km * operatingCostTruck_eur_km / capacityTruck_m3);

                            double serviceCostTruck_eur_parcel = properties.modeChoice().getServiceCostTruck_eur_parcel();
                            serviceCostTruck.put(loadClass, area_km2 * density * serviceCostTruck_eur_parcel);
                        }

                        double minCost = Double.MAX_VALUE;
                        int selectedCombinationIndex = 0;
                        Map<Integer, EnumMap<LoadClass, Double>> combinations = generateCombinations();
                        for (int combinationIndex : combinations.keySet()) {
                            double cost = 0d;
                            double sumOfDensitiesBike = 0d;
                            double sumOfDensitiesTruck = 0d;
                            EnumMap<LoadClass, Double> thisCombination = combinations.get(combinationIndex);
                            for (LoadClass loadClass : LoadClass.values()) {
                                double isCargoBike = thisCombination.get(loadClass);
                                cost += longHaulCostsBike.get(loadClass) * isCargoBike;
                                cost += longHaulCostsTruck.get(loadClass) * (1d - isCargoBike);
                                cost += extraHandlingCostsBike.get(loadClass) * isCargoBike;
                                cost += serviceCostBike.get(loadClass) * isCargoBike;
                                cost += serviceCostTruck.get(loadClass) * (1d - isCargoBike);
                                sumOfDensitiesBike += densities.get(loadClass) * isCargoBike;
                                sumOfDensitiesTruck += densities.get(loadClass) * (1d - isCargoBike);

                                printWriter.print(zone.getId());
                                printWriter.print(",");
                                printWriter.print(analysisZone.id);
                                printWriter.print(",");
                                printWriter.print(analysisZone.xCenter);
                                printWriter.print(",");
                                printWriter.print(analysisZone.yCenter);
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
                                printWriter.print(isCargoBike == 1 ? "cargoBike" : "truck");
                                printWriter.print(",");
                                printWriter.print(longHaulCostsBike.get(loadClass) * isCargoBike + longHaulCostsTruck.get(loadClass) * (1d - isCargoBike));
                                printWriter.print(",");
                                printWriter.print(serviceCostBike.get(loadClass) * isCargoBike + serviceCostTruck.get(loadClass) * (1d - isCargoBike));
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

                            double operatingCostBike_eur_km = properties.modeChoice().getOperatingCostBike_eur_km();
                            double kApproximation = properties.modeChoice().getkApproximation();
                            cost = cost +
                                    Math.sqrt(sumOfDensitiesBike) * kApproximation * operatingCostBike_eur_km * area_km2 +
                                    Math.sqrt(sumOfDensitiesTruck) * kApproximation * operatingCostTruck_eur_km * area_km2;

                            printWriter.print(zone.getId());
                            printWriter.print(",");
                            printWriter.print(analysisZone.id);
                            printWriter.print(",");
                            printWriter.print(analysisZone.xCenter);
                            printWriter.print(",");
                            printWriter.print(analysisZone.yCenter);
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


                            if (cost < minCost) {
                                minCost = cost;
                                selectedCombinationIndex = combinationIndex;
                            }
                        }

                        EnumMap<LoadClass, Double> selectedCombination = combinations.get(selectedCombinationIndex);
                        for (LoadClass loadClass : selectedCombination.keySet()) {
                            ParcelDistributionType parcelDistributionType = selectedCombination.get(loadClass) == 0. ?
                                    ParcelDistributionType.MOTORIZED : ParcelDistributionType.CARGO_BIKE;

                            modeByClassAndZone.get(analysisZone.id).put(loadClass, parcelDistributionType);

                        }
                    }
                }
            }
        }

        printWriter.close();

    }

    private void chooseParcelDistributionType() {

        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()) {
            List<Integer> internalZonesServedByMicroDepots = new ArrayList<>();
            for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {
                for (InternalMicroZone internalMicroZone : microDepot.getZonesServedByThis()) {
                    internalZonesServedByMicroDepots.add(internalMicroZone.getId());
                }
            }

            for (Parcel parcel : dataSet.getParcelsByDistributionCenter().get(distributionCenter)) {

                if (!parcel.isToDestination()) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }

                if (parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }

                if (!internalZonesServedByMicroDepots.contains(parcel.getDestMicroZoneId())) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }
                double maxWeightForCargoBike_kg = properties.modeChoice().getMaxWeightForCargoBike_kg();
                if (parcel.getWeight_kg() > maxWeightForCargoBike_kg) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }

                if (parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
                    continue;
                }
                ParcelDistributionType modeAtThisMicroZone = getModeAtThisMicroZone(parcel.getDestMicroZoneId(), parcel.getWeight_kg());
                parcel.setParcelDistributionType(modeAtThisMicroZone);
                if (modeAtThisMicroZone.equals(ParcelDistributionType.CARGO_BIKE)) {
                    here:
                    for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {
                        InternalZone internalZone = (InternalZone) dataSet.getZones().get(distributionCenter.getZoneId());
                        if (microDepot.getZonesServedByThis().contains(internalZone.getMicroZones().get(parcel.getDestMicroZoneId()))) {
                            parcel.setMicroDepot(microDepot);
                            break here;
                        }
                    }
                }


            }
        }
        logger.info("Finished mode choice assignment");
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

    private Map<Integer, EnumMap<LoadClass, Double>> generateCombinations() {
        Map<Integer, EnumMap<LoadClass, Double>> combinations = new HashMap<>();
        int seq = 0;
        EnumMap<LoadClass, Double> combination;
        combination = new EnumMap<>(LoadClass.class);
        combination.put(LoadClass.XS, 0d);
        combination.put(LoadClass.S, 0d);
        combination.put(LoadClass.M, 0d);
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

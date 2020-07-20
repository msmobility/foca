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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.core.utils.gis.PointFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import scala.Int;
import sun.java2d.pipe.SpanShapeRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class ContinuousApproximationModeChoice implements ModeChoiceModel {

    private final Logger logger = Logger.getLogger(ContinuousApproximationModeChoice.class);
    private DataSet dataSet;
    private Properties properties;

    private PrintWriter printWriter;
    private PrintWriter printWriterSolution;

    private double scaleFactor = 1.;


    private final Map<Integer, Integer> microZoneToAnalysisZoneMap = new HashMap<>();
    private final Map<Integer, Map<LoadClass, ParcelDistributionType>> modeByClassAndZone = new HashMap<>();

    enum LoadClass {
        //        XS(4.1, 0.0175), S(9.3, 0.035), M(17, 0.14), L(1000., 0.28);
        XS(4.1, 0.5), S(9.3, 1), M(17, 4), L(1000., 8);
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
            this.printWriterSolution = new PrintWriter(new File("./output/" + properties.getRunId() + "/analyticalModeChoiceSolution.csv"));
            printWriter.println("zone,analysisZone,x,y,dc,distanceToDc,size,area,density,combination,mode," +
                    "costs_lh,costs_service,cost_extra,costs_routing_bike,costs_routing_truck,costs_all");
            printWriterSolution.println("zone,analysisZone,dc,solution");
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
                if (parcel.getParcelDistributionType() == null && !parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    countNull++;
                } else if (parcel.getParcelDistributionType().equals(ParcelDistributionType.MOTORIZED) && !parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    countVan++;
                } else if (parcel.getParcelDistributionType().equals(ParcelDistributionType.CARGO_BIKE) && !parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    countBike++;
                } else {
                    //logger.error("another parcel distribution type");
                }
            }
        }

        logger.info("Modal share van = " + countVan * scaleFactor);
        logger.info("Modal share bike = " + countBike * scaleFactor);
        logger.info("Cases with null = " + countNull * scaleFactor);
    }

    class AnalysisZone {
        int id;
        double congestion;
        double area;
        SimpleFeature feature;
        double xCenter;
        double yCenter;

        public AnalysisZone(int id, double congestion, double area, SimpleFeature feature) {
            this.id = id;
            this.congestion = congestion;
            this.area = area;
            this.feature = feature;
            Coordinate centroidCoordinates = ((Geometry) this.feature.getDefaultGeometry()).getCentroid().getCoordinate();
            this.xCenter = centroidCoordinates.getX();
            this.yCenter = centroidCoordinates.getY();
        }
    }


    private void chooseZonalModes() {

        for (int zoneId : properties.getAnalysisZones()) {
            InternalZone zone = (InternalZone) dataSet.getZones().get(zoneId);

            Map<Integer, AnalysisZone> analysisZones = new HashMap<>();
            //create raster analysis cells
//            BoundingBox bounds = zone.getShapeFeature().getBounds();
//            double xMin = bounds.getMinX();
//            double yMin = bounds.getMinY();
//            double xMax = bounds.getMaxX();
//            double yMax = bounds.getMaxY();

            int counter = 0;

//            double gridSpacing = properties.modeChoice().getGridSpacing();
//            double y = yMin + gridSpacing / 2;
//            while (y < yMax) {
//                double x = xMin + gridSpacing / 2;
//                while (x < xMax) {
//                    analysisZones.put(counter, new AnalysisZone(counter, x, y));
//                    counter++;
//                    x += gridSpacing;
//                }
//                y += gridSpacing;
//            }

            Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures(properties.modeChoice().getZoneSystemFile());

            for (SimpleFeature feature : features) {
                int id = Integer.parseInt(feature.getAttribute("id").toString());
                double congestionFactor = Double.parseDouble(feature.getAttribute("congestion").toString());
                double area = Double.parseDouble(feature.getAttribute("area").toString());
                analysisZones.put(id, new AnalysisZone(id, congestionFactor, area, feature));
                counter++;
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

                            //finds the closest analysis zone
                            int selectedAZ = -1;
                            PointFeatureFactory factory = new PointFeatureFactory.Builder().create();
                            for (AnalysisZone az : analysisZones.values()) {
                                if (((Geometry) az.feature.getDefaultGeometry()).contains((Geometry) factory.createPoint(coordinate).getDefaultGeometry())) {
                                    selectedAZ = az.id;
                                }
                            }

                            if (selectedAZ != -1) {
                                microZoneToAnalysisZoneMap.put(parcel.getDestMicroZoneId(), selectedAZ);
                                parcelsByZoneAndLoadClass.putIfAbsent(selectedAZ, new HashMap<>());
                                parcelsByZoneAndLoadClass.get(selectedAZ).putIfAbsent(loadClass, 0);
                                parcelsByZoneAndLoadClass.get(selectedAZ).put(loadClass, parcelsByZoneAndLoadClass.get(selectedAZ).get(loadClass) + 1);
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
                        double area_km2 = analysisZone.area;

                        Map<LoadClass, Double> longHaulCostsTruck = new HashMap<>();
                        Map<LoadClass, Double> longHaulCostsBike = new HashMap<>();
                        Map<LoadClass, Double> extraHandlingCostsBike = new HashMap<>();
                        Map<LoadClass, Double> serviceCostTruck = new HashMap<>();
                        Map<LoadClass, Double> serviceCostBike = new HashMap<>();
                        Map<LoadClass, Double> densities = new HashMap<>();

                        double operatingCostTruck_eur_km = properties.modeChoice().getOperatingCostTruck_eur_km();
                        double operatingCostFeeder_eur_km = properties.modeChoice().getOperatingCostFeeder_eur_km();
                        for (LoadClass loadClass : LoadClass.values()) {
                            //calculate costs by truck and by cargo bike and by size
                            int parcels;
                            try {
                                parcels = parcelsByZoneAndLoadClass.get(analysisZone.id).get(loadClass);
                            } catch (NullPointerException e) {
                                parcels = 0;
                            }

                            double density = parcels / area_km2 * scaleFactor;


                            densities.put(loadClass, density);

                            //by cargo bike
                            double capacityFeeder = properties.modeChoice().getCapacityFeeder_units();
                            longHaulCostsBike.put(loadClass, area_km2 * density *
                                    2d * distanceToDc_km * operatingCostFeeder_eur_km * loadClass.volume_m3 / capacityFeeder);

                            double extraHandlingBike_eur_unit = properties.modeChoice().getExtraHandlingBike_eur_unit();
                            extraHandlingCostsBike.put(loadClass, area_km2 * loadClass.volume_m3 * density *
                                    extraHandlingBike_eur_unit);

                            double serviceCostBike_eur_parcel = properties.modeChoice().getServiceCostBike_eur_parcel();
                            serviceCostBike.put(loadClass, area_km2 * density * serviceCostBike_eur_parcel);

                            //by truck
                            double capacityTruck = properties.modeChoice().getCapacityTruck_units();
                            longHaulCostsTruck.put(loadClass, area_km2 * density *
                                    2d * distanceToDc_km * operatingCostFeeder_eur_km * loadClass.volume_m3 / capacityTruck);

                            double serviceCostTruck_eur_parcel = properties.modeChoice().getServiceCostTruck_eur_parcel();
                            serviceCostTruck.put(loadClass, area_km2 * density * serviceCostTruck_eur_parcel * analysisZone.congestion);
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
                                cost += serviceCostBike.get(loadClass) * isCargoBike;
                                cost += extraHandlingCostsBike.get(loadClass) * isCargoBike;
                                cost += longHaulCostsTruck.get(loadClass) * (1d - isCargoBike);
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

                            //adds a penalty for cargo-bike if the density is too low, which happens in the boundaries of the region
                            double bike_penalty = 1.;

                            if (sumOfDensitiesBike + sumOfDensitiesTruck < 10) {
                                bike_penalty = 1000.;
                            }

                            cost = cost +
                                    Math.sqrt(sumOfDensitiesBike) * kApproximation * operatingCostBike_eur_km * area_km2 * bike_penalty +
                                    Math.sqrt(sumOfDensitiesTruck) * kApproximation * operatingCostTruck_eur_km * area_km2 * analysisZone.congestion;

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
                            printWriter.print(Math.sqrt(sumOfDensitiesBike) * kApproximation * operatingCostBike_eur_km * area_km2 * bike_penalty);
                            printWriter.print(",");
                            printWriter.print(Math.sqrt(sumOfDensitiesTruck) * kApproximation * operatingCostTruck_eur_km * area_km2 * analysisZone.congestion);
                            printWriter.print(",");
                            printWriter.print(cost);
                            if (cost < minCost) {
                                minCost = cost;
                                selectedCombinationIndex = combinationIndex;
                            } else {
                            }

                            printWriter.println();
                        }

                        EnumMap<LoadClass, Double> selectedCombination = combinations.get(selectedCombinationIndex);
                        for (LoadClass loadClass : selectedCombination.keySet()) {
                            ParcelDistributionType parcelDistributionType = selectedCombination.get(loadClass) == 0. ?
                                    ParcelDistributionType.MOTORIZED : ParcelDistributionType.CARGO_BIKE;
                            modeByClassAndZone.get(analysisZone.id).put(loadClass, parcelDistributionType);
                        }

                        printWriterSolution.print(zone.getId() + "," + analysisZoneId + "," + distributionCenter.getId() + "," + selectedCombinationIndex);
                        printWriterSolution.println();
                    }
                }
            }
        }

        printWriter.close();
        printWriterSolution.close();

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

//                if (!parcel.isToDestination()) {
//                    parcel.setParcelDistributionType(ParcelDistributionType.MOTORIZED);
//                    continue;
//                }

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

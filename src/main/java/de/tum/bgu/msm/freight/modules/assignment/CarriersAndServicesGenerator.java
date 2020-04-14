package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.FreightFlowUtils;
import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelDistributionType;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelTransaction;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.data.geo.ParcelShop;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates carriers and its services (equivalent to population and plans)
 */
public class CarriersAndServicesGenerator {

    private DataSet dataSet;
    private Network network;
    private Properties properties;
    private static final Logger logger = Logger.getLogger(CarriersAndServicesGenerator.class);
    /**
     * this maps keeps the parcels in micro depots to assign first the trips to micro depot
     * with motorized vehicles and later the cargo "TransportMode.bike" trips from the depot
     */
    private Map<MicroDepot, List<Parcel>> parcelsByMicrodepotScaled = new HashMap<>();
    private Map<ParcelShop, List<Parcel>> parcelsByShop;
    private final int fixDeliveryTime_s = 60;
    private final double parcelAccessSpeed_ms = 5 / 3.6;
    private final int MAX_NUMBER_PARCELS;

    private final Map<Carrier, String> modeByCarrier = new HashMap<>();

    public CarriersAndServicesGenerator(DataSet dataSet, Network network, Properties properties) {
        this.dataSet = dataSet;
        this.network = network;
        this.properties = properties;
        MAX_NUMBER_PARCELS = properties.shortDistance().getMaxParcelsByCarrier();
    }

    public void generateCarriers(Carriers carriers, CarrierVehicleTypes types) {
        AtomicInteger carrierCounter = new AtomicInteger(0);
        List<DistributionCenter> selectedDistributionCenters = new ArrayList<>();
        Map<Integer, DistributionCenter> allDistributionCenters = new HashMap<>();
        dataSet.getParcelsByDistributionCenter().keySet().forEach(x -> allDistributionCenters.put(x.getId(), x));
        if (properties.shortDistance().getSelectedDistributionCenters()[0] == -1) {
            //dist centers of interest are not defined
            selectedDistributionCenters.addAll(allDistributionCenters.values());
        } else {
            //some dist centers are defined
            for (int distCentId : properties.shortDistance().getSelectedDistributionCenters()) {
                selectedDistributionCenters.add(allDistributionCenters.get(distCentId));
            }
        }

        for (DistributionCenter distributionCenter : selectedDistributionCenters) {
            List<Parcel> parcelsInThisDistributionCenter = dataSet.getParcelsByDistributionCenter().get(distributionCenter);
            for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {
                //initialize
                parcelsByMicrodepotScaled.put(microDepot, new ArrayList<>());
            }
            parcelsByShop = new HashMap<>();
            for (ParcelShop parcelShop : distributionCenter.getParcelShopsServedByThis()) {
                //initialize
                parcelsByShop.put(parcelShop, new ArrayList<>());
            }

            int numberOfCarriers = (int) Math.floor(parcelsInThisDistributionCenter.size() / MAX_NUMBER_PARCELS * properties.getSampleFactorForParcels()) + 1;
            int numberOfParcelsByCarrier = parcelsInThisDistributionCenter.size() / numberOfCarriers;

            //split the problem of van deliveries to various carriers by limiting the number of services by carrier
            for (int carrierIndex = 0; carrierIndex < numberOfCarriers; carrierIndex++) {
                int firstParcel = carrierIndex * numberOfParcelsByCarrier;
                int lastParcel = (1 + carrierIndex) * numberOfParcelsByCarrier - 1;
                logger.info("Split distribution center into carrier " + carrierIndex + ": parcels " + firstParcel + " to " + lastParcel);
                List<Parcel> parcelsInThisCarrier = parcelsInThisDistributionCenter.subList(firstParcel, lastParcel);
                Carrier carrier = CarrierImpl.newInstance(Id.create(carrierCounter.getAndIncrement(), Carrier.class));
                modeByCarrier.put(carrier, TransportMode.truck);
                Coordinate coordinates = distributionCenter.getCoordinates();
                Link link = getNearestLinkByMode(coordinates, ParcelDistributionType.MOTORIZED);
                Id<Link> linkId = link.getId();
                VehicleType type = types.getVehicleTypes().get(Id.create("van", VehicleType.class));
                carrier.getCarrierCapabilities().getVehicleTypes().add(type);
                carrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
                CarrierVehicle vehicle = getGenericVehicle(type, carrier.getId(), linkId, 7 * 60 * 60, 17 * 60 * 60);
                //vehicle.getType().setNetworkMode(TransportMode.truck);
                carrier.getCarrierCapabilities().getCarrierVehicles().put(vehicle.getId(), vehicle);
                createDeliveriesByMotorizedModes(parcelsInThisCarrier, carrier);

                //add only if there are effective parcels to deliver
                if (!carrier.getServices().isEmpty()) {
                    carriers.addCarrier(carrier);
                } else {
                    carriers.getCarriers().remove(carrier.getId());
                }

            }

            //deliveries by cargo bikes
            //feeders
            Carrier feederCarrier = CarrierImpl.newInstance(Id.create(carrierCounter.getAndIncrement() + "_feeder", Carrier.class));
            carriers.addCarrier(feederCarrier);
            modeByCarrier.put(feederCarrier, TransportMode.truck);
            Coordinate coordinates = distributionCenter.getCoordinates();
            Link link = getNearestLinkByMode(coordinates, ParcelDistributionType.MOTORIZED);
            Id<Link> linkId = link.getId();
            VehicleType type = types.getVehicleTypes().get(Id.create("van", VehicleType.class));
            feederCarrier.getCarrierCapabilities().getVehicleTypes().add(type);
            feederCarrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
            CarrierVehicle vehicle = getGenericVehicle(type, feederCarrier.getId(), linkId, 7 * 60 * 60, 8 * 60 * 60);
            //vehicle.getType().setNetworkMode(TransportMode.truck); looks like it does not make anything
            feederCarrier.getCarrierCapabilities().getCarrierVehicles().put(vehicle.getId(), vehicle);

            for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {
                Coord destCoord = new Coord(microDepot.getCoord_gk4().x, microDepot.getCoord_gk4().y);
                TimeWindow timeWindow = generateRandomTimeSubWindow(7, 8, 1);
                int demandedCapacity = parcelsByMicrodepotScaled.get(microDepot).size();

                if (demandedCapacity > 0) {
                    double remainder = demandedCapacity;
                    int feederCounter = 0;
                    //add feeder trips to the carrier
                    while (remainder > 0) {
                        double current = Math.min(remainder, type.getCapacity().getOther());
                        remainder -= current;

                        double duration_s = Math.min(5 * demandedCapacity, 15 * 60);
                        Id<Link> linkParcelDelivery = NetworkUtils.getNearestLink(network, destCoord).getId();
                        CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create("to_micro_depot_" + microDepot.getId() + "_" + feederCounter, CarrierService.class), linkParcelDelivery);
                        serviceBuilder.setCapacityDemand((int) Math.round(current));
                        serviceBuilder.setServiceDuration(duration_s);
                        serviceBuilder.setServiceStartTimeWindow(timeWindow);
                        CarrierService carrierService = serviceBuilder.build();
                        feederCarrier.getServices().put(carrierService.getId(), carrierService);
                        feederCounter++;
                    }

                    //create a microdepot Carrier (sub-carrier) with only bicycles
                    Carrier microDepotCarrier = CarrierImpl.newInstance(Id.create(microDepot.getId() + "_microDepot", Carrier.class));
                    carriers.addCarrier(microDepotCarrier);
                    modeByCarrier.put(microDepotCarrier, TransportMode.bike);
                    Coordinate microDepotCoord = microDepot.getCoord_gk4();
                    Link microDepotLink = getNearestLinkByMode(microDepotCoord, ParcelDistributionType.MOTORIZED);
                    Id<Link> microDepotLinkId = microDepotLink.getId();
                    VehicleType cargoBikeType = types.getVehicleTypes().get(Id.create("cargoBike", VehicleType.class));
                    microDepotCarrier.getCarrierCapabilities().getVehicleTypes().add(cargoBikeType);
                    List<Parcel> parcelsInThisMicroDepot = parcelsByMicrodepotScaled.get(microDepot);
                    CarrierVehicle cargoBike = getGenericVehicle(cargoBikeType, microDepotCarrier.getId(),
                            microDepotLinkId, 8 * 60 * 60, 17 * 60 * 60);
                    //cargoBike.getType().setNetworkMode("TransportMode.bike");
                    microDepotCarrier.getCarrierCapabilities().getCarrierVehicles().put(cargoBike.getId(), cargoBike);
                    microDepotCarrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);

                    //deliver the parcels later with cargo bikes
                    createDeliveriesByCargoBikes(parcelsInThisMicroDepot, microDepotCarrier);
                }
            }

            Carrier feederCarrierParcelShop = CarrierImpl.newInstance(Id.create(carrierCounter.getAndIncrement() + "_toParcelShop", Carrier.class));
            carriers.addCarrier(feederCarrierParcelShop);
            modeByCarrier.put(feederCarrierParcelShop, TransportMode.truck);
            coordinates = distributionCenter.getCoordinates();
            link = getNearestLinkByMode(coordinates, ParcelDistributionType.MOTORIZED);
            linkId = link.getId();
            type = types.getVehicleTypes().get(Id.create("van", VehicleType.class));
            feederCarrierParcelShop.getCarrierCapabilities().getVehicleTypes().add(type);
            feederCarrierParcelShop.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
            vehicle = getGenericVehicle(type, feederCarrierParcelShop.getId(), linkId, 7 * 60 * 60, 8 * 60 * 60);
            //vehicle.getType().setNetworkMode(TransportMode.truck); looks like it does not make anything
            TimeWindow timeWindow = generateRandomTimeSubWindow(7, 8, 1);
            feederCarrierParcelShop.getCarrierCapabilities().getCarrierVehicles().put(vehicle.getId(), vehicle);
            for (ParcelShop parcelShop : parcelsByShop.keySet()) {
                int feederCounter = 0;
                int demandedCapacity = parcelsByShop.get(parcelShop).size();
                double remainder = demandedCapacity;
                while (remainder > 0) {
                    double current = Math.min(remainder, type.getCapacity().getOther());
                    remainder = remainder - current;
                    double duration_s = Math.min(5 * demandedCapacity, 15 * 60);
                    Coord destCoord = new Coord(parcelShop.getCoord_gk4().x, parcelShop.getCoord_gk4().y);
                    Id<Link> linkParcelDelivery = NetworkUtils.getNearestLink(network, destCoord).getId();
                    CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create("to_shop_" + parcelShop.getZoneId() + "_" + feederCounter, CarrierService.class), linkParcelDelivery);
                    serviceBuilder.setCapacityDemand((int) Math.round(current));
                    serviceBuilder.setServiceDuration(duration_s);
                    serviceBuilder.setServiceStartTimeWindow(timeWindow);
                    CarrierService carrierService = serviceBuilder.build();
                    feederCarrier.getServices().put(carrierService.getId(), carrierService);
                    feederCounter++;
                    remainder -= current;

                }
            }
            logger.info("Completed distribution center: " + distributionCenter.getId() + ", " + distributionCenter.getName());

        }
        dataSet.setModeByCarrier(modeByCarrier);

    }

    private Link getNearestLinkByMode(Coordinate coordinates, ParcelDistributionType mode) {
        if (mode.equals(ParcelDistributionType.MOTORIZED)) {
            Link thisLink = NetworkUtils.getNearestLink(network, new Coord(coordinates.x, coordinates.y));
            Link nearestLink;
            while ((nearestLink = FreightFlowUtils.findUpstreamLinksForMotorizedVehicle(thisLink)) == null) {
                thisLink = thisLink.getFromNode().getInLinks().values().iterator().next();
            }
            return nearestLink;

        } else {
            return NetworkUtils.getNearestLink(network, new Coord(coordinates.x, coordinates.y));
        }

    }

    private void createDeliveriesByMotorizedModes(List<Parcel> parcelsThisCarrier, Carrier carrier) {
        int parcelIndex = 0;
        int parcelsToMicroDepotIndex = 0;
        int parcelsToParcelShop = 0;
        for (Parcel parcel : parcelsThisCarrier) {
            if (properties.getRand().nextDouble() < properties.getSampleFactorForParcels()) {
                Coord parcelCoord;
                parcel.setAssigned(true);
                if (!parcel.getParcelTransaction().equals(ParcelTransaction.PARCEL_SHOP)) {
                    if (parcel.isToDestination()) {
                        parcelCoord = new Coord(parcel.getDestCoord().x, parcel.getDestCoord().y);
                    } else {
                        parcelCoord = new Coord(parcel.getOriginCoord().x, parcel.getOriginCoord().y);
                    }

                    if (parcel.getParcelDistributionType().equals(ParcelDistributionType.MOTORIZED)) {
                        TimeWindow timeWindow = generateRandomTimeSubWindow(7, 17, 1);
                        Id<Link> linkParcelDelivery = getNearestLinkByMode(new Coordinate(parcelCoord.getX(), parcelCoord.getY()), ParcelDistributionType.MOTORIZED).getId();
                        Node toNode = network.getLinks().get(linkParcelDelivery).getToNode();
                        double distance = NetworkUtils.getEuclideanDistance(toNode.getCoord(), parcelCoord);
                        parcel.setAccessDistance_m(distance);
                        double duration_s = fixDeliveryTime_s + distance / parcelAccessSpeed_ms;

                        CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create(parcel.getId(),
                                CarrierService.class), linkParcelDelivery);
                        serviceBuilder.setCapacityDemand(1);
                        serviceBuilder.setServiceDuration(duration_s);
                        serviceBuilder.setServiceStartTimeWindow(timeWindow);
                        CarrierService carrierService = serviceBuilder.build();
                        carrier.getServices().put(carrierService.getId(), carrierService);
                        parcelIndex++;
                    } else if (parcel.getParcelDistributionType().equals(ParcelDistributionType.CARGO_BIKE)) {
                        MicroDepot microDepot = parcel.getMicroDepot();
                        parcelsByMicrodepotScaled.get(microDepot).add(parcel);
                        parcelsToMicroDepotIndex++;
                    }
                } else {
                    ParcelShop parcelShop = parcel.getParcelShop();
                    parcelsByShop.get(parcelShop).add(parcel);
                    parcelsToParcelShop++;
                }
            }
        }
//        for (MicroDepot microDepot : parcelsByMicrodepotScaled.keySet()) {
//        }
        logger.info("Assigned " + parcelIndex + " parcels at this carrier");
        logger.info("Assigned " + parcelsToMicroDepotIndex + " parcels at this carrier via microDepot");
        logger.info("Assigned " + parcelsToParcelShop + " parcels at this carrier via parcelShop");
    }

    private void createDeliveriesByCargoBikes(List<Parcel> parcelsInThisMicroDepot, Carrier carrier) {

        int parcelIndex = 0;
        for (Parcel parcel : parcelsInThisMicroDepot) {
            //no need to scale again
            Coord parcelCoord;
            if (parcel.isToDestination() && parcel.getDestCoord() != null) {
                parcelCoord = new Coord(parcel.getDestCoord().x, parcel.getDestCoord().y);
            } else if (!parcel.isToDestination() && parcel.getOriginCoord() != null) {
                parcelCoord = new Coord(parcel.getOriginCoord().x, parcel.getOriginCoord().y);
            } else {
                parcelCoord = null;
            }

            if (parcel.isAssigned()) {
                TimeWindow timeWindow;
                timeWindow = generateRandomTimeSubWindow(8, 17, 1);
                Id<Link> linkParcelDelivery = getNearestLinkByMode(new Coordinate(parcelCoord.getX(), parcelCoord.getY()), ParcelDistributionType.CARGO_BIKE).getId();
                Node toNode = network.getLinks().get(linkParcelDelivery).getToNode();
                double distance = NetworkUtils.getEuclideanDistance(toNode.getCoord(), parcelCoord);
                parcel.setAccessDistance_m(distance);
                double duration_s = fixDeliveryTime_s + distance / parcelAccessSpeed_ms;
                CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create(parcel.getId(), CarrierService.class), linkParcelDelivery);
                serviceBuilder.setCapacityDemand(1);
                serviceBuilder.setServiceDuration(duration_s);
                serviceBuilder.setServiceStartTimeWindow(timeWindow);
                CarrierService carrierService = serviceBuilder.build();
                carrier.getServices().put(carrierService.getId(), carrierService);
                parcelIndex++;
            }
        }

        logger.info("Assigned " + parcelIndex + " parcels at this micro-depot carrier");


    }


    private static CarrierVehicle getGenericVehicle(VehicleType type, Id<?> carrierId, Id<Link> homeId, double start_s, double end_s) {
        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create(("carrier_" +
                carrierId.toString() +
                "_" + type.getId().toString()), Vehicle.class), homeId);
        vBuilder.setEarliestStart(start_s);
        vBuilder.setLatestEnd(end_s);
        vBuilder.setType(type);
        return vBuilder.build();
    }

    private static CarrierVehicle getSpecificVehicle(VehicleType type, Id<?> carrierId, int index, Id<Link> homeId, double start_s, double end_s) {
        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create(("carrier_" +
                carrierId.toString() +
                "_" + type.getId().toString() + "_" + index), Vehicle.class), homeId);
        vBuilder.setEarliestStart(start_s);
        vBuilder.setLatestEnd(end_s);
        vBuilder.setType(type);
        return vBuilder.build();
    }

//    private static CarrierVehicle getLightVehicle(Id<?> id, Id<Link> homeId, String depot) {
//        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create(("carrier_" + id.toString() + "_lightVehicle_" + depot), Vehicle.class), homeId);
//        vBuilder.setEarliestStart(6 * 60 * 60);
//        vBuilder.setLatestEnd(16 * 60 * 60);
//        vBuilder.setType(createLightType());
//        return vBuilder.build();
//    }
//
//    private static CarrierVehicleType createLightType() {
//        CarrierVehicleType.Builder typeBuilder = CarrierVehicleType.Builder.newInstance(Id.create("small", VehicleType.class));
//        typeBuilder.setCapacity(6);
//        typeBuilder.setFixCost(80.0);
//        typeBuilder.setCostPerDistanceUnit(0.00047);
//        typeBuilder.setCostPerTimeUnit(0.008);
//        return typeBuilder.build();
//    }
//
//    private static CarrierVehicle getHeavyVehicle(Id<?> id, Id<Link> homeId, String depot) {
//        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create("carrier_" + id.toString() + "_heavyVehicle_" + depot, Vehicle.class), homeId);
//        vBuilder.setEarliestStart(6 * 60 * 60);
//        vBuilder.setLatestEnd(16 * 60 * 60);
//        vBuilder.setType(createHeavyType());
//        return vBuilder.build();
//    }
//
//    private static CarrierVehicleType createHeavyType() {
//        CarrierVehicleType.Builder typeBuilder = CarrierVehicleType.Builder.newInstance(Id.create("heavy", VehicleType.class));
//        typeBuilder.setCapacity(25);
//        typeBuilder.setFixCost(130.0);
//        typeBuilder.setCostPerDistanceUnit(0.00077);
//        typeBuilder.setCostPerTimeUnit(0.008);
//        return typeBuilder.build();
//    }

    public TimeWindow generateRandomTimeSubWindow(double minTime_h, double maxTime_h, int slices) {
        double interval_s = (maxTime_h - minTime_h) * 3600 / slices;
        int i = 0;
        double randomNumber = properties.getRand().nextDouble();
        while (randomNumber * slices > i) {
            i++;
        }
        return TimeWindow.newInstance(3600 * minTime_h + (i - 1) * interval_s, 3600 * minTime_h + i * interval_s);
    }

}

package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.ParcelDistributionType;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.data.geo.MicroDepot;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
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

public class CarriersAndServicesGenerator {

    private DataSet dataSet;
    private Network network;
    private Properties properties;
    private static final Logger logger = Logger.getLogger(CarriersAndServicesGenerator.class);
    /**
     * this maps keeps the parcels in microdepots to assign first the trips to microdepot
     * with motorized vehicles and later the cargo bike trips from the depot
     */
    private Map<MicroDepot, List<Parcel>> parcelsByMicrodepot_scaled = new HashMap<>();
    private final int fixDeliveryTime_s = 60;
    private final double parcelAccessSpeed_ms = 5 / 3.6;
    private final int MAX_NUMBER_PARCELS = 500;

    public CarriersAndServicesGenerator(DataSet dataSet, Network network, Properties properties) {
        this.dataSet = dataSet;
        this.network = network;
        this.properties = properties;
    }

    public void generateCarriers(Carriers carriers, CarrierVehicleTypes types) {
        AtomicInteger carrierCounter = new AtomicInteger(0);
        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()) {
            List<Parcel> parcelsInThisDistributionCenter = dataSet.getParcelsByDistributionCenter().get(distributionCenter);
            for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {
                //initialize
                parcelsByMicrodepot_scaled.put(microDepot, new ArrayList<>());
            }

            int numberOfCarriers = (int) Math.floor(parcelsInThisDistributionCenter.size() / MAX_NUMBER_PARCELS) + 1;
            int numberOfParcelsByCarrier = parcelsInThisDistributionCenter.size() / numberOfCarriers;

            //split the problem of van deliveries to various carriers by limiting the number of services by carrier
            for (int carrierIndex = 0; carrierIndex < numberOfCarriers; carrierIndex++) {
                int firstParcel = carrierIndex * numberOfParcelsByCarrier;
                int lastParcel = (1 + carrierIndex) * numberOfParcelsByCarrier - 1;
                logger.info("Split distribution center into carrier " + carrierIndex + ": parcels " + firstParcel + " to " + lastParcel);
                List<Parcel> parcelsInThisCarrier = parcelsInThisDistributionCenter.subList(firstParcel, lastParcel);
                Carrier carrier = CarrierImpl.newInstance(Id.create(carrierCounter.getAndIncrement(), Carrier.class));
                Coordinate coordinates = distributionCenter.getCoordinates();
                Link link = getNearestLinkByMode(coordinates, ParcelDistributionType.MOTORIZED);
                Id<Link> linkId = link.getId();
                CarrierVehicleType type = types.getVehicleTypes().get(Id.create("van", VehicleType.class));
                carrier.getCarrierCapabilities().getVehicleTypes().add(type);
                carrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
                CarrierVehicle vehicle = getGenericVehicle(type, carrier.getId(), linkId, 7 * 60 * 60, 17 * 60 * 60);
                carrier.getCarrierCapabilities().getCarrierVehicles().add(vehicle);
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
            Coordinate coordinates = distributionCenter.getCoordinates();
            Link link = getNearestLinkByMode(coordinates, ParcelDistributionType.MOTORIZED);
            Id<Link> linkId = link.getId();
            CarrierVehicleType type = types.getVehicleTypes().get(Id.create("van", VehicleType.class));
            feederCarrier.getCarrierCapabilities().getVehicleTypes().add(type);
            feederCarrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
            CarrierVehicle vehicle = getGenericVehicle(type, feederCarrier.getId(), linkId, 7 * 60 * 60, 8 * 60 * 60);
            feederCarrier.getCarrierCapabilities().getCarrierVehicles().add(vehicle);

            for (MicroDepot microDepot : distributionCenter.getMicroDeportsServedByThis()) {

                Coord destCoord = new Coord(microDepot.getCoord_gk4().x, microDepot.getCoord_gk4().y);
                TimeWindow timeWindow = generateRandomTimeSubWindow(7, 8, 1);
                int demandedCapacity = parcelsByMicrodepot_scaled.get(microDepot).size();

                if (demandedCapacity > 0) {
                    //add feeder trips to the carrier
                    double duration_s = Math.min(5 * demandedCapacity, 15 * 60);
                    Id<Link> linkParcelDelivery = NetworkUtils.getNearestLink(network, destCoord).getId();
                    CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create("to_micro_depot_" + microDepot.getId(), CarrierService.class), linkParcelDelivery);
                    serviceBuilder.setCapacityDemand(demandedCapacity);
                    serviceBuilder.setServiceDuration(duration_s);
                    serviceBuilder.setServiceStartTimeWindow(timeWindow);
                    feederCarrier.getServices().add(serviceBuilder.build());

                    //create a microdepot Carrier (sub-carrier) with only bicycles
                    Carrier microDepotCarrier = CarrierImpl.newInstance(Id.create(microDepot.getId() + "_microDepot", Carrier.class));
                    carriers.addCarrier(microDepotCarrier);
                    Coordinate microDepotCoord = microDepot.getCoord_gk4();
                    Link microDepotLink = getNearestLinkByMode(microDepotCoord, ParcelDistributionType.MOTORIZED);
                    Id<Link> microDepotLinkId = microDepotLink.getId();
                    CarrierVehicleType cargoBikeType = types.getVehicleTypes().get(Id.create("cargoBike", VehicleType.class));
                    microDepotCarrier.getCarrierCapabilities().getVehicleTypes().add(cargoBikeType);
                    List<Parcel> parcelsInThisMicroDepot = parcelsByMicrodepot_scaled.get(microDepot);
                    CarrierVehicle cargoBike = getGenericVehicle(cargoBikeType, microDepotCarrier.getId(),
                            microDepotLinkId, 8 * 60 * 60, 17 * 60 * 60);
                    microDepotCarrier.getCarrierCapabilities().getCarrierVehicles().add(cargoBike);
                    microDepotCarrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);

                    //deliver the parcels later with cargo bikes
                    createDeliveriesByCargoBikes(parcelsInThisMicroDepot, microDepotCarrier);
                }
            }
        }

    }

    private Link getNearestLinkByMode(Coordinate coordinates, ParcelDistributionType mode) {
        if (mode.equals(ParcelDistributionType.MOTORIZED)) {
            Link thisLink = NetworkUtils.getNearestLink(network, new Coord(coordinates.x, coordinates.y));
            Link nearestLink;
            while  ((nearestLink = findUpstreamLinksForMotorizedVehicle(thisLink)) == null) {
                thisLink = thisLink.getFromNode().getInLinks().values().iterator().next();
            }
            return nearestLink;

        } else {
            return NetworkUtils.getNearestLink(network, new Coord(coordinates.x, coordinates.y));
        }

    }

    private Link findUpstreamLinksForMotorizedVehicle(Link thisLink) {
        if (thisLink.getAttributes().getAttribute("onlyCargoBike").equals(true)) {
            Map<Id<Link>, Link> upstreamLinks = (Map<Id<Link>, Link>) thisLink.getFromNode().getInLinks();
            for (Link upstreamLink : upstreamLinks.values()) {
                if (upstreamLink.getAttributes().getAttribute("onlyCargoBike").equals(true)) {
                    return upstreamLink;
                } else {
                    return null;
                }
            }
            return null;
        } else {
            return thisLink;
        }
    }

    private void createDeliveriesByMotorizedModes(List<Parcel> parcelsThisCarrier, Carrier carrier) {
        int parcelIndex = 0;
        int parcelsToMicroDepotIndex = 0;
        for (Parcel parcel : parcelsThisCarrier) {
            if (properties.getRand().nextDouble() < properties.getSampleFactorForParcels()) {
                parcel.setAssigned(true);
                if (parcel.isToDestination() && parcel.getDestCoord() != null) {
                    if (parcel.getParcelDistributionType().equals(ParcelDistributionType.MOTORIZED)) {
                        Coord parcelCoord = new Coord(parcel.getDestCoord().x, parcel.getDestCoord().y);
                        TimeWindow timeWindow = generateRandomTimeSubWindow(7, 17, 1);
                        Id<Link> linkParcelDelivery = getNearestLinkByMode(parcel.getDestCoord(), ParcelDistributionType.MOTORIZED).getId();
                        Node toNode = network.getLinks().get(linkParcelDelivery).getToNode();
                        double distance = NetworkUtils.getEuclideanDistance(toNode.getCoord(), parcelCoord);
                        double duration_s = fixDeliveryTime_s + distance / parcelAccessSpeed_ms;

                        CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create(parcel.getId(),
                                CarrierService.class), linkParcelDelivery);
                        serviceBuilder.setCapacityDemand(1);
                        serviceBuilder.setServiceDuration(duration_s);
                        serviceBuilder.setServiceStartTimeWindow(timeWindow);
                        carrier.getServices().add(serviceBuilder.build());
                        parcelIndex++;
                    } else {
                        MicroDepot microDepot = parcel.getMicroDepot();
                        parcelsByMicrodepot_scaled.get(microDepot).add(parcel);
                        parcelsToMicroDepotIndex++;
                    }
                }
            }
        }
//        for (MicroDepot microDepot : parcelsByMicrodepot_scaled.keySet()) {
//        }
        logger.info("Assigned " + parcelIndex + " parcels at this carrier");
        logger.info("Assigned " + parcelsToMicroDepotIndex + " parcels at this carrier via microDepot");
    }

    private void createDeliveriesByCargoBikes(List<Parcel> parcelsInThisMicroDepot, Carrier carrier) {

        int parcelIndex = 0;
        for (Parcel parcel : parcelsInThisMicroDepot) {
            //no need to scale again
            if (parcel.isToDestination() && parcel.getDestCoord() != null) {
                Coord parcelCoord;
                TimeWindow timeWindow;
                parcelCoord = new Coord(parcel.getDestCoord().x, parcel.getDestCoord().y);
                timeWindow = generateRandomTimeSubWindow(8, 17, 1);
                Id<Link> linkParcelDelivery = getNearestLinkByMode(parcel.getDestCoord(), ParcelDistributionType.CARGO_BIKE).getId();
                Node toNode = network.getLinks().get(linkParcelDelivery).getToNode();
                double distance = NetworkUtils.getEuclideanDistance(toNode.getCoord(), parcelCoord);
                double duration_s = fixDeliveryTime_s + distance / parcelAccessSpeed_ms;
                CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create(parcel.getId(), CarrierService.class), linkParcelDelivery);
                serviceBuilder.setCapacityDemand(1);
                serviceBuilder.setServiceDuration(duration_s);
                serviceBuilder.setServiceStartTimeWindow(timeWindow);
                carrier.getServices().add(serviceBuilder.build());
                parcelIndex++;
            }
        }
        logger.info("Assigned " + parcelIndex + " parcels at this micro-depot carrier");


    }


    private static CarrierVehicle getGenericVehicle(CarrierVehicleType type, Id<?> carrierId, Id<Link> homeId, double start_s, double end_s) {
        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create(("carrier_" +
                carrierId.toString() +
                "_" + type.getId().toString()), Vehicle.class), homeId);
        vBuilder.setEarliestStart(start_s);
        vBuilder.setLatestEnd(end_s);
        vBuilder.setType(type);
        return vBuilder.build();
    }

    private static CarrierVehicle getSpecificVehicle(CarrierVehicleType type, Id<?> carrierId, int index, Id<Link> homeId, double start_s, double end_s) {
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

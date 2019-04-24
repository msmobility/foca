package de.tum.bgu.msm.freight.modules.runMATSim;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.Parcel;
import de.tum.bgu.msm.freight.data.geo.DistributionCenter;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CarriersGen {

    private DataSet dataSet;
    private Network network;
    private Properties properties;
    private static final Logger logger = Logger.getLogger(CarriersGen.class);

    public CarriersGen(DataSet dataSet, Network network, Properties properties) {
        this.dataSet = dataSet;
        this.network = network;
        this.properties = properties;
    }

    public void generateCarriers(Carriers carriers, CarrierVehicleTypes types){

        AtomicInteger carrierCounter = new AtomicInteger(0);

        for (DistributionCenter distributionCenter : dataSet.getParcelsByDistributionCenter().keySet()){

            Carrier carrier = CarrierImpl.newInstance(Id.create(carrierCounter.getAndIncrement(),Carrier.class));
            carriers.addCarrier(carrier);

            Coord coordinates = distributionCenter.getCoordinates();

            Link link = NetworkUtils.getNearestLink(network, coordinates);
            Id<Link> linkId = link.getId();
            //light
            carrier.getCarrierCapabilities().getCarrierVehicles().add(getLightVehicle(carrier.getId(), linkId, "a"));
            //heavy
            carrier.getCarrierCapabilities().getCarrierVehicles().add(getHeavyVehicle(carrier.getId(), linkId, "a"));

//            Link oppositeLink = NetworkUtils.findLinkInOppositeDirection(link);
//            if (oppositeLink != null){
//                Id<Link> oppositeLinkId = oppositeLink.getId();
//                carrier.getCarrierCapabilities().getCarrierVehicles().add(getLightVehicle(carrier.getId(), oppositeLinkId, "b"));
//                carrier.getCarrierCapabilities().getCarrierVehicles().add(getHeavyVehicle(carrier.getId(), oppositeLinkId, "b"));
//            }

            for (CarrierVehicleType type : types.getVehicleTypes().values()){
                carrier.getCarrierCapabilities().getVehicleTypes().add(type);
            }

            carrier.getCarrierCapabilities().setFleetSize(CarrierCapabilities.FleetSize.INFINITE);
            List<Parcel> parcelsInThisDistributionCenter = dataSet.getParcelsByDistributionCenter().get(distributionCenter);
            createCustomers(parcelsInThisDistributionCenter, carrier);

        }

    }

    private void createCustomers(List<Parcel> parcelsInThisDistributionCenter, Carrier carrier) {

        int parcelIndex = 0;
        for(Parcel parcel : parcelsInThisDistributionCenter) {
            if (parcel.isToDestination() &&
                    properties.getRand().nextDouble() < properties.getSampleFactorForParcels() &&
                    parcel.getDestCoord() != null) {
                Coord parcelCoord = parcel.getDestCoord();
                Id<Link> linkParcelDelivery = NetworkUtils.getNearestLink(network, parcelCoord).getId();
                CarrierService.Builder serviceBuilder = CarrierService.Builder.newInstance(Id.create((parcelIndex), CarrierService.class), linkParcelDelivery);
                serviceBuilder.setCapacityDemand(1);
                serviceBuilder.setServiceDuration(5 * 60);
                serviceBuilder.setServiceStartTimeWindow(TimeWindow.newInstance(6 * 60 * 60, 15 * 60 * 60));
                carrier.getServices().add(serviceBuilder.build());
                parcelIndex++;
            }
        }
        logger.info("Assigned " + parcelIndex + " parcels at this carrier");


    }


    private static CarrierVehicle getLightVehicle(Id<?> id, Id<Link> homeId, String depot) {
        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create(("carrier_"+id.toString()+"_lightVehicle_" + depot) ,Vehicle.class), homeId);
        vBuilder.setEarliestStart(6*60*60);
        vBuilder.setLatestEnd(16*60*60);
        vBuilder.setType(createLightType());
        return vBuilder.build();
    }

    private static CarrierVehicleType createLightType() {
        CarrierVehicleType.Builder typeBuilder = CarrierVehicleType.Builder.newInstance(Id.create("small",VehicleType.class));
        typeBuilder.setCapacity(6);
        typeBuilder.setFixCost(80.0);
        typeBuilder.setCostPerDistanceUnit(0.00047);
        typeBuilder.setCostPerTimeUnit(0.008);
        return typeBuilder.build();
    }

    private static CarrierVehicle getHeavyVehicle(Id<?> id, Id<Link> homeId, String depot) {
        CarrierVehicle.Builder vBuilder = CarrierVehicle.Builder.newInstance(Id.create("carrier_" + id.toString() + "_heavyVehicle_" + depot, Vehicle.class), homeId);
        vBuilder.setEarliestStart(6*60*60);
        vBuilder.setLatestEnd(16*60*60);
        vBuilder.setType(createHeavyType());
        return vBuilder.build();
    }

    private static CarrierVehicleType createHeavyType() {
        CarrierVehicleType.Builder typeBuilder = CarrierVehicleType.Builder.newInstance(Id.create("heavy", VehicleType.class));
        typeBuilder.setCapacity(25);
        typeBuilder.setFixCost(130.0);
        typeBuilder.setCostPerDistanceUnit(0.00077);
        typeBuilder.setCostPerTimeUnit(0.008);
        return typeBuilder.build();
    }

}

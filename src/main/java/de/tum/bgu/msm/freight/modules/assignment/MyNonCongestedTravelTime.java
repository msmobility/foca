package de.tum.bgu.msm.freight.modules.assignment;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierVehicleType;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;

import java.util.List;

/**
 * Implementation of the TravelTIme interface to temporary avoid routing motor vehicles on cargo bike links. The problem was
 * that Freight extension defines the mode for every freight vehicles in the class CarrierAgent and possibly in many other places.
 * This will not work well when there are cars on the road network. Ideally will soon use bike as mode for cargo bikes.
 */
public class MyNonCongestedTravelTime implements TravelTime {

    private VehicleType cargoBikeType = CarrierVehicleType.Builder.newInstance(Id.create("cargoBike", VehicleType.class)).build();

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
        double velocity;
        VehicleType carrierVehicleType = vehicle.getType();
        if (carrierVehicleType.getMaximumVelocity() < link.getFreespeed(time)) {
            velocity = vehicle.getType().getMaximumVelocity();
        } else {
            velocity = link.getFreespeed(time);
        }
        //if the vehicle is not a cargo bike, and the link is only for cargoBikes, speed equal to infinity
        // (this will not work with cars)
        if (link.getAttributes().getAttribute("onlyCargoBike").equals(true) &&
                !carrierVehicleType.getId().equals(cargoBikeType.getId())){
            return link.getLength() / 0.001;
        }

        if (velocity <= 0.0D) {
            throw new IllegalStateException("velocity must be bigger than zero");
        } else {
            return link.getLength() / velocity;
        }
    }



}

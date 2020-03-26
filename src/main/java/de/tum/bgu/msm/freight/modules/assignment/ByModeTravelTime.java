package de.tum.bgu.msm.freight.modules.assignment;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class ByModeTravelTime implements TravelTime {

    private final String mode;

    public ByModeTravelTime(String mode) {
        this.mode = mode;
    }

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
        double velocity = vehicle.getType().getMaximumVelocity();
        if(link.getAllowedModes().contains(mode)){
            return link.getLength()/Math.min(velocity, link.getFreespeed());
        } else {
            return link.getLength()/0.001;
        }
    }
}

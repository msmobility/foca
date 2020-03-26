package de.tum.bgu.msm.freight.modules.assignment;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.util.TravelTime;
import org.matsim.vehicles.Vehicle;

public class BikeTravelTime implements TravelTime {

    @Override
    public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle) {
        if(link.getAllowedModes().contains("bike")){
            return link.getLength()/link.getFreespeed();
        } else {
            return Double.MAX_VALUE;
        }
    }
}

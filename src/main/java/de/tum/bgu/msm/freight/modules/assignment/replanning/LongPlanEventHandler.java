package de.tum.bgu.msm.freight.modules.assignment.replanning;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.LinkEnterEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;


public class LongPlanEventHandler implements LinkEnterEventHandler, PersonEntersVehicleEventHandler {

    Scenario scenario;
    Network net;
    Population pop;
    Logger logger = Logger.getLogger(LongPlanEventHandler.class);

    //contains the first stop after midnight
    Map<Id<Person>, Id<Vehicle>> personToVehicle  = new HashMap<>();
    Map<Id<Vehicle>, Id<Link>> stopsAfter24h = new HashMap<>();
    Map<Id<Vehicle>, Id<Link>> stopsAfter48h = new HashMap<>();

    public LongPlanEventHandler(Scenario scenario) {
        this.scenario = scenario;
        this.net = scenario.getNetwork();
        this.pop = scenario.getPopulation();
    }


    @Override
    public void reset(int iteration) {
        stopsAfter24h = new HashMap<>();
        stopsAfter48h = new HashMap<>();
        personToVehicle = new HashMap<>();

    }


    public void handlePlan(Plan longPlan) {

        Id<Vehicle> vehicleId = personToVehicle.get(longPlan.getPerson().getId());
        if (stopsAfter24h.containsKey(vehicleId)) {
            //the plan takes 3 days, need to create 2 new persons
            longPlan.getPlanElements().remove(longPlan.getPlanElements().size() - 1);
            Activity act = pop.getFactory().createActivityFromLinkId("destination", stopsAfter24h.get(vehicleId));
            longPlan.addActivity(act);
        }

    }

    @Override
    public void handleEvent(LinkEnterEvent linkEnterEvent) {
        if (linkEnterEvent.getTime() > 24 * 60 * 60) {
            if (linkEnterEvent.getTime() > 48 * 60 * 60) {
                if (!stopsAfter48h.containsKey(linkEnterEvent.getVehicleId())){
                    stopsAfter48h.put(linkEnterEvent.getVehicleId(), linkEnterEvent.getLinkId());
                }
            } else {
                if (!stopsAfter24h.containsKey(linkEnterEvent.getVehicleId())){
                    stopsAfter24h.put(linkEnterEvent.getVehicleId(), linkEnterEvent.getLinkId());
                }
            }
        }

    }

    @Override
    public void handleEvent(PersonEntersVehicleEvent personEntersVehicleEvent) {

    }
}

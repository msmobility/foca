package de.tum.bgu.msm.emission;


import de.tum.bgu.msm.emission.data.AnalyzedLink;
import de.tum.bgu.msm.emission.data.AnalyzedVehicle;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.emissions.events.ColdEmissionEvent;
import org.matsim.contrib.emissions.events.ColdEmissionEventHandler;
import org.matsim.contrib.emissions.events.WarmEmissionEvent;
import org.matsim.contrib.emissions.events.WarmEmissionEventHandler;
import org.matsim.vehicles.Vehicle;

import java.util.HashMap;
import java.util.Map;

public class LinkEmissionHandler implements WarmEmissionEventHandler, ColdEmissionEventHandler {



    private Network network;
    private Map<Id<Vehicle>, AnalyzedVehicle> emmisionsByVehicle;
    private Map<Id<Link>, AnalyzedLink> emmisionsByLink;

    public LinkEmissionHandler(Network network) {
        this.network = network;
        emmisionsByLink = new HashMap<>();
        emmisionsByVehicle = new HashMap<>();
    }

    @Override
    public void handleEvent(WarmEmissionEvent event) {
        Id<Link> linkId = event.getLinkId();
        Link matsimLink = network.getLinks().get(linkId);
        emmisionsByLink.putIfAbsent(linkId,new AnalyzedLink(linkId, matsimLink));

        if (emmisionsByLink.get(linkId).getWarmEmissions().isEmpty()){
            emmisionsByLink.get(linkId).getWarmEmissions().putAll(event.getWarmEmissions());
        } else {
            Map<String, Double> currentEmissions = emmisionsByLink.get(linkId).getWarmEmissions();
            for (String pollutant : currentEmissions.keySet()){
                currentEmissions.put(pollutant, currentEmissions.get(pollutant) + event.getWarmEmissions().get(pollutant));
            }
            emmisionsByLink.get(linkId).getWarmEmissions().putAll(currentEmissions);
        }

        Id<Vehicle> vehicleId = event.getVehicleId();
        emmisionsByVehicle.putIfAbsent(vehicleId, new AnalyzedVehicle(vehicleId));
        emmisionsByVehicle.get(vehicleId).addDistanceTravelled(matsimLink.getLength());


        if (emmisionsByVehicle.get(vehicleId).getWarmEmissions().isEmpty()){
            emmisionsByVehicle.get(vehicleId).getWarmEmissions().putAll(event.getWarmEmissions());
        } else {
            Map<String, Double> currentEmissions = emmisionsByVehicle.get(vehicleId).getWarmEmissions();
            for (String pollutant : currentEmissions.keySet()){
                currentEmissions.put(pollutant, currentEmissions.get(pollutant) + event.getWarmEmissions().get(pollutant));
            }
            emmisionsByVehicle.get(vehicleId).getWarmEmissions().putAll(currentEmissions);
        }
    }



    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(ColdEmissionEvent event) {

        Id<Link> linkId = event.getLinkId();
        Link matsimLink = network.getLinks().get(linkId);
        emmisionsByLink.putIfAbsent(linkId,new AnalyzedLink(linkId, matsimLink));

        if (emmisionsByLink.get(linkId).getWarmEmissions().isEmpty()){
            emmisionsByLink.get(linkId).getWarmEmissions().putAll(event.getColdEmissions());
        } else {
            Map<String, Double> currentEmissions = emmisionsByLink.get(linkId).getWarmEmissions();
            for (String pollutant : currentEmissions.keySet()){
                currentEmissions.put(pollutant, currentEmissions.get(pollutant) + event.getColdEmissions().get(pollutant));
            }
            emmisionsByLink.get(linkId).getWarmEmissions().putAll(currentEmissions);
        }

        Id<Vehicle> vehicleId = event.getVehicleId();
        emmisionsByVehicle.putIfAbsent(vehicleId, new AnalyzedVehicle(vehicleId));

        if (emmisionsByVehicle.get(vehicleId).getWarmEmissions().isEmpty()){
            emmisionsByVehicle.get(vehicleId).getWarmEmissions().putAll(event.getColdEmissions());
        } else {
            Map<String, Double> currentEmissions = emmisionsByVehicle.get(vehicleId).getWarmEmissions();
            for (String pollutant : currentEmissions.keySet()){
                currentEmissions.put(pollutant, currentEmissions.get(pollutant) + event.getColdEmissions().get(pollutant));
            }
            emmisionsByVehicle.get(vehicleId).getColdEmissions().putAll(currentEmissions);
        }
    }

    public Map<Id<Vehicle>, AnalyzedVehicle> getEmmisionsByVehicle() {
        return emmisionsByVehicle;
    }

    public Map<Id<Link>, AnalyzedLink> getEmmisionsByLink() {
        return emmisionsByLink;
    }
}

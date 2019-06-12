package de.tum.bgu.msm.emission;


import org.matsim.contrib.emissions.events.WarmEmissionEvent;
import org.matsim.contrib.emissions.events.WarmEmissionEventHandler;

public class LinkEmissionHandler implements WarmEmissionEventHandler {


    double total_C02;

    public LinkEmissionHandler() {
        this.total_C02 = 0;
    }

    @Override
    public void handleEvent(WarmEmissionEvent event) {
        total_C02 += event.getWarmEmissions().get("CO2");
    }

    public double getTotalC02() {
        return total_C02;
    }

    @Override
    public void reset(int iteration) {

    }
}

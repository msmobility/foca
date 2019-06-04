package de.tum.bgu.msm.freight.modules.runMATSim;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.counts.CountEventHandler;
import de.tum.bgu.msm.freight.io.input.LinksFileReader;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.counts.MultiDayCounts;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.freight.Freight;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.controler.CarrierModule;
import org.matsim.contrib.freight.replanning.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.contrib.freight.usecases.analysis.LegHistogram;
import org.matsim.contrib.freight.usecases.chessboard.RunChessboard;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.mobsim.qsim.AbstractQSimModule;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.scenario.MutableScenario;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.io.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

public class MATSimAssignment implements Module {

    private Config config;
    private MutableScenario scenario;
    private Network network;
    private Controler controler;

    private Properties properties;
    private DataSet dataSet;
    private MATSimFreightManager matSimFreightManager;

    public MATSimAssignment() {

    }

    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.properties = properties;
        this.dataSet = dataSet;

        config = MATSimConfigUtils.configure(ConfigUtils.createConfig(), properties);

        scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        controler = new Controler(scenario);

        matSimFreightManager = new MATSimFreightManager(config, scenario, controler, properties);

        if (properties.isRunParcelDelivery()) {
            //configure MATSim for freight extension
            matSimFreightManager.configureMATSimForFreight();
        }


    }

    @Override
    public void run() {
        createPopulation();

        runMatsim();
    }


    private void generateCarriersPopulation(Carriers carriers, CarrierVehicleTypes carrierVehicleTypes){

        //create carriers
        new CarriersGen(dataSet, network, properties).generateCarriers(carriers, carrierVehicleTypes);

        //assign plans to carriers
        new CarriersPlanGen(scenario.getNetwork()).generateCarriersPlan(carriers);

    }



    private void createPopulation() {
        Population population = dataSet.getMatsimPopulation();
        scenario.setPopulation(population);

        generateCarriersPopulation(matSimFreightManager.getCarriers(), matSimFreightManager.getCarrierVehicleTypes());
    }

    private void runMatsim() {

        CountEventHandler countEventHandler = new CountEventHandler(properties);
        if (properties.isReadEventsForCounts()) {
            LinksFileReader linksFileReader = new LinksFileReader(dataSet, properties.getCountStationLinkListFile());
            linksFileReader.read();

            for (Id linkId : dataSet.getObservedCounts().keySet()) {
                countEventHandler.addLinkById(linkId);
            }
            controler.getEvents().addHandler(countEventHandler);
        }

        controler.run();

        if (properties.isReadEventsForCounts()) {
            try {
                MultiDayCounts.printOutCounts("./output/" + properties.getRunId() + "/" + properties.getCountsFileName(), countEventHandler.getMapOfCOunts());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}

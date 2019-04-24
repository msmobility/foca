package de.tum.bgu.msm.freight.modules.runMATSim;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.modules.Module;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.counts.CountEventHandler;
import de.tum.bgu.msm.freight.io.input.LinksFileReader;
import de.tum.bgu.msm.freight.modules.longDistanceTruckAssignment.counts.MultiDayCounts;
import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.Id;
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

    public MATSimAssignment() {

    }

    @Override
    public void setup(DataSet dataSet, Properties properties) {
        this.properties = properties;
        this.dataSet = dataSet;
        configMatsim();
    }

    @Override
    public void run() {
        createPopulation();
        runMatsim();
    }

    private void configMatsim() {
        config = ConfigUtils.createConfig();

        config.controler().setFirstIteration(0);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.5);
            config.strategy().addStrategySettings(strategySettings);
        }
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.5);
            config.strategy().addStrategySettings(strategySettings);
        }

        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(4);

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("start");
        homeActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("end");
        workActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(workActivity);

        config.controler().setRunId(properties.getRunId());
        config.controler().setOutputDirectory("./output/" + properties.getRunId() + "/matsim");
        config.network().setInputFile(properties.getNetworkFile());



        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        config.qsim().setUsingThreadpool(false);

        int iterations = properties.getIterations();
        config.controler().setLastIteration(iterations);
        config.controler().setWritePlansInterval(iterations);
        config.controler().setWriteEventsInterval(iterations);

        config.qsim().setStuckTime(10);
        //skips scaling of the network!
        config.qsim().setFlowCapFactor(1);
        config.qsim().setStorageCapFactor(1);

        config.linkStats().setWriteLinkStatsInterval(1);

        //add truck as new mode to MATSim
        Set<String> modes = new HashSet<>();
        modes.addAll(config.qsim().getMainModes());
        modes.add(TransportMode.truck);
        config.qsim().setMainModes(modes);

        config.plansCalcRoute().setNetworkModes(modes);

        PlanCalcScoreConfigGroup.ModeParams carParams = config.planCalcScore().getOrCreateModeParams(TransportMode.car);
        PlanCalcScoreConfigGroup.ModeParams truckParams = new PlanCalcScoreConfigGroup.ModeParams(TransportMode.truck);
        truckParams.setConstant(carParams.getConstant());
        truckParams.setDailyMonetaryConstant(carParams.getDailyMonetaryConstant());
        truckParams.setMarginalUtilityOfDistance(carParams.getMarginalUtilityOfDistance());
        truckParams.setDailyUtilityConstant(carParams.getDailyUtilityConstant());
        truckParams.setMonetaryDistanceRate(carParams.getMonetaryDistanceRate());
        config.planCalcScore().addModeParams(truckParams);

        Set<String> analyzedModes = new HashSet<>();
        analyzedModes.add("truck");
        analyzedModes.add("car");

        config.travelTimeCalculator().setAnalyzedModes(analyzedModes);
        config.travelTimeCalculator().setSeparateModes(false);

        config.vehicles().setVehiclesFile(properties.getVehicleFile());
        config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
        config.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.FIFO);
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.queue);


        scenario = (MutableScenario) ScenarioUtils.loadScenario(config);
        network = scenario.getNetwork();
        controler = new Controler(scenario);

    }

    private void configureMATSimForFreight() {


        Freight.configure(controler);

        final Carriers carriers = FreightUtils.getCarriers( scenario ) ;

        final CarrierVehicleTypes types = new CarrierVehicleTypes();
        new CarrierVehicleTypeReader(types).readFile(properties.getVehicleFileForParcelDelivery());
        new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(types);

                //create carriers
        new CarriersGen(dataSet, network, properties).generateCarriers(carriers, types);




        //assign plans to carriers
        new CarriersPlanGen(scenario.getNetwork()).generateCarriersPlan(carriers);

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                                CarrierModule carrierModule = new CarrierModule(carriers);
                                carrierModule.setPhysicallyEnforceTimeWindowBeginnings(true);
                                install(carrierModule);

                bind(CarrierPlanStrategyManagerFactory.class).toInstance(new FreightFlowUtils.MyCarrierPlanStrategyManagerFactory(types));
                bind(CarrierScoringFunctionFactory.class).toInstance( new FreightFlowUtils.MyCarrierScoringFunctionFactory() );
            }
        });
        controler.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                final CarrierScoreStats scores = new CarrierScoreStats(carriers, config.controler().getOutputDirectory() +"/carrier_scores", true);
                final int statInterval = 1;
                final LegHistogram freightOnly = new LegHistogram(900);
                freightOnly.setInclPop(false);
                binder().requestInjection(freightOnly);
                final LegHistogram withoutFreight = new LegHistogram(900);
                binder().requestInjection(withoutFreight);

                addEventHandlerBinding().toInstance(withoutFreight);
                addEventHandlerBinding().toInstance(freightOnly);
                addControlerListenerBinding().toInstance(scores);
                addControlerListenerBinding().toInstance(new IterationEndsListener() {

                    @Inject
                    private OutputDirectoryHierarchy controlerIO;

                    @Override
                    public void notifyIterationEnds(IterationEndsEvent event) {
                        if (event.getIteration() % statInterval != 0) return;
                        //write plans
                        String dir = controlerIO.getIterationPath(event.getIteration());
                        new CarrierPlanXmlWriterV2(carriers).write(dir + "/" + event.getIteration() + ".carrierPlans.xml");

                        //write stats
                        freightOnly.writeGraphic(dir + "/" + event.getIteration() + ".legHistogram_freight.png");
                        freightOnly.reset(event.getIteration());

                        withoutFreight.writeGraphic(dir + "/" + event.getIteration() + ".legHistogram_withoutFreight.png");
                        withoutFreight.reset(event.getIteration());
                    }
                });
            }
        });
    }



    private void createPopulation(){
        Population population = dataSet.getMatsimPopulation();

        scenario.setPopulation(population);
    }

    private void runMatsim() {

        if (properties.isRunParcelDelivery()){
            //configure MATSim for freight extension
            configureMATSimForFreight();
        }


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

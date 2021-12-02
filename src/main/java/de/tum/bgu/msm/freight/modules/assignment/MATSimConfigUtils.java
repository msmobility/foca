package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.properties.Properties;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.OutputDirectoryHierarchy;

import java.util.HashSet;
import java.util.Set;

public class MATSimConfigUtils {

    public static Config configure(Config config, Properties properties) {

        config.controler().setFirstIteration(0);
        config.controler().setMobsim("qsim");
        config.controler().setWritePlansInterval(config.controler().getLastIteration());
        config.controler().setWriteEventsInterval(config.controler().getLastIteration());
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        //config.qsim().setEndTime(30 * 60 * 60);

        config.vspExperimental().setWritingOutputEvents(true); // writes final events into toplevel directory

        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ChangeExpBeta");
            strategySettings.setWeight(0.8);
            config.strategy().addStrategySettings(strategySettings);
        }
        {
            StrategyConfigGroup.StrategySettings strategySettings = new StrategyConfigGroup.StrategySettings();
            strategySettings.setStrategyName("ReRoute");
            strategySettings.setWeight(0.2);
            config.strategy().addStrategySettings(strategySettings);
        }

        config.strategy().setFractionOfIterationsToDisableInnovation(0.8);
        config.strategy().setMaxAgentPlanMemorySize(4);

        PlanCalcScoreConfigGroup.ActivityParams startActivity = new PlanCalcScoreConfigGroup.ActivityParams("start");
        startActivity.setTypicalDuration(12 * 60 * 60);
        config.planCalcScore().addActivityParams(startActivity);

        PlanCalcScoreConfigGroup.ActivityParams endActivity = new PlanCalcScoreConfigGroup.ActivityParams("end");
        endActivity.setTypicalDuration(8 * 60 * 60);
        config.planCalcScore().addActivityParams(endActivity);

        PlanCalcScoreConfigGroup.ActivityParams homeActivity = new PlanCalcScoreConfigGroup.ActivityParams("home");
        homeActivity.setTypicalDuration(12*60*60);
        config.planCalcScore().addActivityParams(homeActivity);

        PlanCalcScoreConfigGroup.ActivityParams workActivity = new PlanCalcScoreConfigGroup.ActivityParams("work");
        workActivity.setTypicalDuration(8*60*60);
        config.planCalcScore().addActivityParams(workActivity);

        PlanCalcScoreConfigGroup.ActivityParams educationActivity = new PlanCalcScoreConfigGroup.ActivityParams("education");
        educationActivity.setTypicalDuration(8*60*60);
        config.planCalcScore().addActivityParams(educationActivity);

        PlanCalcScoreConfigGroup.ActivityParams shoppingActivity = new PlanCalcScoreConfigGroup.ActivityParams("shopping");
        shoppingActivity.setTypicalDuration(1*60*60);
        config.planCalcScore().addActivityParams(shoppingActivity);

        PlanCalcScoreConfigGroup.ActivityParams otherActivity = new PlanCalcScoreConfigGroup.ActivityParams("other");
        otherActivity.setTypicalDuration(1*60*60);
        config.planCalcScore().addActivityParams(otherActivity);

        PlanCalcScoreConfigGroup.ActivityParams airportActivity = new PlanCalcScoreConfigGroup.ActivityParams("airport");
        airportActivity.setTypicalDuration(1*60*60);
        config.planCalcScore().addActivityParams(airportActivity);

        {
            PlanCalcScoreConfigGroup.ActivityParams activityParams = new PlanCalcScoreConfigGroup.ActivityParams("orig_passenger");
            activityParams.setTypicalDuration(1 * 60 * 60);
            config.planCalcScore().addActivityParams(activityParams);
        }
        {
            PlanCalcScoreConfigGroup.ActivityParams activityParams = new PlanCalcScoreConfigGroup.ActivityParams("dest_passenger");
            activityParams.setTypicalDuration(1*60*60);
            config.planCalcScore().addActivityParams(activityParams);
        }


        PlansCalcRouteConfigGroup.ModeRoutingParams carPassengerParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("car_passenger");
        carPassengerParams.setTeleportedModeFreespeedFactor(1.0);
        config.plansCalcRoute().addModeRoutingParams(carPassengerParams);

        PlansCalcRouteConfigGroup.ModeRoutingParams ptParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("pt");
        ptParams.setBeelineDistanceFactor(1.5);
        ptParams.setTeleportedModeSpeed(50/3.6);
        config.plansCalcRoute().addModeRoutingParams(ptParams);

        /*PlansCalcRouteConfigGroup.ModeRoutingParams bicycleParams = new PlansCalcRouteConfigGroup.ModeRoutingParams(""TransportMode.bike"");
        bicycleParams.setBeelineDistanceFactor(1.3);
        bicycleParams.setTeleportedModeSpeed(15/3.6);
        config.plansCalcRoute().addModeRoutingParams(bicycleParams);*/

        config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);


        PlansCalcRouteConfigGroup.ModeRoutingParams walkParams = new PlansCalcRouteConfigGroup.ModeRoutingParams("walk");
        walkParams.setBeelineDistanceFactor(1.3);
        walkParams.setTeleportedModeSpeed(5/3.6);
        config.plansCalcRoute().addModeRoutingParams(walkParams);

        config.controler().setRunId(properties.getRunId());
        config.controler().setOutputDirectory("./output/" + properties.getRunId() + "/matsim");
        config.network().setInputFile(properties.getNetworkFile());


        config.qsim().setNumberOfThreads(16);
        config.global().setNumberOfThreads(16);
        config.parallelEventHandling().setNumberOfThreads(16);
        //config.qsim().setUsingThreadpool(false);

        int iterations = properties.getIterations();
        config.controler().setLastIteration(iterations);
        config.controler().setWritePlansInterval(iterations);
        config.controler().setWriteEventsInterval(iterations);

        double parcelFactor = properties.getSampleFactorForParcels();
        double truckFactor = properties.longDistance().getTruckScaleFactor();
        double scaleFactor;

        if (parcelFactor == truckFactor){
            scaleFactor = parcelFactor;
        } else {
            scaleFactor = Math.max(parcelFactor, truckFactor);
        }

        config.qsim().setStuckTime(10);
        //skips scaling of the network?
        config.qsim().setFlowCapFactor(scaleFactor * properties.getMatsimAdditionalScaleFactor());
        config.qsim().setStorageCapFactor(scaleFactor * properties.getMatsimAdditionalScaleFactor());

        config.linkStats().setWriteLinkStatsInterval(config.controler().getLastIteration());

        //add truck and TransportMode.bike as new mode to MATSim - these modes are routed in the same network and affected by congestion
        Set<String> modes = new HashSet<>();
        modes.addAll(config.qsim().getMainModes());
        modes.add(TransportMode.truck);
        modes.add(TransportMode.bike);

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

        PlanCalcScoreConfigGroup.ModeParams cargoBikeParams = new PlanCalcScoreConfigGroup.ModeParams("TransportMode.bike");
        cargoBikeParams.setConstant(carParams.getConstant());
        cargoBikeParams.setDailyMonetaryConstant(carParams.getDailyMonetaryConstant());
        cargoBikeParams.setMarginalUtilityOfDistance(carParams.getMarginalUtilityOfDistance());
        cargoBikeParams.setDailyUtilityConstant(carParams.getDailyUtilityConstant());
        cargoBikeParams.setMonetaryDistanceRate(carParams.getMonetaryDistanceRate());
        config.planCalcScore().addModeParams(cargoBikeParams);

        Set<String> analyzedModes = new HashSet<>();
        analyzedModes.add(TransportMode.truck);
        analyzedModes.add(TransportMode.car);
        analyzedModes.add(TransportMode.bike);

        config.travelTimeCalculator().setAnalyzedModes(analyzedModes);
        config.travelTimeCalculator().setSeparateModes(true);

        config.vehicles().setVehiclesFile(properties.getVehicleFile());
        config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.modeVehicleTypesFromVehiclesData);
        config.qsim().setLinkDynamics(QSimConfigGroup.LinkDynamics.FIFO); //todo consider passingQ? how is passing in MATSim?
        config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.queue);

        return config;
    }





}

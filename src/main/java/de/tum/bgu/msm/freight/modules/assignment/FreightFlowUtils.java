package de.tum.bgu.msm.freight.modules.assignment;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlan;
import org.matsim.contrib.freight.carrier.CarrierVehicleTypes;
import org.matsim.contrib.freight.replanning.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.replanning.modules.ReRouteVehicles;
import org.matsim.contrib.freight.replanning.modules.TimeAllocationMutator;
import org.matsim.contrib.freight.scoring.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.usecases.chessboard.TravelDisutilities;
import org.matsim.core.replanning.GenericPlanStrategyImpl;
import org.matsim.core.replanning.GenericStrategyManager;
import org.matsim.core.replanning.selectors.ExpBetaPlanChanger;
import org.matsim.core.replanning.selectors.KeepSelected;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.SumScoringFunction;

import javax.inject.Inject;
import java.util.Map;

public class FreightFlowUtils {

    public static class MyCarrierScoringFunctionFactory implements CarrierScoringFunctionFactory {

        @Inject
        private Network network;

        @Override
        public ScoringFunction createScoringFunction(Carrier carrier) {
            SumScoringFunction sf = new SumScoringFunction();
            MyCarrierScoring.DriversLegScoring driverLegScoring = new MyCarrierScoring.DriversLegScoring(carrier, network);
            MyCarrierScoring.VehicleEmploymentScoring vehicleEmploymentScoring = new MyCarrierScoring.VehicleEmploymentScoring(carrier);
            MyCarrierScoring.DriversActivityScoring actScoring = new MyCarrierScoring.DriversActivityScoring();
            sf.addScoringFunction(driverLegScoring);
            sf.addScoringFunction(vehicleEmploymentScoring);
            sf.addScoringFunction(actScoring);
            return sf;
        }

    }

    public static class MyCarrierPlanStrategyManagerFactory implements CarrierPlanStrategyManagerFactory {

        @Inject
        private Network network;

        @Inject
        private LeastCostPathCalculatorFactory leastCostPathCalculatorFactory;

        @Inject
        private Map<String, TravelTime> modeTravelTimes;

        private final CarrierVehicleTypes types;

        public MyCarrierPlanStrategyManagerFactory(CarrierVehicleTypes types) {
            this.types = types;
        }

        @Override
        public GenericStrategyManager<CarrierPlan, Carrier> createStrategyManager() {
            TravelDisutility travelDisutility = TravelDisutilities.createBaseDisutility(types, modeTravelTimes.get(TransportMode.car));
            final LeastCostPathCalculator router = leastCostPathCalculatorFactory.createPathCalculator(network,
                    travelDisutility, modeTravelTimes.get(TransportMode.car));

            final GenericStrategyManager<CarrierPlan, Carrier> strategyManager = new GenericStrategyManager<>();
            strategyManager.setMaxPlansPerAgent(5);
            {
                GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new ExpBetaPlanChanger<CarrierPlan, Carrier>(1.));
                //						strategy.addStrategyModule(new ReRouter(router, services.getNetwork(), services.getLinkTravelTimes(), .1));
                strategyManager.addStrategy(strategy, null, 1.0);

            }
            //					{
            //						GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<CarrierPlan, Carrier>( new ExpBetaPlanChanger<CarrierPlan, Carrier>(1.) ) ;
            //						strategy.addStrategyModule(new ReRouter(router, services.getNetwork(), services.getLinkTravelTimes(), 1.));
            //						strategyManager.addStrategy( strategy, null, 0.1) ;
            //					}
            {
                GenericPlanStrategyImpl<CarrierPlan, Carrier> strategy = new GenericPlanStrategyImpl<>(new KeepSelected<CarrierPlan, Carrier>());
                strategy.addStrategyModule(new TimeAllocationMutator());
                strategy.addStrategyModule(new ReRouteVehicles(router, network, modeTravelTimes.get(TransportMode.car), 1.));
                strategyManager.addStrategy(strategy, null, 0.5);
            }
            //					{
            //						GenericPlanStrategyImpl<CarrierPlan,Carrier> strategy = new GenericPlanStrategyImpl<CarrierPlan,Carrier>( new KeepSelected<CarrierPlan,Carrier>() ) ;
            //                        strategy.addStrategyModule(new ReScheduling(services.getNetwork(),types,services.getLinkTravelTimes(), "sschroeder/input/usecases/chessboard/vrpalgo/algorithm_v2.xml"));
            //                        strategy.addStrategyModule(new ReRouter(router, services.getNetwork(), services.getLinkTravelTimes(), 1.));
            //                        strategyManager.addStrategy( strategy, null, 0.1) ;
            //					}
            return strategyManager;
        }
    }
}

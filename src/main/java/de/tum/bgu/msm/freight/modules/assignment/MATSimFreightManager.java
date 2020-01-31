package de.tum.bgu.msm.freight.modules.assignment;

import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.contrib.freight.Freight;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.controler.CarrierModule;
import org.matsim.contrib.freight.controler.CarrierPlanStrategyManagerFactory;
import org.matsim.contrib.freight.controler.CarrierScoringFunctionFactory;
import org.matsim.contrib.freight.usecases.analysis.CarrierScoreStats;
import org.matsim.contrib.freight.usecases.analysis.LegHistogram;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.Config;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.vehicles.VehicleType;

import javax.inject.Inject;
import java.util.function.BiConsumer;

/**
 * Class that contains the freight extension data containers - carriers, carrier vehicle types, etc.
 */
public class MATSimFreightManager {

    private final Config config;
    private final Scenario scenario;
    private final Controler controler;
    private final Properties properties;

    private final Carriers carriers;
    private final CarrierVehicleTypes carrierVehicleTypes;
    private final Logger logger = Logger.getLogger(MATSimFreightManager.class);

    public Carriers getCarriers() {
        return carriers;
    }

    public CarrierVehicleTypes getCarrierVehicleTypes() {
        return carrierVehicleTypes;
    }

    public MATSimFreightManager(Config config, Scenario scenario, Controler controler, Properties properties) {
        this.config = config;
        this.scenario = scenario;
        this.controler = controler;
        this.properties = properties;
        Freight.configure(controler);
        carriers = FreightUtils.getOrCreateCarriers(scenario);
        carrierVehicleTypes = new CarrierVehicleTypes();
    }

    public void configureMATSimForFreight() {

        new CarrierVehicleTypeReader(carrierVehicleTypes).readFile(properties.getVehicleFileForParcelDelivery());
        new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(carrierVehicleTypes);

        for (VehicleType type : carrierVehicleTypes.getVehicleTypes().values()) {
            if (type.getId().equals(Id.create("van", VehicleType.class))){
                type.setNetworkMode(TransportMode.truck);
            } else if (type.getId().equals(Id.create("cargoBike", VehicleType.class))) {
                type.setNetworkMode("TransportMode.bike");
            }
        }


        logger.info("Vehicle types loaded");

        controler.addOverridingModule(new AbstractModule() {
            @Override
            public void install() {
                CarrierModule carrierModule = new CarrierModule();
                install(carrierModule);

                bind(CarrierPlanStrategyManagerFactory.class).toInstance(new MatsimFreightUtils.MyCarrierPlanStrategyManagerFactory(carrierVehicleTypes));
                bind(CarrierScoringFunctionFactory.class).toInstance(new MatsimFreightUtils.MyCarrierScoringFunctionFactory());
            }
        });

        logger.info("Added freight module");

        controler.addOverridingModule(new AbstractModule() {

            @Override
            public void install() {
                final CarrierScoreStats scores = new CarrierScoreStats(carriers, config.controler().getOutputDirectory() + "/carrier_scores", true);
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

        logger.info("Added freight output plots module");
    }

}

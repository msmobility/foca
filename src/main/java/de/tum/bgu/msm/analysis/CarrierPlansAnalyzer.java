package de.tum.bgu.msm.analysis;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static org.matsim.contrib.freight.carrier.Tour.*;

public class CarrierPlansAnalyzer {

    private final static Logger logger = Logger.getLogger(CarrierPlansAnalyzer.class);

    public static void main(String[] args) throws FileNotFoundException {

        new CarrierPlansAnalyzer().analyzeCarriersPlans(args[0], args[1], args[2]);


    }


    void analyzeCarriersPlans(String carrierVehicleTypeFile, String carriersPlanFile, String outputFile) throws FileNotFoundException {

        PrintWriter pw = new PrintWriter(new File(outputFile));

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Carriers carriers = FreightUtils.getCarriers(scenario);
        CarrierVehicleTypes carrierVehicleTypes = new CarrierVehicleTypes();

        new CarrierVehicleTypeReader(carrierVehicleTypes).readFile(carrierVehicleTypeFile);
        new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(carrierVehicleTypes);


        CarrierPlanXmlReaderV2 carriersPlanReader = new CarrierPlanXmlReaderV2(carriers);
        carriersPlanReader.readFile(carriersPlanFile);

        pw.println("carrier,tour,service,time,type");

        for (Carrier carrier : carriers.getCarriers().values()){

            String carrierId = carrier.getId().toString();

            CarrierPlan plan = carrier.getSelectedPlan();

            for (ScheduledTour tour : plan.getScheduledTours()){
                String tourId = tour.getVehicle().getVehicleId().toString();
                double departureTime_s = tour.getDeparture();
                int numberOfServices = 0;
                for (TourElement element : tour.getTour().getTourElements()){
                    if (element instanceof ServiceActivity){
                        ServiceActivity activity = (ServiceActivity) element;
                        double serviceTime = activity.getDuration();
                        numberOfServices++;
                        pw.println(carrierId + "," + tourId + "," + numberOfServices + "," + serviceTime + ",service");

                    } else if (element instanceof Leg){
                        Leg leg = (Leg) element;
                        double travelTime = leg.getExpectedTransportTime();
                        pw.println(carrierId + "," + tourId + "," + numberOfServices + "," + travelTime + ",leg");
                    }
                }


            }

        }
        pw.close();

    }
}

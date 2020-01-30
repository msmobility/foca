package de.tum.bgu.msm.analysis;


import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Route;
import org.matsim.contrib.freight.carrier.*;
import org.matsim.contrib.freight.utils.FreightUtils;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


import static org.matsim.contrib.freight.carrier.Tour.*;
import static org.matsim.contrib.freight.jsprit.MatsimJspritFactory.createRoute;

public class CarrierPlansAnalyzer {

    private final static Logger logger = Logger.getLogger(CarrierPlansAnalyzer.class);


    public static void main(String[] args) throws FileNotFoundException {


        String[] scenarios = args;
        String baseFolder = "./output/";
        for (String scenario : scenarios) {
            String vehicleTypes = baseFolder + scenario + "/matsim/output_vehicleTypes.xml";
            String carrierPlans = baseFolder + scenario + "/matsim/output_carriers.xml.gz";
            String outputFile = baseFolder + scenario + "/carriers_analysis.csv";
            new CarrierPlansAnalyzer().analyzeCarriersPlans(vehicleTypes, carrierPlans, outputFile);
        }

    }


    public void analyzeCarriersPlans(String carrierVehicleTypeFile, String carriersPlanFile, String outputFile) throws FileNotFoundException {
        Properties properties = new Properties();
        PrintWriter pw = new PrintWriter(new File(outputFile));

        Config config = ConfigUtils.createConfig();
        config.network().setInputFile(properties.getNetworkFile());
        Scenario scenario = ScenarioUtils.loadScenario(config);
        Carriers carriers = FreightUtils.getCarriers(scenario);
        CarrierVehicleTypes carrierVehicleTypes = new CarrierVehicleTypes();

        Network network = scenario.getNetwork();


        new CarrierVehicleTypeReader(carrierVehicleTypes).readFile(carrierVehicleTypeFile);
        new CarrierVehicleTypeLoader(carriers).loadVehicleTypes(carrierVehicleTypes);


        CarrierPlanXmlReader carriersPlanReader = new CarrierPlanXmlReader(carriers);
        carriersPlanReader.readFile(carriersPlanFile);

        pw.println("carrier,tour,service,number_of_services,time,distance,type,vehicle_type,parcels");

        for (Carrier carrier : carriers.getCarriers().values()) {

            String carrierId = carrier.getId().toString();

            CarrierPlan plan = carrier.getSelectedPlan();

            for (ScheduledTour tour : plan.getScheduledTours()) {
                String tourId = tour.getVehicle().getVehicleId().toString();
                String vehType;
                if (tourId.contains("van")){
                    if (tourId.contains("feeder")){
                        vehType= "truck_feeder";
                    } else{
                        vehType = "truck";
                    }
                } else {
                    vehType = "cargoBike";
                }
                double departureTime_s = tour.getDeparture();
                int thisServiceIndex = 0;
                int numberOfServices = 0;
                int numberOfParcels = 0;

                for (TourElement element : tour.getTour().getTourElements()) {
                    if (element instanceof ServiceActivity){
                        numberOfServices++;
                        numberOfParcels+= ((ServiceActivity) element).getService().getCapacityDemand();
                    }
                }


                for (TourElement element : tour.getTour().getTourElements()) {
                    if (element instanceof ServiceActivity) {
                        ServiceActivity activity = (ServiceActivity) element;
                        int parcels_this_service = activity.getService().getCapacityDemand();
                        numberOfParcels -= parcels_this_service;
                        double serviceTime = activity.getDuration();
                        thisServiceIndex++;
                        pw.println(carrierId + "," +
                                tourId + "," +
                                thisServiceIndex + "," +
                                numberOfServices + "," +
                                serviceTime + ",0,service," +
                                vehType + "," +
                                numberOfParcels);

                    } else if (element instanceof Leg) {
                        Leg leg = (Leg) element;
                        double travelTime = leg.getExpectedTransportTime();
                        Route route = leg.getRoute();
                        String description = route.getRouteDescription();
                        double distance = 0;
                        for (String linkId : description.split(" ")) {
                            distance += network.getLinks().get(Id.createLinkId(linkId)).getLength();
                        }

                        pw.println(carrierId + "," +
                                tourId + "," +
                                thisServiceIndex + "," +
                                numberOfServices + "," +
                                travelTime + "," +
                                distance + ",leg," +
                                vehType + "," +
                                numberOfParcels);
                    }
                }


            }

        }
        pw.close();

    }
}

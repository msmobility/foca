package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDTruckTrip;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.SDTruckTrip;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.io.output.OutputWriter;
import de.tum.bgu.msm.freight.modules.assignment.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.longDistanceDisaggregation.FlowsToLDTruckConverter;
import de.tum.bgu.msm.freight.modules.longDistanceDisaggregation.LDTruckODAllocator;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.GlobalModalShareModeChoice;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.ModeChoiceModel;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.SDTruckGenerator;
import de.tum.bgu.msm.freight.modules.syntheticMicroDepotGeneration.SyntheticMicroDepots;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.core.population.io.PopulationWriter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlowsMucRunScenariosWithShares {


    private static final Logger logger = Logger.getLogger(FreightFlowsMucRunScenariosWithShares.class);

    public static void main(String[] args) {

        double shareOfCargoBikes = Double.parseDouble(args[0])/100;



        List<Properties> listOfSimulations = new ArrayList<>();

        {


            Properties properties =new Properties(Properties.initializeResourceBundleFromFile(args[0]));
            properties.initializeRandomNumber();
            properties.flowsProperties.setMatrixFolder("./input/matrices/", properties);
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(0.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(shareOfCargoBikes);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_" + args[0]);
            properties.shortDistance().setReadMicroDepotsFromFile(false);
            try {
                properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            listOfSimulations.add(properties);
        }



        for (Properties properties : listOfSimulations) {
            //adds a 5% pf cars as background traffic
            //properties.setMatsimBackgroundTrafficPlanFile("./input/carPlans/cars_5_percent.xml.gz");
            FreightFlowsMucRunScenariosWithShares freightFlows = new FreightFlowsMucRunScenariosWithShares();
            logger.info("Start simulation " + properties.getRunId());
            freightFlows.run(properties);
            logger.info("End simulation " + properties.getRunId());
        }


    }

    public void run(Properties properties) {

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        SyntheticMicroDepots syntheticMicroDepots = new SyntheticMicroDepots();
        FlowsToLDTruckConverter flowsToLDTruckConverter = new FlowsToLDTruckConverter();
        LDTruckODAllocator LDTruckODAllocator = new LDTruckODAllocator();
        SDTruckGenerator SDTruckGenerator = new SDTruckGenerator();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        ModeChoiceModel modeChoiceModel = new GlobalModalShareModeChoice();
        MATSimAssignment matSimAssignment = new MATSimAssignment();

        syntheticMicroDepots.setup(dataSet, properties);
        flowsToLDTruckConverter.setup(dataSet, properties);
        LDTruckODAllocator.setup(dataSet, properties);
        SDTruckGenerator.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        modeChoiceModel.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        syntheticMicroDepots.run();
        flowsToLDTruckConverter.run();
        LDTruckODAllocator.run();
        SDTruckGenerator.run();
        parcelGenerator.run();
        modeChoiceModel.run();
        matSimAssignment.run();

        PopulationWriter pw;


        String outputFolder = properties.getOutputFolder();
        OutputWriter.printOutObjects(dataSet.getAssignedFlowSegments(), FlowSegment.getHeader(), outputFolder + properties.getRunId() + "/flowSegments.csv");
        OutputWriter.printOutObjects(dataSet.getLDTruckTrips(), LDTruckTrip.getHeader(), outputFolder + properties.getRunId() + "/ld_trucks.csv");
        OutputWriter.printOutObjects(dataSet.getSDTruckTrips(), SDTruckTrip.getHeader(), outputFolder + properties.getRunId() + "/sd_trucks.csv");
        List<Parcel> parcelsList = new ArrayList<>();
        for (List<Parcel> listOfParcelsInDc : dataSet.getParcelsByDistributionCenter().values()) {
            parcelsList.addAll(listOfParcelsInDc);
        }
        OutputWriter.printOutObjects(parcelsList, Parcel.getHeader(), outputFolder + properties.getRunId() + "/parcels.csv");
    }
}

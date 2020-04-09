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
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.*;
import de.tum.bgu.msm.freight.modules.syntheticMicroDepotGeneration.SyntheticMicroDepots;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlowsRunSingle {


    private static final Logger logger = Logger.getLogger(FreightFlowsRunSingle.class);

    public static void main(String[] args) {


        Properties properties = new Properties(Properties.initializeResourceBundleFromFile(args[0]));
        properties.flows().setMatrixFolder("./input/matrices/");
        properties.setAnalysisZones(new int[]{9162});
        properties.setNetworkFile("./networks/matsim/final_V11_emissions.xml.gz");
        properties.setTruckScaleFactor(0.2);
        properties.setSampleFactorForParcels(0.2);
        properties.setIterations(50);
        properties.shortDistance().setSelectedDistributionCenters(new int[]{13,14,16,17,19,20,21,22,23,24});
        properties.setRunId("base");
        properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters.csv");
        //properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0);
        properties.shortDistance().setDistanceBetweenMicrodepotsInGrid(2000.);
        properties.shortDistance().setMaxDistanceToMicroDepot(2000.);

        properties.shortDistance().setReadMicroDepotsFromFile(false);
        properties.longDistance().setDisaggregateLongDistanceFlows(false);
        properties.longDistance().setLongDistanceTruckInputFile("./input/preProcessedInput/ld_trucks_muc.csv");

        properties.modeChoice().setExtraHandlingBike_eur_m3(Double.MAX_VALUE);

        properties.setCountStationLinkListFile("./input/matsim_links_dc20.csv");

        try {
            properties.logProperties("./output/" + properties.getRunId());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //adds a 5% pf cars as background traffic
        properties.setMatsimBackgroundTrafficPlanFile("./input/carPlans/plans_dc_20_20.xml.gz");
        properties.setMatsimAdditionalScaleFactor(0.25/0.20);
        FreightFlowsRunSingle freightFlows = new FreightFlowsRunSingle();
        logger.info("Start simulation " + properties.getRunId());
        freightFlows.run(properties);
        logger.info("End simulation " + properties.getRunId());

    }

    public void run(Properties properties) {
        properties.initializeRandomNumber();

        InputManager io = new InputManager(properties);
        io.readInput();

        DataSet dataSet = io.getDataSet();

        SyntheticMicroDepots syntehticMicroDepots = new SyntheticMicroDepots();

        FlowsToLDTruckConverter flowsToLDTruckConverter = null;
        LDTruckODAllocator LDTruckODAllocator = null;
        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            LDTruckODAllocator = new LDTruckODAllocator();
            flowsToLDTruckConverter = new FlowsToLDTruckConverter();
        }
        SDTruckGenerator SDTruckGenerator = new SDTruckGenerator();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        ModeChoiceModel modeChoiceModel = new ContinuousApproximationModeChoice();
        MATSimAssignment matSimAssignment = new MATSimAssignment();


        syntehticMicroDepots.setup(dataSet, properties);
        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            flowsToLDTruckConverter.setup(dataSet, properties);
            LDTruckODAllocator.setup(dataSet, properties);
        }

        SDTruckGenerator.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        modeChoiceModel.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        syntehticMicroDepots.run();

        if (properties.longDistance().isDisaggregateLongDistanceFlows()){
            flowsToLDTruckConverter.run();
            LDTruckODAllocator.run();

        }
        SDTruckGenerator.run();
        parcelGenerator.run();
        modeChoiceModel.run();
        matSimAssignment.run();

        //PopulationWriter pw;


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

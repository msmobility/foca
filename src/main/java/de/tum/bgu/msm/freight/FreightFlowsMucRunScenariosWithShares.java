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
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.SDTruckGenerator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;
import org.matsim.core.population.io.PopulationWriter;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlowsMucRunScenariosWithShares {


    private static final Logger logger = Logger.getLogger(FreightFlowsMucRunScenariosWithShares.class);

    public static void main(String[] args) {

        List<Properties> listOfSimulations = new ArrayList<>();

        {
            Properties properties = new Properties();
            properties.setMatrixFolder("./input/matrices/");
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(1.);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_100_v2");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
            try {
                properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            listOfSimulations.add(properties);
        }

        {
            Properties properties = new Properties();
            properties.setMatrixFolder("./input/matrices/");
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.8);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_80_v2");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
            try {
                properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            listOfSimulations.add(properties);
        }

        {
            Properties properties = new Properties();
            properties.setMatrixFolder("./input/matrices/");
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.6);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_60_v2");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
            try {
                properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            listOfSimulations.add(properties);
        }

        {
            Properties properties = new Properties();
            properties.setMatrixFolder("./input/matrices/");
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.4);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_40_v2");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
            try {
                properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            listOfSimulations.add(properties);
        }

        {
            Properties properties = new Properties();
            properties.setMatrixFolder("./input/matrices/");
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.2);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_20_v2");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
            try {
                properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            listOfSimulations.add(properties);
        }

        {
            Properties properties = new Properties();
            properties.setMatrixFolder("./input/matrices/");
            properties.setAnalysisZones(new int[]{9162});
            properties.setTruckScaleFactor(1.00);
            properties.setSampleFactorForParcels(.25);
            properties.setIterations(50);
            properties.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.);
            properties.shortDistance().setSelectedDistributionCenters(new int[]{20});
            properties.setRunId("muc_hd_0_v2");
            properties.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
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

        FlowsToLDTruckConverter flowsToLDTruckConverter = new FlowsToLDTruckConverter();
        LDTruckODAllocator LDTruckODAllocator = new LDTruckODAllocator();
        SDTruckGenerator SDTruckGenerator = new SDTruckGenerator();
        ParcelGenerator parcelGenerator = new ParcelGenerator();
        MATSimAssignment matSimAssignment = new MATSimAssignment();


        flowsToLDTruckConverter.setup(dataSet, properties);
        LDTruckODAllocator.setup(dataSet, properties);
        SDTruckGenerator.setup(dataSet, properties);
        parcelGenerator.setup(dataSet, properties);
        matSimAssignment.setup(dataSet, properties);

        flowsToLDTruckConverter.run();
        LDTruckODAllocator.run();
        SDTruckGenerator.run();
        parcelGenerator.run();
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

package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.data.freight.longDistance.FlowSegment;
import de.tum.bgu.msm.freight.data.freight.longDistance.LDTruckTrip;
import de.tum.bgu.msm.freight.data.freight.urban.Parcel;
import de.tum.bgu.msm.freight.data.freight.urban.SDTruckTrip;
import de.tum.bgu.msm.freight.io.input.InputManager;
import de.tum.bgu.msm.freight.io.output.OutputWriter;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.SDTruckGenerator;
import de.tum.bgu.msm.freight.modules.shortDistanceDisaggregation.ParcelGenerator;
import de.tum.bgu.msm.freight.modules.longDistanceDisaggregation.FlowsToLDTruckConverter;
import de.tum.bgu.msm.freight.modules.assignment.MATSimAssignment;
import de.tum.bgu.msm.freight.modules.longDistanceDisaggregation.LDTruckODAllocator;
import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class FreightFlows {


    private static final Logger logger = Logger.getLogger(FreightFlows.class);

    public static void main(String[] args) {

        List<Properties> listOfSimulations = new ArrayList<>();

        Properties properties_zero = new Properties();
        properties_zero.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties_zero.setAnalysisZones(new int[]{9162});
        properties_zero.setTruckScaleFactor(1.00);
        properties_zero.setSampleFactorForParcels(1.00);
        properties_zero.setIterations(50);
        properties_zero.shortDistance().setSelectedDistributionCenters(new int[]{20});
        properties_zero.setRunId("muc_scenario_zero_c");
        properties_zero.setDistributionCentersFile("./input/distributionCenters/distributionCenters_zero_c.xml");
        properties_zero.shortDistance().setShareOfCargoBikesAtZonesServedByMicroDepot(0.0);
        try {
            properties_zero.logProperties("./output/" + properties_zero.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        listOfSimulations.add(properties_zero);


        Properties properties_one = new Properties();
        properties_one.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties_one.setAnalysisZones(new int[]{9162});
        properties_one.setTruckScaleFactor(1.00);
        properties_one.setSampleFactorForParcels(1.00);
        properties_one.setIterations(50);
        properties_one.shortDistance().setSelectedDistributionCenters(new int[]{20});
        properties_one.setRunId("muc_scenario_1km");
        properties_one.setDistributionCentersFile("./input/distributionCenters/distributionCenters_1km.xml");
        try {
            properties_one.logProperties("./output/" + properties_one.getRunId() + "/properties.txt");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        listOfSimulations.add(properties_one);


        Properties properties_two = new Properties();
        properties_two.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties_two.setAnalysisZones(new int[]{9162});
        properties_two.setTruckScaleFactor(1.00);
        properties_two.setSampleFactorForParcels(1.00);
        properties_two.setIterations(50);
        properties_two.shortDistance().setSelectedDistributionCenters(new int[]{20});
        properties_two.setRunId("muc_scenario_paketbox");
        properties_two.setDistributionCentersFile("./input/distributionCenters/distributionCenters_paketbox.xml");
        try {
            properties_two.logProperties("./output/" + properties_two.getRunId() + "/properties.txt");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        //listOfSimulations.add(properties_two);
//
//
        Properties properties_three = new Properties();
        properties_three.setMatrixFileName("./input/matrices/ketten-2010.csv");
        properties_three.setAnalysisZones(new int[]{9162});
        properties_three.setTruckScaleFactor(1.00);
        properties_three.setSampleFactorForParcels(1.00);
        properties_three.setIterations(50);
        properties_three.shortDistance().setSelectedDistributionCenters(new int[]{20});
        properties_three.setRunId("muc_scenario_3km");
        properties_three.setDistributionCentersFile("./input/distributionCenters/distributionCenters_3km.xml");
        try {
            properties_three.logProperties("./output/" + properties_three.getRunId() + "/properties.txt");
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        //listOfSimulations.add(properties_three);

        for (Properties properties : listOfSimulations) {
            FreightFlows freightFlows = new FreightFlows();
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

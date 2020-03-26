package de.tum.bgu.msm.freight;


import de.tum.bgu.msm.freight.properties.Properties;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;

public class FreightFlowsOnlyLD {


    private static final Logger logger = Logger.getLogger(FreightFlowsOnlyLD.class);

    public static void main(String[] args) {

        long time = System.currentTimeMillis();

        int year = 2020;

        Properties properties = new Properties(Properties.initializeResourceBundleFromFile(args[0]));
        properties.flowsProperties.setMatrixFolder("./input/matrices/", properties);
        properties.setAnalysisZones(new int[]{});
        properties.setTruckScaleFactor(0.05);
        properties.setSampleFactorForParcels(0.);
        properties.setIterations(1);
        properties.shortDistance().setSelectedDistributionCenters(new int[]{-1});
        properties.setRunId("ld_all_" + year);

        properties.setYear(year);

        try {
            properties.logProperties("./output/" + properties.getRunId() + "/properties.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        FreightFlowsMucRunScenarios freightFlows = new FreightFlowsMucRunScenarios();
        logger.info("Start simulation " + properties.getRunId());
        freightFlows.run(properties);

        time = (System.currentTimeMillis() - time)/1000/60;

        logger.info("End simulation \"" + properties.getRunId()  + "\" in " + time + " minutes.");
    }

}

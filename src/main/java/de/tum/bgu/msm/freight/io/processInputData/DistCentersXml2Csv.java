package de.tum.bgu.msm.freight.io.processInputData;

import de.tum.bgu.msm.freight.data.DataSet;
import de.tum.bgu.msm.freight.io.input.DistributionCenterReaderXML;
import de.tum.bgu.msm.freight.io.input.ZonesReader;
import de.tum.bgu.msm.freight.io.output.DistributionCenterCsvWriter;
import de.tum.bgu.msm.freight.properties.Properties;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;

public class DistCentersXml2Csv {

    public static void main(String[] args) throws FileNotFoundException {

        DataSet dataSet = new DataSet();
        Properties properties = new Properties(Properties.initializeResourceBundleFromFile(args[0]));
        properties.setAnalysisZones(new int[]{9162,9362});
        ZonesReader zonesReader = new ZonesReader(dataSet, properties);
        zonesReader.read();
        DistributionCenterReaderXML distributionCenterReaderXML = new DistributionCenterReaderXML(dataSet);
        distributionCenterReaderXML.setValidating(false);
        distributionCenterReaderXML.read(args[0]);

        new DistributionCenterCsvWriter().writeToCsv(dataSet, args[1]);


    }

}

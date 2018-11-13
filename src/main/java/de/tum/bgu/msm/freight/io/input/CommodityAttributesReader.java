package de.tum.bgu.msm.freight.io.input;

import de.tum.bgu.msm.freight.data.Commodity;
import de.tum.bgu.msm.freight.data.DistanceBin;
import de.tum.bgu.msm.freight.data.FreightFlowsDataSet;
import de.tum.bgu.msm.freight.io.CSVReader;
import de.tum.bgu.msm.freight.properties.Properties;
import de.tum.bgu.msm.util.MitoUtil;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class CommodityAttributesReader extends CSVReader {

    private final static Logger logger = Logger.getLogger(ZonesReader.class);
    private int codeIndex;
    private Map<DistanceBin, Integer> truckLoadIndexes = new HashMap<>();
    private Map<DistanceBin, Integer> emptyTruckIndexes = new HashMap<>();

    private Properties properties;

    protected CommodityAttributesReader(FreightFlowsDataSet dataSet, Properties properties) {
        super(dataSet);
        this.properties = properties;
    }

    @Override
    protected void processHeader(String[] header) {

        codeIndex = MitoUtil.findPositionInArray("Code", header);

        for (DistanceBin bin : DistanceBin.values()){
            String titleLoad = "load_" + bin.toString();
            String titleEmpty = "empty_" + bin.toString();
            truckLoadIndexes.put(bin,MitoUtil.findPositionInArray(titleLoad, header));
            emptyTruckIndexes.put(bin,MitoUtil.findPositionInArray(titleEmpty, header));
        }

    }

    @Override
    protected void processRecord(String[] record) {
        Commodity commodity = Commodity.getMapOfValues().get(Integer.parseInt(record[codeIndex]));

        for (DistanceBin bin : DistanceBin.values()){
            dataSet.getTruckLoadsByDistanceAndCommodity().put(commodity, bin, Double.parseDouble(record[truckLoadIndexes.get(bin)]));
            dataSet.getEmptyTruckProportionsByDistanceAndCommodity().put(commodity, bin, Double.parseDouble(record[emptyTruckIndexes.get(bin)]));
        }

    }

    @Override
    public void read() {
        super.read(properties.getCommodityAttributeFile(), ",");
        logger.info("Commodity attributes read");

    }
}
